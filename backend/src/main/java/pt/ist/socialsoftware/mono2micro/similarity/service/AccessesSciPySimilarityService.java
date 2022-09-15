package pt.ist.socialsoftware.mono2micro.similarity.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.similarity.dto.AccessesSciPySimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.repository.AccessesSciPySimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AccessesSciPySimilarityService {
    @Autowired
    AccessesSciPySimilarityRepository similarityRepository;

    @Autowired
    AccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    GridFsService gridFsService;

    public void createSimilarity(AccessesSciPySimilarityDto similarityDto) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(similarityDto.getStrategyName());
        if (strategy.getSimilarities().stream().anyMatch(similarity -> similarity.equalsDto(similarityDto)))
            return;

        AccessesSciPySimilarity similarity = getNewAccessesSciPySimilarityForStrategy(strategy, similarityDto);
        similarityGenerator.createSimilarityMatrixForSciPy(similarity);
        similarityGenerator.createAccessesSciPyDendrogram(similarity);
        strategyRepository.save(strategy);
        similarityRepository.save(similarity);
    }

    public AccessesSciPySimilarity getNewAccessesSciPySimilarityForStrategy(AccessesSciPyStrategy strategy, AccessesSciPySimilarityDto similarityDto) {
        AccessesSciPySimilarity similarity = new AccessesSciPySimilarity(similarityDto);
        int i = 0;
        String similarityName;
        do {
            similarityName = similarityDto.getStrategyName() + " S" + ++i;
        } while (similarityRepository.existsByName(similarityName));
        similarity.setName(similarityName);
        similarity.setStrategy(strategy);
        strategy.addSimilarity(similarity);
        return similarity;
    }

    public byte[] getDendrogramImage(String similarityName) throws IOException {
        AccessesSciPySimilarity similarity = similarityRepository.getDendrogramImage(similarityName);
        InputStream inputStream = gridFsService.getFile(similarity.getDendrogramName());
        return IOUtils.toByteArray(inputStream);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteSimilarityProperties(AccessesSciPySimilarity similarity) { // Used to delete fields that can't otherwise be accessed by abstract Similarity
        AccessesSciPyStrategy strategy = strategyRepository.findByName(similarity.getStrategy().getName());
        strategy.removeSimilarity(similarity.getName());
        strategyRepository.save(strategy);
        gridFsService.deleteFile(similarity.getSimilarityMatrixName());
        gridFsService.deleteFile(similarity.getDendrogramName());
        gridFsService.deleteFile(similarity.getCopheneticDistanceName());
    }
}
