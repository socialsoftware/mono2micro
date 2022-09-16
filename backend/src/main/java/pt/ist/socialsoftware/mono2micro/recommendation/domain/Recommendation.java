package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

public abstract class Recommendation {
    @Id
    String name;
    @DBRef(lazy = true)
    Strategy strategy;
    String recommendationResultName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public abstract String getType();

    public abstract boolean equalsDto(RecommendationDto dto);

    public String getRecommendationResultName() {
        return recommendationResultName;
    }

    public void setRecommendationResultName(String recommendationResultName) {
        this.recommendationResultName = recommendationResultName;
    }
}
