package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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

    	Map<String, List<String>> cache = new HashMap<>(); // < entity + mode, List<controllerName>> controllersThatTouchSameEntities for a given mode

		float controllerComplexity = 0;


		Set<Controller.LocalTransaction> allLocalTransactions = controller.getAllLocalTransactions();

		for (Controller.LocalTransaction lt : allLocalTransactions) {

			Set<String> controllersThatTouchSameEntities = new HashSet<>();
			List<AccessDto> clusterAccesses = lt.getClusterAccesses();

			for (AccessDto a : clusterAccesses) {
				String entity = a.getEntity();
				String mode = a.getMode();

				String key = String.join("-", entity, mode);
				List<String> controllersThatTouchThisEntityAndMode = cache.get(key);

				if (controllersThatTouchThisEntityAndMode == null) {
					controllersThatTouchThisEntityAndMode = costOfAccess(controller, entity, mode);
					cache.put(key, controllersThatTouchThisEntityAndMode);
				}

				controllersThatTouchSameEntities.addAll(controllersThatTouchThisEntityAndMode);
			}

			controllerComplexity += controllersThatTouchSameEntities.size();
		}

		controller.setComplexity(controllerComplexity);
    }

    private List<String> costOfAccess (
    	Controller controller,
		String entity,
		String mode
	) {
		List<String> controllersThatTouchThisEntityAndMode = new ArrayList<>();

        for (Controller otherController : this.graph.getControllers()) {
            if (
            	!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) &&
                otherController.getEntities().get(entity).contains(mode.equals("W") ? "R" : "W") &&
            	this.controllerClusters.get(otherController.getName()).size() > 1
			) {
				controllersThatTouchThisEntityAndMode.add(otherController.getName());
            }
        }

        return controllersThatTouchThisEntityAndMode;
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