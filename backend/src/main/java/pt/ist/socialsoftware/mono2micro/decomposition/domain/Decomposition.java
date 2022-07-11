package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;

@Document("decomposition")
public abstract class Decomposition {
	@Id
	private String name;
	private Strategy strategy;
	private List<Metric> metrics = new ArrayList<>();


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