package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.history.domain.History;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.*;

public abstract class Decomposition {
	@Id
	String name;
	String type;
	boolean expert;
	boolean outdated; // Used to avoid long waiting times during interaction
	Map<String, Object> metrics = new HashMap<>(); // Map<MetricCalculator type, MetricCalculator value>
	Map<String, Cluster> clusters = new HashMap<>();
	@DBRef(lazy = true)
	Strategy strategy;
	@DBRef
	Similarity similarity;
	@DBRef
    History history;

	List<RepresentationInformation> representationInformations = new ArrayList<>();

	public Decomposition() {}
	public Decomposition(Similarity similarity) {
		this.similarity = similarity;
		similarity.addDecomposition(this);
		this.strategy = similarity.getStrategy();
		strategy.addDecomposition(this);
	}

	public abstract void setup() throws Exception;
	public abstract void update() throws Exception;
	public abstract void deleteProperties();
	public abstract void calculateMetrics();
	public abstract void renameCluster(RenameOperation operation);
	public abstract void mergeClusters(MergeOperation operation);
	public abstract void splitCluster(SplitOperation operation);
	public abstract void transferEntities(TransferOperation operation);
	public abstract void formCluster(FormClusterOperation operation);
	public abstract Decomposition snapshotDecomposition(String decompositionName) throws Exception;

	public String getName() { return this.name; }

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isExpert() {
		return expert;
	}

	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	public boolean isOutdated() {
		return outdated;
	}

	public void setOutdated(boolean outdated) {
		this.outdated = outdated;
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

	public List<RepresentationInformation> getRepresentationInformations() {
		return representationInformations;
	}

	public void setRepresentationInformations(List<RepresentationInformation> representationInformations) {
		this.representationInformations = representationInformations;
	}

	public void addRepresentationInformation(RepresentationInformation representationInformation) {
		this.representationInformations.add(representationInformation);
	}

	public RepresentationInformation getRepresentationInformationByType(String type) {
		return this.representationInformations.stream().filter(r -> r.getType().equals(type)).findFirst().orElse(null);
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Similarity getSimilarity() {
		return similarity;
	}

	public void setSimilarity(Similarity similarity) {
		this.similarity = similarity;
	}

	public History getHistory() {
		return history;
	}

	public void setHistory(History history) {
		this.history = history;
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

	public Map<Short, String> getEntityIDToClusterName() {
		Map<Short, String> entityIDToClusterName = new HashMap<>();
		for (Cluster cluster : this.clusters.values())
			for (Element element : cluster.getElements())
				entityIDToClusterName.put(element.getId(), cluster.getName());
		return entityIDToClusterName;
	}

	public String getEdgeWeights(String representationInfo) throws Exception {
		RepresentationInformation representationInformation = getRepresentationInformationByType(representationInfo);
		return representationInformation.getEdgeWeights(this);
	}

	public String getSearchItems(String representationInfo) throws Exception {
		RepresentationInformation representationInformation = getRepresentationInformationByType(representationInfo);
		return representationInformation.getSearchItems(this);
	}
}