package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	private String name;
	private String cutValue;
	private float silhouetteScore;
	private List<Cluster> clusters;

	public Graph() {

	}

	public Graph(String name, String cutValue, float silhouetteScore) {
		this.name = name;
		this.cutValue = cutValue;
		this.silhouetteScore = silhouetteScore;
		this.clusters = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCutValue() {
		return this.cutValue;
	}

	public void setCutValue(String cutValue) {
		this.cutValue = cutValue;
	}

	public float getSilhouetteScore() {
		return this.silhouetteScore;
	}

	public void setSilhouetteScore(float silhouetteScore) {
		this.silhouetteScore = silhouetteScore;
	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void addCluster(Cluster cluster) {
		this.clusters.add(cluster);
	}

	public void mergeClusters(String cluster1, String cluster2, String newName) {
		Cluster mergedCluster = new Cluster(newName);
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster1)) {
				for (Entity entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);
				clusters.remove(i);
				break;
			}
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {
				for (Entity entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);
				clusters.remove(i);
				break;
			}
		}
		this.addCluster(mergedCluster);
	}

	public boolean renameCluster(String clusterName, String newName) {
		if (this.getClustersNames().contains(newName))
			return false;
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(clusterName)) {
				clusters.get(i).setName(newName);
			}
		}
		return true;
	}

	public List<String> getClustersNames() {
		List<String> clustersNames = new ArrayList<>();
		for (Cluster cluster : this.clusters)
			clustersNames.add(cluster.getName());
		return clustersNames;
	}

	public Cluster getCluster(String clusterName) {
		for (Cluster cluster : this.clusters)
			if (cluster.getName().equals(clusterName))
				return cluster;
		return null;
	}

	public void splitCluster(String clusterName, String newName, String[] entities) {
		Cluster currentCluster = this.getCluster(clusterName);
		Cluster newCluster = new Cluster(newName);
		for (String entityName : entities) {
			newCluster.addEntity(currentCluster.getEntity(entityName));
			currentCluster.removeEntity(entityName);
		}
		this.addCluster(newCluster);
	}

	public void transferEntities(String fromCluster, String toCluster, String[] entities) {
		Cluster c1 = this.getCluster(fromCluster);
		Cluster c2 = this.getCluster(toCluster);
		for (String entityName : entities) {
			c2.addEntity(c1.getEntity(entityName));
			c1.removeEntity(entityName);
		}
	}
}