package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.service.SimilarityService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

@Service
public class StrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    SimilarityService similarityService;

    @Autowired
    RecommendationService recommendationService;

    public void createStrategy(String codebaseName, String algorithmType, List<String> strategyTypes) {
        Codebase codebase = codebaseRepository.findByName(codebaseName);
        for (Strategy strategy : codebase.getStrategies())
            if (strategy.getAlgorithmType().equals(algorithmType) &&
                    strategy.getStrategyTypes().size() == strategyTypes.size() &&
                    strategy.getStrategyTypes().containsAll(strategyTypes)) // strategy already exists
                return;

        Strategy strategy = new Strategy(codebase, algorithmType, strategyTypes);
        strategy.setCodebase(codebase);
        codebase.addStrategy(strategy);
        strategyRepository.save(strategy);
        codebaseRepository.save(codebase);
    }

    public void removeSpecificStrategyProperties(Strategy strategy) {
        deleteStrategySimilarities(strategy);
        deleteStrategyRecommendations(strategy);
    }

    public List<Similarity> getStrategySimilarities(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        return strategy.getSimilarities();
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

    public Strategy getStrategy(String strategyName) {
        return strategyRepository.findByName(strategyName);
    }

    public List<Decomposition> getStrategyDecompositions(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        return strategy.getDecompositions();
    }

    private void deleteStrategySimilarities(Strategy strategy) {
        for (Similarity similarity : strategy.getSimilarities())
            similarityService.deleteSimilarity(similarity);
    }

    private void deleteStrategyRecommendations(Strategy strategy) {
        for (Recommendation recommendation: strategy.getRecommendations())
            recommendationService.deleteRecommendation(recommendation);
    }
}