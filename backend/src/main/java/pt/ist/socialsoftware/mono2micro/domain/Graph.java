package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	private String name;
	private String cutValue;
	private float silhouetteScore;
	private List<Cluster> clusters;
	private Map<String,Float> controllersComplexity;

	public Graph() {

	}

	public Graph(String name, String cutValue, float silhouetteScore) {
		this.name = name;
		this.cutValue = cutValue;
		this.silhouetteScore = silhouetteScore;
		this.clusters = new ArrayList<>();
		this.controllersComplexity = new HashMap<>();
	}

	public void calculateMetrics(List<Controller> controllers) {
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c1 = this.clusters.get(i);
			c1.calculateComplexity(controllers);
			c1.calculateCohesion(controllers);
			
			float coupling = 0;
			for (int j = 0; j < clusters.size(); j++) {
				if (i != j) {
					Cluster c2 = this.clusters.get(j);
					int commonControllers = 0;
					int controllersC1 = 0;
					for (Controller c : controllers) {
						boolean touchedC1 = false;
						boolean touchedC2 = false;
						for (String entity : c.getEntities()) {
							if (c1.containsEntity(entity)) {
								touchedC1 = true;
							}
								
							if (c2.containsEntity(entity))
								touchedC2 = true;
						}
						if (touchedC1)
							controllersC1++;
						if (touchedC1 && touchedC2)
							commonControllers++;
					}
					coupling = coupling + ((float)commonControllers / controllersC1);
				}
			}
			coupling = coupling / (this.clusters.size() - 1);
			c1.setCoupling(coupling);
		}

		this.controllersComplexity = new HashMap<>();
		for (int i = 0; i < controllers.size(); i++) {
			Controller controller = controllers.get(i);
			float complexity = 0;
			for (Cluster cluster : this.clusters) {
				for (String entity : cluster.getEntities()) {
					if (controller.getEntities().contains(entity)) {
						complexity++;
						break;
					}
				}
			}
			complexity /= this.clusters.size();
			this.controllersComplexity.put(controller.getName(), complexity);
		}
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

	public Map<String,Float> getControllersComplexity() {
		return this.controllersComplexity;
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
				for (String entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);
				clusters.remove(i);
				break;
			}
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {
				for (String entity : clusters.get(i).getEntities())
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
		for (String entity : entities) {
			newCluster.addEntity(entity);
			currentCluster.removeEntity(entity);
		}
		this.addCluster(newCluster);
	}

	public void transferEntities(String fromCluster, String toCluster, String[] entities) {
		Cluster c1 = this.getCluster(fromCluster);
		Cluster c2 = this.getCluster(toCluster);
		for (String entity : entities) {
			c2.addEntity(entity);
			c1.removeEntity(entity);
		}
	}
}