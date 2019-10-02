package pt.ist.socialsoftware.mono2micro.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

public class Metrics {
    private Graph graph;

    public Metrics(Graph graph) {
        this.graph = graph;
    }

    public void calculateMetrics() {
        Map<String,List<Controller>> clusterControllers = graph.getClusterControllers();
		Map<String,List<Pair<Cluster,String>>> controllerClustersSeq = graph.getControllerClustersSeq();
		float maximumSeqLength = 0;
		float totalSequenceLength = 0;
		for (String ctr : controllerClustersSeq.keySet()) {
			totalSequenceLength += controllerClustersSeq.get(ctr).size() - 1;
			if (controllerClustersSeq.get(ctr).size() > maximumSeqLength)
				maximumSeqLength = controllerClustersSeq.get(ctr).size();
		}
		
		for (Controller controller : graph.getControllers()) {
			calculateControllerComplexity(controller, controllerClustersSeq, maximumSeqLength);
            calculateControllerComplexity2(controller);
        }
            
		for (Cluster cluster : graph.getClusters()) {
			calculateClusterComplexity(cluster, clusterControllers);

			cluster.calculateCohesion(clusterControllers.get(cluster.getName()));

			calculateClusterCoupling(cluster, clusterControllers, controllerClustersSeq, totalSequenceLength);
		}
    }

    private void calculateControllerComplexity2(Controller controller) {
		if (graph.getControllerClusters().get(controller.getName()).size() == 1) {
			controller.setComplexity2(0);
			return;	
		}

        float complexity2 = 0;
        String lastCluster = "";
		Set<String> clusterReadCost = new HashSet<>();
		Set<String> clusterWriteCost = new HashSet<>();
        for (Pair<String,String> entityAccess : controller.getEntitiesSeq()) {
            String entity = entityAccess.getFirst();
            String mode = entityAccess.getSecond();
            String clusterAccessed = this.graph.getClusterWithEntity(entity).getName();

            if (!lastCluster.equals("") && !lastCluster.equals(clusterAccessed)) {                
				complexity2 += clusterReadCost.size();
				complexity2 += clusterWriteCost.size();
				clusterReadCost.clear();
				clusterWriteCost.clear();
			}

			if (mode.equals("R"))
				costOfRead(controller, entity, clusterReadCost);
			else
				costOfWrite(controller, entity, clusterWriteCost);

            lastCluster = new String(clusterAccessed);
        }

		complexity2 += clusterReadCost.size();
		complexity2 += clusterWriteCost.size();

        controller.setComplexity2(complexity2);
    }

    private float getMaximumSequenceLength() {
        Map<String,List<Pair<Cluster,String>>> controllerClustersSeq = this.graph.getControllerClustersSeq();
        
        float maximumSequenceLength = 0;
        for (String controller : controllerClustersSeq.keySet()) {
			if (controllerClustersSeq.get(controller).size() > maximumSequenceLength)
				maximumSequenceLength = controllerClustersSeq.get(controller).size();
        }
        return maximumSequenceLength;
    }

    private void costOfRead(Controller controller, String entity, Set<String> clusterReadCost) {
        for (Controller otherController : this.graph.getControllers()) {
            if (!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) && 
                otherController.getEntities().get(entity).contains("W")) {
                    clusterReadCost.add(otherController.getName());
                }
        }
    }

    private void costOfWrite(Controller controller, String entity, Set<String> clusterWriteCost) {
        for (Controller otherController : this.graph.getControllers()) {
            if (!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) && 
                otherController.getEntities().get(entity).contains("R")) {
                    clusterWriteCost.add(otherController.getName());
                }
        }
    }

    private void calculateControllerComplexity(Controller controller,
        Map<String, List<Pair<Cluster, String>>> controllerClustersSeq, float maximumSeqLength) {
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
			complexity = clusterAccessedAmount / graph.getClusters().size();
			complexityRW = clusterAccessedRW / graph.getClusters().size();
			complexitySeq = controllerClustersSeq.get(controller.getName()).size() / maximumSeqLength;
		}
		controller.setComplexity(complexity);
		controller.setComplexityRW(complexityRW);
		controller.setComplexitySeq(complexitySeq);
	}

	private void calculateClusterComplexity(Cluster cluster, Map<String,List<Controller>> clusterControllers) {
		float complexity = 0, complexityRW = 0, complexitySeq = 0;
		if (graph.getClusters().size() > 1) {
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

	private void calculateClusterCoupling(Cluster c1, Map<String,List<Controller>> clusterControllers, Map<String,List<Pair<Cluster,String>>> controllerClustersSeq, float totalSequenceLength) {
		Map<String,Float> coupling = new HashMap<>();
		Map<String,Float> couplingRW = new HashMap<>();
		Map<String,Float> couplingSeq = new HashMap<>();
		for (Cluster c2 : graph.getClusters()) {
			if (c1.getName().equals(c2.getName())) {
				coupling.put(c2.getName(), Float.valueOf(1));
				couplingRW.put(c2.getName(), Float.valueOf(1));
				couplingSeq.put(c2.getName(), Float.valueOf(1)); 
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