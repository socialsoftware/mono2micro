package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;


import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

public class Metrics {
    private Graph graph;
	private Map<String,List<Cluster>> controllerClusters;
	private Map<String,List<Controller>> clusterControllers;

    public Metrics(Graph graph) {
        this.graph = graph;
		this.controllerClusters = graph.getControllerClusters();
		this.clusterControllers = graph.getClusterControllers();
    }

    public void calculateMetrics() {
		float graphComplexity = 0;
		float graphCohesion = 0;
		float graphCoupling = 0;

		for (Cluster cluster : graph.getClusters()) {
			cluster.setCouplingDependencies(new HashMap<>());
		}
		
		for (Controller controller : graph.getControllers()) {
			calculateControllerComplexity(controller);
			graphComplexity += controller.getComplexity();
			calculateClusterDependencies(controller);
		}

		graphComplexity /= graph.getControllers().size();
		graphComplexity = BigDecimal.valueOf(graphComplexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setComplexity(graphComplexity);

		for (Cluster cluster : graph.getClusters()) {
			calculateClusterComplexity(cluster);

			calculateClusterCohesion(cluster);
			graphCohesion += cluster.getCohesion();

			calculateClusterCoupling(cluster);
			graphCoupling += cluster.getCoupling();
		}

		graphCohesion /= graph.getClusters().size();
		graphCohesion = BigDecimal.valueOf(graphCohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCohesion(graphCohesion);
		graphCoupling /= graph.getClusters().size();
		graphCoupling = BigDecimal.valueOf(graphCoupling).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCoupling(graphCoupling);
    }

	private void calculateClusterDependencies(Controller controller) {
		Set<Controller.LocalTransaction> allLocalTransactions = controller.getAllLocalTransactions();

		for (Controller.LocalTransaction lt : allLocalTransactions) {
			Cluster fromCluster = graph.getCluster(lt.getClusterName());

			if (fromCluster == null) // root node
				continue;

			List<Controller.LocalTransaction> nextLocalTransactions = controller.getNextLocalTransactions(lt);

			for (Controller.LocalTransaction nextLt : nextLocalTransactions) {
				String toEntity = nextLt.getClusterAccesses().get(0).getEntity();
				fromCluster.addCouplingDependency(nextLt.getClusterName(), toEntity);
			}
		}
	}

	private void calculateControllerComplexity(Controller controller) {
    	if (this.controllerClusters.get(controller.getName()).size() == 1) {
    		controller.setComplexity(0);
    		return;
		}

    	Map<String, Integer> cache = new HashMap<>();

		float controllerComplexity = 0;

		Set<Controller.LocalTransaction> allLocalTransactions = controller.getAllLocalTransactions();

		for (Controller.LocalTransaction lt : allLocalTransactions) {

			List<AccessDto> clusterAccesses = lt.getClusterAccesses();
			int ltComplexity = 0;

			for (AccessDto a : clusterAccesses) {
				String entity = a.getEntity();
				String mode = a.getMode();

				Integer cost;
				String key;

				if (mode.equals("R")) {
					key = String.join("-", controller.getName(), entity, mode);
					cost = cache.get(key);

					if (cost == null) {
						cost = costOfRead(controller, entity);
						cache.put(key, cost);
					}
				}
				else {
					key = String.join("-", controller.getName(), entity, mode);
					cost = cache.get(key);

					if (cost == null) {
						cost = costOfWrite(controller, entity);
						cache.put(key, cost);
					}
				}

				ltComplexity += cost;
			}

			controllerComplexity += ltComplexity;
		}

		controller.setComplexity(controllerComplexity);
    }

    private int costOfRead (
    	Controller controller,
		String entity
	) {
    	int cost = 0;

        for (Controller otherController : this.graph.getControllers()) {
            if (
            	!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) &&
                otherController.getEntities().get(entity).contains("W") &&
            	this.controllerClusters.get(otherController.getName()).size() > 1
			) {
            	cost++;
            }
        }

        return cost;
    }

    private int costOfWrite (
    	Controller controller,
		String entity
	) {
    	int cost = 0;

        for (Controller otherController : this.graph.getControllers()) {
            if (
            	!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) && 
                otherController.getEntities().get(entity).contains("R") &&
				this.controllerClusters.get(otherController.getName()).size() > 1
			) {
            	cost++;
            }
        }

        return cost;
    }

	private void calculateClusterComplexity(Cluster cluster) {
    	List<Controller> controllersThatAccessThisCluster = this.clusterControllers.get(cluster.getName());

		float complexity = 0;
		for (Controller controller : controllersThatAccessThisCluster) {
			complexity += controller.getComplexity();
		}
		complexity /= controllersThatAccessThisCluster.size();
		complexity = BigDecimal.valueOf(complexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setComplexity(complexity);
	}

	public void calculateClusterCohesion(Cluster cluster) {
		List<Controller> controllersThatAccessThisCluster = this.clusterControllers.get(cluster.getName());

		float cohesion = 0;

		for (Controller controller : controllersThatAccessThisCluster) {
			float numberEntitiesTouched = 0;

			for (String controllerEntity : controller.getEntities().keySet()) {
				if (cluster.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}
			cohesion += numberEntitiesTouched / cluster.getEntities().size();
		}
		cohesion /= controllersThatAccessThisCluster.size();
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	private void calculateClusterCoupling(Cluster c1) {
    	float coupling = 0;
    	for (String c2 : c1.getCouplingDependencies().keySet()) {
    		coupling += (float) c1.getCouplingDependencies().get(c2).size() / graph.getCluster(c2).getEntities().size();
		}
		coupling = graph.getClusters().size() == 1 ? 0 : coupling / (graph.getClusters().size() - 1);
		coupling = BigDecimal.valueOf(coupling).setScale(2, RoundingMode.HALF_UP).floatValue();
		c1.setCoupling(coupling);
	}
}