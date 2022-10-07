package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;

public abstract class Recommendation {
    @Id
    String name;

    String type;

    @DBRef(lazy = true)
    Strategy strategy;
    String recommendationResultName;
    boolean isCompleted; // true when all the decompositions are calculated

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

    public abstract List<String> getImplementations();

    public boolean containsImplementation(String implementation) {
        return getImplementations().contains(implementation);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public abstract boolean equalsDto(RecommendationDto dto);

    public String getRecommendationResultName() {
        return recommendationResultName;
    }

    public void setRecommendationResultName(String recommendationResultName) {
        this.recommendationResultName = recommendationResultName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
