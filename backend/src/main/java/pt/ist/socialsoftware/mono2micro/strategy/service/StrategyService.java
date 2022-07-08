package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy.RECOMMENDATION_ACCESSES_SCIPY;

@Service
public class StrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    AccessesSciPyStrategyService accessesSciPyStrategyService;

    @Autowired
    RecommendAccessesSciPyStrategyService recommendAccessesSciPyStrategy;

    @Autowired
    DecompositionService decompositionService;

    public void deleteSingleStrategy(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        Codebase codebase = strategy.getCodebase();
        codebase.removeStrategy(strategyName);
        strategy.getDecompositions().forEach(decomposition -> decompositionService.deleteDecomposition(decomposition));
        removeSpecificStrategyProperties(strategy);
        codebaseRepository.save(codebase);
        strategyRepository.deleteByName(strategyName);
    }

    public void deleteStrategy(Strategy strategy) { // Used when codebase is deleted
        strategy.getDecompositions().forEach(decomposition -> decompositionService.deleteDecomposition(decomposition));
        removeSpecificStrategyProperties(strategy);
        strategyRepository.deleteByName(strategy.getName());
    }

    private void removeSpecificStrategyProperties(Strategy strategy) {
        switch (strategy.getType()) {
            case ACCESSES_SCIPY:
                accessesSciPyStrategyService.deleteStrategyProperties((AccessesSciPyStrategy) strategy);
                break;
            case RECOMMENDATION_ACCESSES_SCIPY:
                recommendAccessesSciPyStrategy.deleteStrategyProperties((RecommendAccessesSciPyStrategy) strategy);
                break;
        }
    }

    public List<Decomposition> getDecompositions(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        return strategy.getDecompositions();
    }
}
