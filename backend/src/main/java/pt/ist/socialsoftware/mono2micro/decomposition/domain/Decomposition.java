package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.Map;

public abstract class Decomposition {
	@Id
	String name;
	@DBRef
	Strategy strategy;
	Map<String, Object> metrics; // Map<Metric type, Metric value>


	public abstract String getStrategyType();

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public String getName() { return this.name; }

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Object> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(String metricType, Object metricValue) {
		this.metrics.put(metricType, metricValue);
	}
}