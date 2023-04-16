package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;

public class RecommendationDtoFactory {
    public static RecommendationDto getRecommendationDto(Recommendation recommendation) {
        switch (recommendation.getType()) {
            case RECOMMEND_MATRIX_SCIPY:
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
            case RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new RecommendMatrixSciPyDto((RecommendMatrixSciPy) recommendation);
            default:
                throw new RuntimeException("The recommendation type " + recommendation.getType() + " is not present.");
        }
    }
}
