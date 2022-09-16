package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.AccessesSciPyDecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Service
public class DecompositionService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    AccessesSciPyDecompositionService accessesSciPyDecompositionService;

    public void createDecomposition(DecompositionDto decompositionDto) throws Exception {
        switch (decompositionDto.getType()) {
            case ACCESSES_SCIPY:
                AccessesSciPyDecompositionDto dto = (AccessesSciPyDecompositionDto) decompositionDto;
                accessesSciPyDecompositionService.createDecomposition(dto.getSimilarityName(), dto.getCutType(), dto.getCutValue());
                return;
            default:
                throw new RuntimeException("No implementation type given in decompositionDto");
        }
    }

    public Decomposition getDecomposition(String decompositionName) {
        return decompositionRepository.findByName(decompositionName);
    }

    public void deleteSingleDecomposition(String decompositionName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        removeSpecificDecompositionProperties(decomposition);
        Strategy strategy = strategyRepository.findByName(decomposition.getStrategy().getName());
        strategy.removeDecomposition(decomposition.getName());
        strategyRepository.save(strategy);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void deleteDecomposition(Decomposition decomposition) {
        removeSpecificDecompositionProperties(decomposition);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    private void removeSpecificDecompositionProperties(Decomposition decomposition) {
        switch (decomposition.getStrategyType()) {
            case ACCESSES_SCIPY:
                accessesSciPyDecompositionService.deleteDecompositionProperties((AccessesSciPyDecomposition) decomposition);
                break;
        }
    }

    public void mergeClusters(String decompositionName,  String clusterName,  String otherClusterName,  String newName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                accessesSciPyDecompositionService.mergeClustersOperation((AccessesSciPyDecomposition) decomposition, clusterName, otherClusterName, newName);
                return;
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }

    public void renameCluster(String decompositionName,  String clusterName,  String newName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                accessesSciPyDecompositionService.renameClusterOperation((AccessesSciPyDecomposition) decomposition, clusterName, newName);
                return;
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }

    public void splitCluster(String decompositionName,  String clusterName,  String newName, String entities) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                accessesSciPyDecompositionService.splitClusterOperation((AccessesSciPyDecomposition) decomposition, clusterName, newName, entities);
                return;
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }

    public void transferEntities(String decompositionName,  String clusterName,  String toClusterName, String entities) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                accessesSciPyDecompositionService.transferEntitiesOperation((AccessesSciPyDecomposition) decomposition, clusterName, toClusterName, entities);
                return;
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }
}