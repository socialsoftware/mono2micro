package pt.ist.socialsoftware.mono2micro.domain.decomposition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.domain.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "strategyType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AccessesSciPyDecomposition.class, name = Strategy.StrategyType.ACCESSES_SCIPY)
})
public abstract class Decomposition {
	private String name;
	private String codebaseName;
	private String strategyName;
	private List<Metric> metrics = new ArrayList<>();


	@JsonIgnore
	public abstract String getStrategyType();

	public String getCodebaseName() { return this.codebaseName; }

	public void setCodebaseName(String codebaseName) { this.codebaseName = codebaseName; }

	public String getStrategyName() { return this.strategyName; }

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getName() { return this.name; }

	public void setName(String name) {
		this.name = name;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(Metric metric) {
		this.metrics.add(metric);
	}
}