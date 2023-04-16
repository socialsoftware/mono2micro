package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RecommendMatrixSciPyDto.class, name = RECOMMEND_MATRIX_SCIPY),
        @JsonSubTypes.Type(value = RecommendMatrixSciPyDto.class, name = RECOMMEND_MATRIX_CLASS_VECTORIZATION),
        @JsonSubTypes.Type(value = RecommendMatrixSciPyDto.class, name = RECOMMEND_MATRIX_ENTITY_VECTORIZATION),
        @JsonSubTypes.Type(value = RecommendMatrixSciPyDto.class, name = RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH),
        @JsonSubTypes.Type(value = RecommendMatrixSciPyDto.class, name = RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES)
})
public abstract class RecommendationDto {
    String type;
    String strategyName;
    String name;
    boolean isCompleted;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
