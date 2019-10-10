package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
		float graphComplexity = 0;
		
		for (Controller controller : graph.getControllers()) {
			calculateControllerComplexity(controller);
			graphComplexity += controller.getComplexity();
		}
		graphComplexity /= graph.getControllers().size();
		graphComplexity = BigDecimal.valueOf(graphComplexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setComplexity(graphComplexity);
            
		for (Cluster cluster : graph.getClusters()) {
			calculateClusterComplexity(cluster, clusterControllers.get(cluster.getName()));

			calculateClusterCohesion(cluster, clusterControllers.get(cluster.getName()));

			calculateClusterCoupling(cluster);
		}
    }

    private void calculateControllerComplexity(Controller controller) {
		if (graph.getControllerClusters().get(controller.getName()).size() == 1) {
			controller.setComplexity(0);
			return;	
		}

        float complexity = 0;
        String lastCluster = "";
		Set<String> clusterReadCost = new HashSet<>();
		Set<String> clusterWriteCost = new HashSet<>();
        for (Pair<String,String> entityAccess : controller.getEntitiesSeq()) {
            String entity = entityAccess.getFirst();
            String mode = entityAccess.getSecond();
            String clusterAccessed = this.graph.getClusterWithEntity(entity).getName();

            if (!lastCluster.equals("") && !lastCluster.equals(clusterAccessed)) {
				complexity += clusterReadCost.size();
				complexity += clusterWriteCost.size();
				clusterReadCost.clear();
				clusterWriteCost.clear();
			}

			if (mode.equals("R"))
				costOfRead(controller, entity, clusterReadCost);
			else
				costOfWrite(controller, entity, clusterWriteCost);

            lastCluster = new String(clusterAccessed);
        }

		complexity += clusterReadCost.size();
		complexity += clusterWriteCost.size();

        controller.setComplexity(complexity);
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

	private void calculateClusterComplexity(Cluster cluster, List<Controller> controllers) {
		if (graph.getClusters().size() == 1) {
			cluster.setComplexity(0);
			return;	
		}

		float complexity = 0;
		for (Controller controller : controllers) {
			complexity += controller.getComplexity();
		}
		complexity /= controllers.size();
		complexity = BigDecimal.valueOf(complexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setComplexity(complexity);
	}

	public void calculateClusterCohesion(Cluster cluster, List<Controller> controllers) {
		float cohesion = 0;
		for (Controller controller : controllers) {
			float numberEntitiesTouched = 0;
			for (String controllerEntity : controller.getEntities().keySet()) {
				if (cluster.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}
			cohesion += numberEntitiesTouched / cluster.getEntities().size();
		}
		cohesion /= controllers.size();
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	private void calculateClusterCoupling(Cluster c1) {
		Map<String,Float> coupling = new HashMap<>();
		for (Cluster c2 : graph.getClusters()) {
			if (c1.getName().equals(c2.getName())) {
				coupling.put(c2.getName(), Float.valueOf(-1));
				continue;
			}
			List<String> touchedEntities = new ArrayList<>();
			for (Controller controller : this.graph.getControllers()) {
				for (int i = 0; i < controller.getEntitiesSeq().size() - 1; i++) {
					String touchedEntity = controller.getEntitiesSeq().get(i).getFirst();
					String nextTouchedEntity = controller.getEntitiesSeq().get(i+1).getFirst();

					Cluster touchedEntityCluster = graph.getClusterWithEntity(touchedEntity);
					Cluster nextTouchedEntityCluster = graph.getClusterWithEntity(nextTouchedEntity);

					if (c1.getName().equals(touchedEntityCluster.getName()) &&
						c2.getName().equals(nextTouchedEntityCluster.getName()) &&
						!touchedEntities.contains(nextTouchedEntity)) {
							touchedEntities.add(nextTouchedEntity);
						}
				}
			}

			coupling.put(c2.getName(), Float.valueOf(touchedEntities.size()));
		}
		c1.setCoupling(coupling);
	}
}