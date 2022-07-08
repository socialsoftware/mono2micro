package pt.ist.socialsoftware.mono2micro.strategy.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy.RECOMMENDATION_ACCESSES_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSciPyStrategyDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = RecommendAccessesSciPyStrategyDto.class, name = RECOMMENDATION_ACCESSES_SCIPY)
})
public abstract class StrategyDto {
    private String name;
    private String codebaseName;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
