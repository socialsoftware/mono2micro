package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Dendrogram {
	private String codebaseName;
	private String name;
	private List<Graph> graphs = new ArrayList<>();
	private String linkageType;
	private float accessMetricWeight;
	private float writeMetricWeight;
	private float readMetricWeight;
	private float sequenceMetric1Weight;
	private float sequenceMetric2Weight;
	private List<String> profiles = new ArrayList<>();

	public Dendrogram() {
	}

	public float getSequenceMetric2Weight() {
		return sequenceMetric2Weight;
	}

	public void setSequenceMetric2Weight(float sequenceMetric2Weight) {
		this.sequenceMetric2Weight = sequenceMetric2Weight;
	}

	public float getSequenceMetric1Weight() {
		return sequenceMetric1Weight;
	}

	public void setSequenceMetric1Weight(float sequenceMetric1Weight) {
		this.sequenceMetric1Weight = sequenceMetric1Weight;
	}

	public float getReadMetricWeight() {
		return readMetricWeight;
	}

	public void setReadMetricWeight(float readMetricWeight) {
		this.readMetricWeight = readMetricWeight;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLinkageType() {
		return this.linkageType;
	}

	public void setLinkageType(String linkageType) {
		this.linkageType = linkageType;
	}

	public float getAccessMetricWeight() {
		return this.accessMetricWeight;
	}

	public float getWriteMetricWeight() {
		return this.writeMetricWeight;
	}

	public void setAccessMetricWeight(float accessMetricWeight) {
		this.accessMetricWeight = accessMetricWeight;
	}

	public void setWriteMetricWeight(float writeMetricWeigh) {
		this.writeMetricWeight = writeMetricWeigh;
	}

	public String getCodebaseName() {
		return this.codebaseName;
	}

	public void setCodebaseName(String codebaseName) {
		this.codebaseName = codebaseName;
	}

	public List<String> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<String> profiles) {
		this.profiles = profiles;
	}


	public List<Graph> getGraphs() {
		return this.graphs;
	}

	public boolean deleteGraph(String graphName) {
		for (int i = 0; i < this.graphs.size(); i++) {
			if (this.graphs.get(i).getName().equals(graphName)) {
				this.graphs.remove(i);
				return true;
			}
		}
		return false;
	}

	public Graph getGraph(String graphName) {
		for (Graph graph : this.graphs) {
			if (graph.getName().equals(graphName))
				return graph;
		}
		return null;
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
		graph.calculateMetrics();
	}

	public List<String> getGraphNames() {
		List<String> graphNames = new ArrayList<>();
		for (Graph graph : this.graphs)
			graphNames.add(graph.getName());
		return graphNames;
	}
}
