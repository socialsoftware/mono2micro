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

	public void mergeClusters(String cluster1, String cluster2) {
		Cluster mergedCluster = new Cluster(cluster1 + "+" + cluster2);
		this.addCluster(mergedCluster);
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster1)) {
				for (String entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);
				clusters.remove(i);
			}
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {
				for (String entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);
				clusters.remove(i);
			}
		}
	}

	public void renameCluster(String clusterName, String newName) {
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(clusterName)) {
				clusters.get(i).setName(newName);
			}
		}
	}
}
