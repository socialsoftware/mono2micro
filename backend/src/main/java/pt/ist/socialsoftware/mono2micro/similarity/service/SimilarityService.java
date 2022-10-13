package pt.ist.socialsoftware.mono2micro.similarity.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityFactory;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

        similarity.generate();

        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }

    public void deleteSingleSimilarity(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);

        for (Decomposition decomposition: similarity.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);

        similarity.removeProperties();

        Strategy strategy = similarity.getStrategy();
        strategy.removeSimilarity(similarity.getName());
        strategyRepository.save(strategy);

        similarityRepository.deleteByName(similarityName);
    }

    public void deleteSimilarity(Similarity similarity) { // Used when strategy is deleted
        similarity.removeProperties();
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
