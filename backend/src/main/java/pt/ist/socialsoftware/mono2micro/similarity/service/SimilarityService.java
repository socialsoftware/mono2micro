package pt.ist.socialsoftware.mono2micro.similarity.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityFactory;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.generator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.SimilarityMatrixGeneratorService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.SimilaritiesStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram.DENDROGRAM;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.generator.SimilarityMatrix.SIMILARITY_MATRIX;

@Service
public class SimilarityService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    SimilarityMatrixGeneratorService similarityMatrixGeneratorService;

    @Autowired
    SciPyClusteringAlgorithmService sciPyClusteringAlgorithmService;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    GridFsService gridFsService;

    public void createSimilarity(SimilarityDto similarityDto) throws Exception {
        SimilaritiesStrategy similaritiesStrategy = (SimilaritiesStrategy) strategyRepository.findByName(similarityDto.getStrategyName());
        if (similaritiesStrategy.getSimilarities().stream().anyMatch(similarity -> similarity.equalsDto(similarityDto)))
            return;
        Similarity similarity = getNewSimilarityForStrategy(similaritiesStrategy, similarityDto);

        if (similarity.containsImplementation(SIMILARITY_MATRIX)) {
            similarityMatrixGeneratorService.createSimilarityMatrixFromWeights((SimilarityMatrix) similarity);
        }
        if (similarity.containsImplementation(DENDROGRAM)) {
            sciPyClusteringAlgorithmService.createDendrogramImage((Dendrogram) similarity);
        }
        //ADD NEW ENTRIES HERE

        similarityRepository.save(similarity);
        strategyRepository.save((Strategy) similaritiesStrategy);
    }

    private void removeSpecificSimilarityProperties(Similarity similarity) {
        if (similarity.containsImplementation(SIMILARITY_MATRIX)) {
            SimilarityMatrix s = (SimilarityMatrix) similarity;
            gridFsService.deleteFile(s.getSimilarityMatrixName());
        }
        //else if (similarity.containsImplementation(SCIPY)) {
        //}
        else if (similarity.containsImplementation(DENDROGRAM)) {
            Dendrogram s = (Dendrogram) similarity;
            gridFsService.deleteFile(s.getDendrogramName());
            gridFsService.deleteFile(s.getCopheneticDistanceName());
        }
        // ADD NEW ENTRIES HERE
    }

    public Similarity getNewSimilarityForStrategy(SimilaritiesStrategy similaritiesStrategy, SimilarityDto similarityDto) {
        Similarity similarity = SimilarityFactory.getFactory().getSimilarity(similarityDto);

        int i = 0;
        String similarityName;
        do {
            similarityName = similaritiesStrategy.getName() + " S" + ++i;
        } while (similarityRepository.existsByName(similarityName));
        similarity.setName(similarityName);

        similarity.setStrategy((Strategy) similaritiesStrategy);
        similaritiesStrategy.addSimilarity(similarity);
        return similarity;
    }

    public void deleteSingleSimilarity(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);

        for (Decomposition decomposition: similarity.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);
        removeSpecificSimilarityProperties(similarity);

        Strategy strategy = similarity.getStrategy();
        ((SimilaritiesStrategy) strategy).removeSimilarity(similarity.getName());
        strategyRepository.save(strategy);

        similarityRepository.deleteByName(similarityName);
    }

    public void deleteSimilarity(Similarity similarity) { // Used when strategy is deleted
        removeSpecificSimilarityProperties(similarity);
        similarityRepository.deleteByName(similarity.getName());
    }

    public List<Decomposition> getDecompositions(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);
        return similarity.getDecompositions();
    }

    public Similarity getSimilarity(String similarityName) {
        return similarityRepository.findByName(similarityName);
    }

    public byte[] getDendrogramImage(String similarityName) throws IOException {
        Dendrogram similarity = (Dendrogram) similarityRepository.findByName(similarityName);
        InputStream inputStream = gridFsService.getFile(similarity.getDendrogramName());
        return IOUtils.toByteArray(inputStream);
    }
}
