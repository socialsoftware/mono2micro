package pt.ist.socialsoftware.mono2micro.domain.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSciPyStrategy.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = RecommendAccessesSciPyStrategy.class, name = RECOMMENDATION_ACCESSES_SCIPY)
})
public abstract class Strategy {
    private String name;
    private String codebaseName;
    private List<String> decompositionsNames;

    // Type differs from Name since the name possesses an extra ID to distinguish strategies
    @JsonIgnore
    public abstract String getType();

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

    public List<String> getDecompositionsNames() {
        return decompositionsNames;
    }

    public void setDecompositionsNames(List<String> decompositionsNames) {
        this.decompositionsNames = decompositionsNames;
    }

    public synchronized void addDecompositionName(String decompositionName) {
        this.decompositionsNames.add(decompositionName);
    }

    public synchronized void removeDecompositionName(String decompositionName) {
        this.decompositionsNames.remove(decompositionName);
    }

    @Override
    public abstract boolean equals(Object obj);

    public static class StrategyType {
        public static final String ACCESSES_SCIPY = "ACCESSES_SCIPY";
        public static final String RECOMMENDATION_ACCESSES_SCIPY = "RECOMMENDATION_ACCESSES_SCIPY";
    }
}