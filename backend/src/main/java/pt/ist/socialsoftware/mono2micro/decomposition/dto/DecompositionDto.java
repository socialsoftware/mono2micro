package pt.ist.socialsoftware.mono2micro.decomposition.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "strategyType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSciPyDecompositionDto.class, name = AccessesSciPyStrategy.ACCESSES_SCIPY)
})
public abstract class DecompositionDto {
    private String name;
    private String strategyName;
    private String strategyType;
    private List<Metric> metrics;

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

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
}
