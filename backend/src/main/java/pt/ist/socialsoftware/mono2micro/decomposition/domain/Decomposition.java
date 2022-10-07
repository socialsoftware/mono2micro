package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Decomposition {
	@Id
	String name;
	Map<String, Object> metrics = new HashMap<>(); // Map<Metric type, Metric value>
	Map<String, Cluster> clusters = new HashMap<>();
	@DBRef(lazy = true)
	Strategy strategy;

	public abstract String getStrategyType();

	public abstract List<String> getImplementations();
	public boolean containsImplementation(String implementation) {
		return getImplementations().contains(implementation);
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

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Map<String, Cluster> getClusters() { return this.clusters; }

	public void setClusters(Map<String, Cluster> clusters) { this.clusters = clusters; }

	public void addCluster(Cluster cluster) {
		Cluster c = this.clusters.putIfAbsent(cluster.getName(), cluster);

		if (c != null) throw new Error("Cluster with name: " + cluster.getName() + " already exists");
	}

	public Cluster removeCluster(String clusterName) {
		Cluster c = this.clusters.remove(clusterName);

		if (c == null) throw new Error("Cluster with name: " + clusterName + " not found");

		return c;
	}

	public Cluster getCluster(String clusterName) {
		Cluster c = this.clusters.get(clusterName);

		if (c == null) throw new Error("Cluster with name: " + clusterName + " not found");

		return c;
	}

	public boolean clusterNameExists(String clusterName) {
		for (String cluster : this.clusters.keySet())
			if (cluster.equals(clusterName))
				return true;
		return false;
	}

	public int maxClusterSize() {
		int max = 0;

		for (Cluster cluster : this.clusters.values()) {
			if (cluster.getElements().size() > max)
				max = cluster.getElements().size();
		}

		return max;
	}
}