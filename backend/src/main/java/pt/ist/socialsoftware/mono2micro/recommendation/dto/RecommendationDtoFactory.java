package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy.RECOMMEND_MATRIX_SCIPY;

public class RecommendationDtoFactory {
    public static RecommendationDto getRecommendationDto(Recommendation recommendation) {
        switch (recommendation.getType()) {
            case RECOMMEND_MATRIX_SCIPY:
                return new RecommendMatrixSciPyDto((RecommendMatrixSciPy) recommendation);
            default:
                throw new RuntimeException("The recommendation type " + recommendation.getType() + " is not present.");
        }
    }
}
