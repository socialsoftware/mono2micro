package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.socialsoftware.mono2micro.utils.Pair;

public class Graph {
	private String name;
	private String dendrogramName;
	private String cutValue;
	private float silhouetteScore;
	private List<Cluster> clusters;
	private Map<String,Float> controllersComplexity;
	private Map<String,Float> controllersComplexityRW;
	private Map<String,Float> controllersComplexitySeq;

	public Graph() {

	}

	public Graph(String name, String cutValue, float silhouetteScore, String dendrogramName) {
		this.name = name;
		this.cutValue = cutValue;
		this.silhouetteScore = silhouetteScore;
		this.dendrogramName = dendrogramName;
		this.clusters = new ArrayList<>();
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

	public Map<String,Float> getControllersComplexitySeq() {
		return this.controllersComplexitySeq;
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

	public void calculateMetrics(Map<String,List<Controller>> clusterControllers, Map<String,List<Pair<Cluster,String>>> controllerClustersSeq) {
		float maximumSeqLength = 0;
		float totalSequenceLength = 0;
		for (String ctr : controllerClustersSeq.keySet()) {
			totalSequenceLength += controllerClustersSeq.get(ctr).size() - 1;
			if (controllerClustersSeq.get(ctr).size() > maximumSeqLength)
				maximumSeqLength = controllerClustersSeq.get(ctr).size();
		}
		
		this.controllersComplexity = new HashMap<>();
		this.controllersComplexityRW = new HashMap<>();
		this.controllersComplexitySeq = new HashMap<>();
		for (String controllerName : controllerClustersSeq.keySet())
			calculateControllerComplexity(controllerName, controllerClustersSeq, maximumSeqLength);
		
		for (Cluster cluster : this.clusters) {
			calculateClusterComplexity(cluster, clusterControllers);

			cluster.calculateCohesion(clusterControllers);

			calculateClusterCoupling(cluster, clusterControllers, controllerClustersSeq, totalSequenceLength);
		}
	}

	public void calculateControllerComplexity(String controllerName, Map<String, List<Pair<Cluster, String>>> controllerClustersSeq, float maximumSeqLength) {
		float complexity, complexityRW, complexitySeq;
		Map<String,String> clusterAccessed = new HashMap<>();
		
		for(Pair<Cluster,String> clusterPair : controllerClustersSeq.get(controllerName)) {
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
			complexitySeq = controllerClustersSeq.get(controllerName).size() / maximumSeqLength;
		}
		this.controllersComplexity.put(controllerName, complexity);
		this.controllersComplexityRW.put(controllerName, complexityRW);
		this.controllersComplexitySeq.put(controllerName, complexitySeq);
	}

	public void calculateClusterComplexity(Cluster cluster, Map<String,List<Controller>> clusterControllers) {
		float complexity = 0, complexityRW = 0, complexitySeq = 0;
		if (this.clusters.size() > 1) {
			for (Controller ctr : clusterControllers.get(cluster.getName())) {
				complexity += this.controllersComplexity.get(ctr.getName());
				complexityRW += this.controllersComplexityRW.get(ctr.getName());
				complexitySeq += this.controllersComplexitySeq.get(ctr.getName());
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