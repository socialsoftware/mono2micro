package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.DendrogramService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

@Service
public class AccessesSciPyStrategyService {

    @Autowired
    DendrogramService dendrogramService;

    @Autowired
    RecommendationService recommendationService;

    public void deleteStrategyProperties(AccessesSciPyStrategy strategy) {
        for (Dendrogram dendrogram: strategy.getDendrograms())
            dendrogramService.deleteDendrogram(dendrogram);
        for (Recommendation recommendation: strategy.getRecommendations())
            recommendationService.deleteRecommendation(recommendation);
    }
}
