package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	private String name;
	private String dendrogramName;
	private String cutValue;
	private float silhouetteScore;
	private List<Cluster> clusters;
	private Map<String,Float> controllersComplexity;
	private Map<String,Float> controllersComplexityRW;

	public Graph() {

	}

	public Graph(String name, String cutValue, float silhouetteScore, String dendrogramName) {
		this.name = name;
		this.cutValue = cutValue;
		this.silhouetteScore = silhouetteScore;
		this.dendrogramName = dendrogramName;
		this.clusters = new ArrayList<>();
		this.controllersComplexity = new HashMap<>();
		this.controllersComplexityRW = new HashMap<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDendrogramName() {
		return this.dendrogramName;
	}

	public void setDendrogramName(String dendrogramName) {
		this.dendrogramName = dendrogramName;
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

	public Map<String,Float> getControllersComplexityRW() {
		return this.controllersComplexityRW;
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
					mergedCluster.addEntity(entity.getName());
				clusters.remove(i);
				break;
			}
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {
				for (Entity entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity.getName());
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

	public void calculateMetrics(List<Controller> controllers, Map<String,List<Cluster>> controllerClusters) {
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = this.clusters.get(i);

			calculateClusterComplexity(cluster, controllers, controllerClusters);

			cluster.calculateCohesion(controllers);

			calculateClusterCoupling(cluster, i, controllers, controllerClusters);
		}

		this.controllersComplexity = new HashMap<>();
		this.controllersComplexityRW = new HashMap<>();
		for (int i = 0; i < controllers.size(); i++) {
			Controller controller = controllers.get(i);
			calculateControllerComplexity(controller);
		}
	}

	public void calculateClusterComplexity(Cluster cluster, List<Controller> controllers, Map<String,List<Cluster>> controllerClusters) {
		float complexity = 0;
		float complexityRW = 0;
		int totalControllers = 0;
		for (Controller controller : controllers) {
			for (Cluster c : controllerClusters.get(controller.getName())) {
				if (c.getName().equals(cluster.getName())) {
					if (controllerClusters.get(controller.getName()).size() > 1)
						totalControllers++;
					
					float complexityRWaux = 0;
					for (Cluster c2 : controllerClusters.get(controller.getName())) {
						if (!c2.getName().equals(cluster.getName())) {
							boolean write = false;
							for (Pair<Entity,String> pair : controller.getEntitiesRW()) {
								if (c2.containsEntity(pair.getFirst().getName())) {
									if (pair.getSecond().contains("W")) {
										write = true;
									}
								}
							}
							if (write)
								complexityRWaux += 1.0;
							else
								complexityRWaux += 0.5;
						}
					}
					
					complexityRW += complexityRWaux / (this.clusters.size() - 1);

					complexity += ((float) controllerClusters.get(controller.getName()).size() - 1) / (this.clusters.size() - 1);
					break;
				}
			}
		}
		complexity /= totalControllers;
		complexityRW /= totalControllers;
		cluster.setComplexity(complexity);
		cluster.setComplexityRW(complexityRW);
	}

	public void calculateClusterCoupling(Cluster cluster, int i, List<Controller> controllers, Map<String,List<Cluster>> controllerClusters) {
		float coupling = 0;
		for (int j = 0; j < clusters.size(); j++) {
			if (i != j) {
				Cluster c2 = this.clusters.get(j);
				int commonControllers = 0;
				int controllersC1 = 0;
				for (Controller c : controllers) {
					boolean touchedC1 = false;
					boolean touchedC2 = false;
					for (Entity entity : c.getEntities()) {
						if (cluster.containsEntity(entity.getName())) {
							touchedC1 = true;
						}
							
						if (c2.containsEntity(entity.getName()))
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
		cluster.setCoupling(coupling);
	}

	public void calculateControllerComplexity(Controller controller) {
		float complexity, complexityRW;
		float clusterAccessed = 0;
		float clusterAccessedRW = 0;
		for (Cluster cluster : this.clusters) {
			for (Entity entity : cluster.getEntities()) {
				if (controller.containsEntity(entity.getName())) {
					clusterAccessed++;

					boolean write = false;
					for (Pair<Entity,String> pair : controller.getEntitiesRW()) {
						if (cluster.containsEntity(pair.getFirst().getName())) {
							if (pair.getSecond().contains("W"))
								write = true;
						}
					}

					if (write)
						clusterAccessedRW += 1;
					else
						clusterAccessedRW += 0.5;
					break;
				}
			}
		}
		if (clusterAccessed == 1) {
			complexity = 0;
			complexityRW = 0;
		} else {
			complexity = clusterAccessed / this.clusters.size();
			complexityRW = clusterAccessedRW / this.clusters.size();
		}
		this.controllersComplexity.put(controller.getName(), complexity);
		this.controllersComplexityRW.put(controller.getName(), complexityRW);
	}
}