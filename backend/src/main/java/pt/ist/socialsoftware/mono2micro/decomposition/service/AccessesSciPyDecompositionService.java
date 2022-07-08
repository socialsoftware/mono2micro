package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;

import java.util.Optional;

@Service
public class AccessesSciPyDecompositionService {
    @Autowired
    AccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    SciPyClusteringAlgorithmService clusteringService;

    public void createDecomposition(String strategyName, String cutType, float cutValue) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(strategyName);
        clusteringService.createDecomposition(strategy, cutType, cutValue);
    }

    public void createExpertDecomposition(String strategyName, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(strategyName);
        clusteringService.createExpertDecomposition(strategy, expertName, expertFile);
    }

    public void deleteDecompositionProperties(AccessesSciPyDecomposition decomposition) {
        //TODO remove history
    }
}
