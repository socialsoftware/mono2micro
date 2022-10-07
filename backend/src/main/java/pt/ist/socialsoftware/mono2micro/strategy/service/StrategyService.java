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
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;
import pt.ist.socialsoftware.mono2micro.similarity.service.SimilarityService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.StrategyFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.RecommendationsStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.SimilaritiesStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.RecommendationsStrategy.CONTAINS_RECOMMENDATIONS;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.SimilaritiesStrategy.CONTAINS_SIMILARITIES;

@Service
public class StrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    RepresentationService representationService;

    @Autowired
    SimilarityService similarityService;

    @Autowired
    RecommendationService recommendationService;

    public void createStrategy(String codebaseName, String strategyType, List<String> representationTypes, List<Object> representations) throws Exception {
        representationService.addRepresentations(codebaseName, representationTypes, representations);

        Codebase codebase = codebaseRepository.findByName(codebaseName);
        if (codebase.getStrategyByType(strategyType) == null) {
            Strategy strategy = StrategyFactory.getFactory().getStrategy(strategyType);
            strategy.setName(codebaseName + " & " + strategyType);
            if (strategy.getRepresentationTypes().stream().allMatch(representationType -> codebase.getRepresentationByType(representationType) != null)) { // Check if all required representations exist
                strategy.setCodebase(codebase);
                codebase.addStrategy(strategy);
                strategyRepository.save(strategy);
                codebaseRepository.save(codebase);
            }
        }
    }

    public void removeSpecificStrategyProperties(Strategy strategy) {
        if (strategy.containsImplementation(CONTAINS_SIMILARITIES)) {
            deleteStrategySimilarities((SimilaritiesStrategy) strategy);
        }
        if (strategy.containsImplementation(CONTAINS_RECOMMENDATIONS)) {
            deleteStrategyRecommendations((RecommendationsStrategy) strategy);
        }
        // ADD OTHER ENTRIES HERE
    }

    public List<Similarity> getStrategySimilarities(String strategyName) {
        Strategy strategy = strategyRepository.findByName(strategyName);
        if (strategy.containsImplementation(CONTAINS_SIMILARITIES))
            return ((SimilaritiesStrategy) strategy).getSimilarities();
        else throw new RuntimeException("Type " + strategy.getType() + "does not have similarities.");
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

    private void deleteStrategySimilarities(SimilaritiesStrategy strategy) {
        for (Similarity similarity : strategy.getSimilarities())
            similarityService.deleteSimilarity(similarity);
    }

    private void deleteStrategyRecommendations(RecommendationsStrategy strategy) {
        for (Recommendation recommendation: strategy.getRecommendations())
            recommendationService.deleteRecommendation(recommendation);
    }
}