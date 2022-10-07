package pt.ist.socialsoftware.mono2micro.strategy.domain.inteface;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

import java.util.List;

public interface RecommendationsStrategy {
    String CONTAINS_RECOMMENDATIONS = "CONTAINS_RECOMMENDATIONS";
    List<Recommendation> getRecommendations();
    void setRecommendations(List<Recommendation> recommendations);
    default void addRecommendation(Recommendation recommendation) {
        getRecommendations().add(recommendation);
    }
}
