package pt.ist.socialsoftware.mono2micro.similarity.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityFactory;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityGenerator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.SimilarityMatrixGenerator;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram.DENDROGRAM;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityGenerator.SimilarityMatrix.SIMILARITY_MATRIX;

@Service
public class SimilarityService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    GridFsService gridFsService;

    public void createSimilarity(SimilarityDto similarityDto) throws Exception {
        Strategy strategy = strategyRepository.findByName(similarityDto.getStrategyName());
        if (strategy.getSimilarities().stream().anyMatch(similarity -> similarity.equalsDto(similarityDto)))
            return;
        Similarity similarity = SimilarityFactory.getSimilarity(strategy, similarityDto);

        if (similarity.containsImplementation(SIMILARITY_MATRIX)) {
            SimilarityMatrixGenerator generator = new SimilarityMatrixGenerator(gridFsService);
            generator.createSimilarityMatrixFromWeights((SimilarityMatrix) similarity);
        }
        if (similarity.containsImplementation(DENDROGRAM)) {
            SciPyClustering clusteringAlgorithm = new SciPyClustering();
            clusteringAlgorithm.createDendrogramImage((Dendrogram) similarity);
        }
        //ADD NEW ENTRIES HERE

        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }

    private void removeSpecificSimilarityProperties(Similarity similarity) {
        if (similarity.containsImplementation(SIMILARITY_MATRIX)) {
            SimilarityMatrix s = (SimilarityMatrix) similarity;
            gridFsService.deleteFile(s.getSimilarityMatrixName());
        }

        if (similarity.containsImplementation(DENDROGRAM)) {
            Dendrogram s = (Dendrogram) similarity;
            gridFsService.deleteFile(s.getDendrogramName());
            gridFsService.deleteFile(s.getCopheneticDistanceName());
        }
        // ADD NEW ENTRIES HERE
    }

    public void deleteSingleSimilarity(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);

        for (Decomposition decomposition: similarity.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);
        removeSpecificSimilarityProperties(similarity);

        Strategy strategy = similarity.getStrategy();
        strategy.removeSimilarity(similarity.getName());
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
