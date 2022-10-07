package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RecommendForSciPyDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = RecommendForSciPyDto.class, name = REPOSITORY_SCIPY),
})
public abstract class RecommendationDto {
    String strategyName;
    String name;
    String type;
    boolean isCompleted;

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
