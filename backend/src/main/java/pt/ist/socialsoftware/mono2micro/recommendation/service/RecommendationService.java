package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendAccessesSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;

import java.io.IOException;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Service
public class RecommendationService {
    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    RecommendAccessesSciPyService recommendAccessesSciPyService;

    public RecommendationDto createRecommendation(RecommendationDto recommendationDto) {
        switch (recommendationDto.getType()) {
            case ACCESSES_SCIPY:
                return new RecommendAccessesSciPyDto(recommendAccessesSciPyService.recommendAccessesSciPy((RecommendAccessesSciPyDto) recommendationDto));
            default:
                throw new RuntimeException("No know type " + recommendationDto.getType() + ".");
        }
    }

    public String getRecommendationResult(String recommendationName) throws IOException {
        Recommendation recommendation = recommendationRepository.getRecommendationResultName(recommendationName);
        switch (recommendation.getType()) {
            case ACCESSES_SCIPY:
                return recommendAccessesSciPyService.getRecommendationResult(recommendation);
            default:
                throw new RuntimeException("No know type " + recommendation.getType() + ".");
        }
    }

    public void createDecompositions(String recommendationName, List<String> decompositionNames) throws Exception {
        Recommendation recommendation = recommendationRepository.findByName(recommendationName);
        switch (recommendation.getType()) {
            case ACCESSES_SCIPY:
                recommendAccessesSciPyService.createDecompositions((RecommendAccessesSciPy) recommendation, decompositionNames);
                return;
            default:
                throw new RuntimeException("No know type " + recommendation.getType() + ".");
        }
    }

    public void deleteRecommendation(Recommendation recommendation) {
        switch (recommendation.getType()) {
            case ACCESSES_SCIPY:
                recommendAccessesSciPyService.deleteStrategyProperties((RecommendAccessesSciPy) recommendation);
                break;
            default:
                throw new RuntimeException("No know type " + recommendation.getType() + ".");
        }
        recommendationRepository.delete(recommendation);
    }
}