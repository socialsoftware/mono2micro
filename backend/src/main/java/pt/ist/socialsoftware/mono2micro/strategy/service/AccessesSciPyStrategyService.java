package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AccessesSciPyStrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;
    @Autowired
    AccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    GridFsService gridFsService;

    public void createStrategy(AccessesSciPyStrategyDto strategyDto) throws Exception {
        Codebase codebase = codebaseRepository.findByName(strategyDto.getCodebaseName());
        if (codebase.getStrategies().stream().anyMatch(strategy -> strategy.equalsDto(strategyDto)))
            return;

        AccessesSciPyStrategy strategy = getNewAccesssesSciPyStrategyForCodebase(codebase, strategyDto);
        similarityGenerator.createSimilarityMatrixForSciPy(strategy);
        clusteringAlgorithm.createAccessesSciPyDendrogram(strategy);
        codebaseRepository.save(codebase);
        strategyRepository.save(strategy);
    }

    public AccessesSciPyStrategy getNewAccesssesSciPyStrategyForCodebase(Codebase codebase, AccessesSciPyStrategyDto strategyDto) {
        AccessesSciPyStrategy strategy = new AccessesSciPyStrategy(strategyDto);
        int i = 0;
        String strategyName;
        do {
            strategyName = strategyDto.getCodebaseName() + " & " + strategyDto.getType() + ++i;
        } while (strategyRepository.existsByName(strategyName));
        strategy.setName(strategyName);
        strategy.setCodebase(codebase);
        codebase.addStrategy(strategy);
        return strategy;
    }

    public byte[] getDendrogramImage(String strategyName) throws IOException {
        AccessesSciPyStrategy strategy = strategyRepository.getDendrogramImage(strategyName);
        InputStream inputStream = gridFsService.getFile(strategy.getImageName());
        return IOUtils.toByteArray(inputStream);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteStrategyProperties(AccessesSciPyStrategy strategy) { // Used to delete fields that can't otherwise be accessed by abstract Strategy
        gridFsService.deleteFile(strategy.getSimilarityMatrixName());
        gridFsService.deleteFile(strategy.getImageName());
        gridFsService.deleteFile(strategy.getCopheneticDistanceName());
    }
}
