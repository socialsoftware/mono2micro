package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;

public class RecommendationDtoFactory {
    public static RecommendationDto getRecommendationDto(Recommendation recommendation) {
        switch (recommendation.getType()) {
            case RECOMMEND_MATRIX_SCIPY:
                return new RecommendMatrixSciPyDto((RecommendMatrixSciPy) recommendation);
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
                return new RecommendMatrixClassVectorizationDto((RecommendMatrixSciPy) recommendation);
            case RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
                return new RecommendMatrixEntityVectorizationDto((RecommendMatrixSciPy) recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new RecommendMatrixFunctionalityVectorizationCallGraphDto((RecommendMatrixSciPy) recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new RecommendMatrixFunctionalityVectorizationSequenceOfAcessesDto((RecommendMatrixSciPy) recommendation);
            default:
                throw new RuntimeException("The recommendation type " + recommendation.getType() + " is not present.");
        }
    }
}
