package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.StrategyFactory;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Service
public class StrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    SourceService sourceService;

    @Autowired
    AccessesSciPyStrategyService accessesSciPyStrategyService;

    public void createStrategy(String codebaseName, String strategyType, List<String> sourceTypes, List<Object> sources) throws Exception {
        sourceService.addSources(codebaseName, sourceTypes, sources);

        Codebase codebase = codebaseRepository.findByName(codebaseName);
        if (codebase.getStrategyByType(strategyType) == null) {
            Strategy strategy = StrategyFactory.getFactory().getStrategy(strategyType);
            strategy.setName(codebaseName + " & " + strategyType);
            if (strategy.getSourceTypes().stream().allMatch(sourceType -> codebase.getSourceByType(sourceType) != null)) { // Check if all required sources exist
                strategy.setCodebase(codebase);
                codebase.addStrategy(strategy);
                strategyRepository.save(strategy);
                codebaseRepository.save(codebase);
            }
        }
    }
    public void deleteSingleStrategy(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        strategy.getCodebase().removeStrategy(strategyName);
        for (Decomposition decomposition: strategy.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);
        removeSpecificStrategyProperties(strategy);
        strategyRepository.delete(strategy);
        codebaseRepository.save(strategy.getCodebase());
    }
    public void deleteStrategy(Strategy strategy) {
        for (Decomposition decomposition: strategy.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);
        removeSpecificStrategyProperties(strategy);
        strategyRepository.delete(strategy);
    }
    public void removeSpecificStrategyProperties(Strategy strategy) {
        switch (strategy.getType()) {
            case ACCESSES_SCIPY:
                accessesSciPyStrategyService.deleteStrategyProperties((AccessesSciPyStrategy) strategy);
                break;
            default:
                throw new RuntimeException("No know type " + strategy.getType() + ".");
        }
    }
    public Strategy getStrategy(String strategyName) {
        return strategyRepository.findByName(strategyName);
    }
    public List<Dendrogram> getStrategyDendrograms(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        switch (strategy.getType()) {
            case ACCESSES_SCIPY:
                return ((AccessesSciPyStrategy) strategy).getDendrograms();
            default:
                throw new RuntimeException("Type " + strategy.getType() + "does not have dendrograms.");
        }
    }
    public List<Decomposition> getStrategyDecompositions(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        return strategy.getDecompositions();
    }
}
