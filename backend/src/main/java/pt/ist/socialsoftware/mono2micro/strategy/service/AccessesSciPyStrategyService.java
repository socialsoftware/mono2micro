package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.service.SimilarityService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

@Service
public class AccessesSciPyStrategyService {

    @Autowired
    SimilarityService similarityService;

    @Autowired
    RecommendationService recommendationService;

    public void deleteStrategyProperties(AccessesSciPyStrategy strategy) {
        for (Similarity similarity : strategy.getSimilarities())
            similarityService.deleteSimilarity(similarity);
        for (Recommendation recommendation: strategy.getRecommendations())
            recommendationService.deleteRecommendation(recommendation);
    }
}
