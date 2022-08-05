package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Service
public class RecommendationService {
    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    RecommendAccessesSciPyService recommendAccessesSciPyService;

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
