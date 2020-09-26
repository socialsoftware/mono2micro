package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

public class Metrics {
    private final Graph graph;
	private final Map<String,List<Cluster>> controllerClusters;
	private final Map<String,List<Controller>> clusterControllers;

    public Metrics(Graph graph) {
        this.graph = graph;
		this.controllerClusters = graph.getControllerClusters();
		this.clusterControllers = graph.getClusterControllers();
    }

    public void calculateMetrics() {
		float graphComplexity = 0;
		float graphCohesion = 0;
		float graphCoupling = 0;
		float graphPerformance = 0;

		List<Controller> graphControllers = graph.getControllers();

		System.out.println("Calculating graph complexity and performance...");

		for (Controller controller : graphControllers) {
			calculateControllerComplexityAndClusterDependencies(controller);
//			calculateRedesignComplexities(controller, Constants.DEFAULT_REDESIGN_NAME);
			graphComplexity += controller.getComplexity();
			graphPerformance += controller.getPerformance();
		}

		int graphControllersAmount = graphControllers.size();

		graphComplexity /= graphControllersAmount;
		graphComplexity = BigDecimal.valueOf(graphComplexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setComplexity(graphComplexity);

		graphPerformance /= graphControllersAmount;
		graphPerformance = BigDecimal.valueOf(graphPerformance).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setPerformance(graphPerformance);

		List<Cluster> graphClusters = graph.getClusters();

		System.out.println("Calculating graph cohesion and coupling");

		for (Cluster cluster : graphClusters) {
			calculateClusterComplexityAndCohesion(cluster); // FIXME What do we do with the cluster complexity?

			graphCohesion += cluster.getCohesion();

			calculateClusterCoupling(cluster);
			graphCoupling += cluster.getCoupling();
		}

		int graphClustersAmount = graphClusters.size();

		graphCohesion /= graphClustersAmount;
		graphCohesion = BigDecimal.valueOf(graphCohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCohesion(graphCohesion);

		graphCoupling /= graphClustersAmount;
		graphCoupling = BigDecimal.valueOf(graphCoupling).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCoupling(graphCoupling);

    }

    private void calculateControllerComplexityAndClusterDependencies(Controller controller) {
		Set<Controller.LocalTransaction> allLocalTransactions = controller.getAllLocalTransactions();

		if (this.controllerClusters.get(controller.getName()).size() == 1) {
			controller.setComplexity(0);

		} else {

			Map<String, List<String>> cache = new HashMap<>(); // < entity + mode, List<controllerName>> controllersThatTouchSameEntities for a given mode
			float controllerComplexity = 0;

			for (Controller.LocalTransaction lt : allLocalTransactions) {
				// ClusterDependencies
				Cluster fromCluster = graph.getCluster(lt.getClusterName());

				if (fromCluster != null) { // not root node
					List<Controller.LocalTransaction> nextLocalTransactions = controller.getNextLocalTransactions(lt);

					for (Controller.LocalTransaction nextLt : nextLocalTransactions) {
						String toEntity = nextLt.getClusterAccesses().get(0).getEntity();
						fromCluster.addCouplingDependency(nextLt.getClusterName(), toEntity);
					}
				}

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
	}

	private List<String> costOfAccess (Controller controller, String entity, String mode) {

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

    private void calculateClusterComplexityAndCohesion(Cluster cluster) {
		List<Controller> controllersThatAccessThisCluster = this.clusterControllers.get(cluster.getName());

		float complexity = 0;
		float cohesion = 0;

		for (Controller controller : controllersThatAccessThisCluster) {
			// complexity calculus
			complexity += controller.getComplexity();

			// cohesion calculus
			float numberEntitiesTouched = 0;

			Set<String> controllerEntities = controller.getEntities().keySet();

			for (String controllerEntity : controllerEntities) {
				if (cluster.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}

			cohesion += numberEntitiesTouched / cluster.getEntities().size();
		}

		// complexity calculus
		complexity /= controllersThatAccessThisCluster.size();
		complexity = BigDecimal.valueOf(complexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setComplexity(complexity);

		// cohesion calculus
		cohesion /= controllersThatAccessThisCluster.size();
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	private void calculateClusterCoupling(Cluster c1) {
    	float coupling = 0;
		Map<String, Set<String>> couplingDependencies = c1.getCouplingDependencies();

    	for (String c2 : couplingDependencies.keySet())
    		coupling += (float) couplingDependencies.get(c2).size() / graph.getCluster(c2).getEntities().size();

    	int graphClustersAmount = graph.getClusters().size();

		coupling = graphClustersAmount == 1 ? 0 : coupling / (graphClustersAmount - 1);
		coupling = BigDecimal.valueOf(coupling).setScale(2, RoundingMode.HALF_UP).floatValue();
		c1.setCoupling(coupling);
	}

//	public void calculateRedesignComplexities(Controller controller, String redesignName){
//		FunctionalityRedesign functionalityRedesign = controller.getFunctionalityRedesign(redesignName);
//		functionalityRedesign.setFunctionalityComplexity(0);
//		functionalityRedesign.setSystemComplexity(0);
//
//		for (int i = 0; i < functionalityRedesign.getRedesign().size(); i++) {
//			LocalTransaction lt = functionalityRedesign.getRedesign().get(i);
//
//			if(!lt.getId().equals(String.valueOf(-1))){
//				try {
//					JSONArray sequence = new JSONArray(lt.getAccessedEntities());
//					for(int j=0; j < sequence.length(); j++){
//						String entity = sequence.getJSONArray(j).getString(0);
//						String accessMode = sequence.getJSONArray(j).getString(1);
//
//						if(accessMode.contains("W")){
//							if(lt.getType() == LocalTransactionTypes.COMPENSATABLE) {
//								functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
//								calculateSystemComplexity(entity, functionalityRedesign);
//							}
//						}
//
//						if (accessMode.contains("R")) {
//							functionalityComplexityCostOfRead(entity, controller, functionalityRedesign);
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

//	private void calculateSystemComplexity(String entity, FunctionalityRedesign functionalityRedesign) {
//		for (Controller otherController : this.graph.getControllers()) {
//			if (!otherController.getName().equals(functionalityRedesign.getName()) &&
//					otherController.containsEntity(entity) &&
//					otherController.getEntities().get(entity).contains("R") &&
//					this.controllerClusters.get(otherController.getName()).size() > 1) {
//				functionalityRedesign.setSystemComplexity(functionalityRedesign.getSystemComplexity() + 1);
//			}
//		}
//	}

//	private void functionalityComplexityCostOfRead(String entity, Controller controller, FunctionalityRedesign functionalityRedesign) throws JSONException {
//		for (Controller otherController : this.graph.getControllers()) {
//			if (!otherController.getName().equals(controller.getName()) &&
//					otherController.containsEntity(entity) &&
//					this.controllerClusters.get(otherController.getName()).size() > 1) {
//
//				if(otherController.getFunctionalityRedesigns().size() == 1 &&
//						otherController.getEntities().get(entity).contains("W")){
//					functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
//				}
//				else if(otherController.getFunctionalityRedesigns().size() > 1 &&
//					otherController.frUsedForMetrics().semanticLockEntities().contains(entity)){
//					functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
//				}
//
//			}
//		}
//	}
}