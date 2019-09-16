package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

import pt.ist.socialsoftware.mono2micro.utils.Pair;

public class Graph {
	private String codebaseName;
	private String dendrogramName;
	private String name;
	private boolean expert;
	private float cutValue;
	private String cutType;
	private float silhouetteScore;
	private List<Controller> controllers = new ArrayList<>();
	private List<Cluster> clusters = new ArrayList<>();

	public Graph() {
	}

	public String getCodebaseName() {
		return this.codebaseName;
	}

	public void setCodebaseName(String codebaseName) {
		this.codebaseName = codebaseName;
	}

	public String getDendrogramName() {
		return this.dendrogramName;
	}

	public void setDendrogramName(String dendrogramName) {
		this.dendrogramName = dendrogramName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isExpert() {
		return expert;
	}

	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	public float getCutValue() {
		return this.cutValue;
	}

	public void setCutValue(float cutValue) {
		this.cutValue = cutValue;
	}

	public String getCutType() {
		return cutType;
	}

	public void setCutType(String cutType) {
		this.cutType = cutType;
	}

	public float getSilhouetteScore() {
		return this.silhouetteScore;
	}

	public void setSilhouetteScore(float silhouetteScore) {
		this.silhouetteScore = silhouetteScore;
	}

	public List<Controller> getControllers() {
		return this.controllers;
	}

	public void addController(Controller controller) {
		this.controllers.add(controller);
	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void addCluster(Cluster cluster) {
		this.clusters.add(cluster);
	}

	public void deleteCluster(String clusterName) {
		for (int i = 0; i < this.clusters.size(); i++) {
			if (this.clusters.get(i).getName().equals(clusterName)) {
				this.clusters.remove(i);
				break;
			}
		}
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
		this.calculateMetrics();
	}

	public void renameCluster(String clusterName, String newName) {
		if (this.getClustersNames().contains(newName)) {
			throw new KeyAlreadyExistsException();
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(clusterName)) {
				clusters.get(i).setName(newName);
				break;
			}
		}
		this.calculateMetrics();
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

	public Cluster getClusterWithEntity(String entityName) {
		for (Cluster cluster : this.clusters) {
			if (cluster.containsEntity(entityName))
				return cluster;
		}
		return null;
	}

	public void splitCluster(String clusterName, String newName, String[] entities) {
		Cluster currentCluster = this.getCluster(clusterName);
		Cluster newCluster = new Cluster(newName);
		for (String entity : entities) {
			newCluster.addEntity(currentCluster.getEntity(entity));
			currentCluster.removeEntity(entity);
		}
		this.addCluster(newCluster);
		this.calculateMetrics();
	}

	public void transferEntities(String fromCluster, String toCluster, String[] entities) {
		Cluster c1 = this.getCluster(fromCluster);
		Cluster c2 = this.getCluster(toCluster);
		for (String entity : entities) {
			c2.addEntity(c1.getEntity(entity));
			c1.removeEntity(entity);
		}
		this.calculateMetrics();
	}

	public void moveEntities(String[] entities, String targetCluster) {
		List<String> removedEntities = new ArrayList<>();
        for (Cluster cluster : this.clusters) {
			for (String entity : entities) {
				if (cluster.containsEntity(entity) && !cluster.getName().equals(targetCluster)) {
					cluster.removeEntity(entity);
					removedEntities.add(entity);
				}
			}
		}
		for (String entity : removedEntities)
			this.getCluster(targetCluster).addEntity(new Entity(entity));
	}

	public Map<String,List<Controller>> getClusterControllers() {
		Map<String,List<Controller>> clusterControllers = new HashMap<>();

		for (Cluster cluster : this.clusters) {
			List<Controller> touchedControllers = new ArrayList<>();
			for (Controller controller : this.controllers) {
				for (String controllerEntity : controller.getEntities().keySet()) {
					if (cluster.containsEntity(controllerEntity)) {
						touchedControllers.add(controller);
						break;
					}
				}
			}
			clusterControllers.put(cluster.getName(), touchedControllers);
		}
		return clusterControllers;
	}

	public Map<String,List<Cluster>> getControllerClusters() {
		Map<String,List<Cluster>> controllerClusters = new HashMap<>();

		for (Controller controller : this.controllers) {
			List<Cluster> touchedClusters = new ArrayList<>();
			for (Cluster cluster : this.clusters) {
				for (Entity clusterEntity : cluster.getEntities()) {
					if (controller.containsEntity(clusterEntity.getName())) {
						touchedClusters.add(cluster);
						break;
					}
				}
			}
			controllerClusters.put(controller.getName(), touchedClusters);
		}
		return controllerClusters;
	}

	public Map<String,List<Pair<Cluster,String>>> getControllerClustersSeq() {
		Map<String,List<Pair<Cluster,String>>> controllerClustersSeq = new HashMap<>();

		for (Controller controller : this.controllers) {
			List<Pair<Cluster,String>> touchedClusters = new ArrayList<>();
			Pair<Cluster,String> lastCluster = null;
			for (Pair<String,String> entityPair : controller.getEntitiesSeq()) {
				String entityName = entityPair.getFirst();
				String mode = entityPair.getSecond();
				Cluster clusterAccessed = getClusterWithEntity(entityName);
				if (lastCluster == null){
					lastCluster = new Pair<Cluster,String>(clusterAccessed, mode);
					touchedClusters.add(new Pair<Cluster,String>(clusterAccessed, mode));
				} else if (lastCluster.getFirst().getName().equals(clusterAccessed.getName())) {
					if (!lastCluster.getSecond().contains(mode)) {
						lastCluster.setSecond("RW");
						touchedClusters.get(touchedClusters.size() - 1).setSecond("RW");
					}
				} else {
					lastCluster = new Pair<Cluster,String>(clusterAccessed, mode);
					touchedClusters.add(new Pair<Cluster,String>(clusterAccessed, mode));
				}
			}
			controllerClustersSeq.put(controller.getName(), touchedClusters);
		}
		return controllerClustersSeq;
	}

	public void calculateMetrics() {
		Map<String,List<Controller>> clusterControllers = this.getClusterControllers();
		Map<String,List<Pair<Cluster,String>>> controllerClustersSeq = this.getControllerClustersSeq();
		float maximumSeqLength = 0;
		float totalSequenceLength = 0;
		for (String ctr : controllerClustersSeq.keySet()) {
			totalSequenceLength += controllerClustersSeq.get(ctr).size() - 1;
			if (controllerClustersSeq.get(ctr).size() > maximumSeqLength)
				maximumSeqLength = controllerClustersSeq.get(ctr).size();
		}
		
		for (Controller controller : this.controllers)
			calculateControllerComplexity(controller, controllerClustersSeq, maximumSeqLength);
		
		for (Cluster cluster : this.clusters) {
			calculateClusterComplexity(cluster, clusterControllers);

			cluster.calculateCohesion(clusterControllers.get(cluster.getName()));

			calculateClusterCoupling(cluster, clusterControllers, controllerClustersSeq, totalSequenceLength);
		}
	}

	public void calculateControllerComplexity(Controller controller, Map<String, List<Pair<Cluster, String>>> controllerClustersSeq, float maximumSeqLength) {
		float complexity, complexityRW, complexitySeq;
		Map<String,String> clusterAccessed = new HashMap<>();
		
		for(Pair<Cluster,String> clusterPair : controllerClustersSeq.get(controller.getName())) {
			String clusterName = clusterPair.getFirst().getName();
			String mode = clusterPair.getSecond();
			if (clusterAccessed.keySet().contains(clusterName)) {
				if (!clusterAccessed.get(clusterName).contains(mode))
					clusterAccessed.put(clusterName,"RW");
			} else {
				clusterAccessed.put(clusterName, mode);
			}
		}

		float clusterAccessedAmount = clusterAccessed.keySet().size();
		float clusterAccessedRW = 0;
		for (String clusterName : clusterAccessed.keySet()) {
			if (clusterAccessed.get(clusterName).contains("W"))
				clusterAccessedRW += 1;
			else
				clusterAccessedRW += 0.5;
		}

		if (clusterAccessedAmount == 1) {
			complexity = 0;
			complexityRW = 0;
			complexitySeq = 0;
		} else {
			complexity = clusterAccessedAmount / this.clusters.size();
			complexityRW = clusterAccessedRW / this.clusters.size();
			complexitySeq = controllerClustersSeq.get(controller.getName()).size() / maximumSeqLength;
		}
		controller.setComplexity(complexity);
		controller.setComplexityRW(complexityRW);
		controller.setComplexitySeq(complexitySeq);
	}

	public void calculateClusterComplexity(Cluster cluster, Map<String,List<Controller>> clusterControllers) {
		float complexity = 0, complexityRW = 0, complexitySeq = 0;
		if (this.clusters.size() > 1) {
			for (Controller ctr : clusterControllers.get(cluster.getName())) {
				complexity += ctr.getComplexity();
				complexityRW += ctr.getComplexityRW();
				complexitySeq += ctr.getComplexitySeq();
			}
			complexity /= clusterControllers.get(cluster.getName()).size();
			complexityRW /= clusterControllers.get(cluster.getName()).size();
			complexitySeq /= clusterControllers.get(cluster.getName()).size();
		}
		cluster.setComplexity(complexity);
		cluster.setComplexityRW(complexityRW);
		cluster.setComplexitySeq(complexitySeq);
	}

	public void calculateClusterCoupling(Cluster c1, Map<String,List<Controller>> clusterControllers, Map<String,List<Pair<Cluster,String>>> controllerClustersSeq, float totalSequenceLength) {
		Map<String,Float> coupling = new HashMap<>();
		Map<String,Float> couplingRW = new HashMap<>();
		Map<String,Float> couplingSeq = new HashMap<>();
		for (Cluster c2 : this.clusters) {
			if (c1.getName().equals(c2.getName())) {
				coupling.put(c2.getName(), new Float(1));
				couplingRW.put(c2.getName(), new Float(1));
				couplingSeq.put(c2.getName(), new Float(1)); 
				continue;
			}
			List<Controller> cluster1Controllers = clusterControllers.get(c1.getName());
			List<Controller> cluster2Controllers = clusterControllers.get(c2.getName());
			float commonControllers = 0;
			float commonControllersW = 0;
			float c1c2Count = 0;
			float c2c1Count = 0;
			for (Controller ctr1 : cluster1Controllers) {
				for (Controller ctr2 : cluster2Controllers) {
					if (ctr1.getName().equals(ctr2.getName())) {
						commonControllers++;
						boolean writeC1 = false;
						boolean writeC2 = false;
						for (int i = 0; i < controllerClustersSeq.get(ctr1.getName()).size(); i++) {
							Pair<Cluster,String> clusterPair = controllerClustersSeq.get(ctr1.getName()).get(i);
							String clusterName = clusterPair.getFirst().getName();
							String mode = clusterPair.getSecond();

							if (clusterName.equals(c1.getName()) && mode.equals("W"))
								writeC1 = true;
							if (clusterName.equals(c2.getName()) && mode.equals("W"))
								writeC2 = true;

							if (i < controllerClustersSeq.get(ctr1.getName()).size() - 1) {
								Pair<Cluster,String> nextClusterPair = controllerClustersSeq.get(ctr1.getName()).get(i+1);
								String nextClusterName = nextClusterPair.getFirst().getName();
								if (clusterName.equals(c1.getName()) && nextClusterName.equals(c2.getName()))
									c1c2Count++;
								if (clusterName.equals(c2.getName()) && nextClusterName.equals(c1.getName()))
									c2c1Count++;
							}
						}

						if (writeC1 && writeC2)
							commonControllersW++;
						break;
					}
				}
			}
			coupling.put(c2.getName(), commonControllers / cluster1Controllers.size());
			couplingRW.put(c2.getName(), commonControllersW / cluster1Controllers.size());
			couplingSeq.put(c2.getName(), totalSequenceLength == 0 ? 0 : (c1c2Count + c2c1Count) / totalSequenceLength);
		}
		c1.setCoupling(coupling);
		c1.setCouplingRW(couplingRW);
		c1.setCouplingSeq(couplingSeq);
	}
}