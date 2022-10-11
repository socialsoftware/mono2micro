package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

public class RecommendationFactory {
    public static Recommendation getRecommendation(RecommendationDto recommendationDto) {
        switch (recommendationDto.getDecompositionType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                return new RecommendMatrixSciPy((RecommendMatrixSciPyDto) recommendationDto);
            default:
                throw new RuntimeException("The type \"" + recommendationDto.getDecompositionType() + "\" is not a valid decomposition type for recommendation creation.");
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
