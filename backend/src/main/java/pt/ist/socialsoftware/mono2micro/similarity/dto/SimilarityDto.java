package pt.ist.socialsoftware.mono2micro.similarity.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimilarityForSciPyDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = SimilarityForSciPyDto.class, name = REPOSITORY_SCIPY),
})
public abstract class SimilarityDto {
    String codebaseName;
    String strategyName;
    String name;
    String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
