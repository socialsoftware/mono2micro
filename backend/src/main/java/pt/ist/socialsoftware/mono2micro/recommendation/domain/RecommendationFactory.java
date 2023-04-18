package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.dto.*;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;

public class RecommendationFactory {
    public static Recommendation getRecommendation(RecommendationDto recommendationDto) {
        switch (recommendationDto.getType()) {
            case RECOMMEND_MATRIX_SCIPY:
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
            case RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new RecommendMatrixSciPy((RecommendMatrixSciPyDto) recommendationDto);
            default:
                throw new RuntimeException("The type \"" + recommendationDto.getType() + "\" is not a valid decomposition type for recommendation creation.");
        }
    }

    public static Recommendation getRecommendation(Strategy strategy, RecommendationDto recommendationDto) {
        Recommendation recommendation = getRecommendation(recommendationDto);
        recommendation.setCompleted(false);

        // Get new name
        int i = 0;
        String recommendationName;
        do {
            recommendationName = strategy.getName() + " | " + " Recommendation " + ++i;
        } while (strategy.containsRecommendationName(recommendationName));
        recommendation.setName(recommendationName);
        recommendation.setRecommendationResultName(recommendation.getName() + "_recommendationResult");
        strategy.addRecommendation(recommendation);
        recommendation.setStrategy(strategy);

        return recommendation;
    }
}
