package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	private String name;
	private List<Cluster> clusters;

	public Graph() {

	}

	public Graph(String name) {
		this.name = name;
		this.clusters = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void addCluster(Cluster cluster) {
		this.clusters.add(cluster);
	}

}
