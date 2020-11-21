package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

public class Metrics {
//    public void calculateGraphMetrics(
//    	Graph graph,
//		DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge> localTransactionsGraph
//	) {
//		float graphComplexity = 0;
//		float graphCohesion = 0;
//		float graphCoupling = 0;
//		float graphPerformance = 0;
//
//		Map<String, Cluster> graphClusters = graph.getClusters();
//
//		System.out.println("Calculating graph complexity and performance...");
//		// int graphNodes = 0;
//		// int maxNumberOfNodes = 0;
//
//		Map<String, List<Cluster>> controllerClusters = Utils.getControllerClusters(
//			(List<Cluster>) graphClusters.values(),
//			controllers
//		);
//
//		for (Controller controller : controllers) {
//			calculateControllerComplexityAndClusterDependencies(
//				graph,
//				controller,
//				controllerClusters,
//				localTransactionsGraph
//			);
//
////			calculateRedesignComplexities(controller, Constants.DEFAULT_REDESIGN_NAME);
//			graphComplexity += controller.getComplexity();
//			graphPerformance += controller.getPerformance();
//			// graphNodes += controller.getAllLocalTransactions().size();
//			// if (controller.getAllLocalTransactions().size() > maxNumberOfNodes)
//			// 	maxNumberOfNodes = controller.getAllLocalTransactions().size();
//		}
//
//		// System.out.println("Média de nós do grafo: " + graphNodes/graphControllers.size());
//		// System.out.println("Máximo numero de nós: " + maxNumberOfNodes);
//
//		controllerClusters = null; // memory release
//
//		int graphControllersAmount = controllers.size();
//
//		graphComplexity = BigDecimal
//							.valueOf(graphComplexity / graphControllersAmount)
//							.setScale(2, RoundingMode.HALF_UP)
//							.floatValue();
//
//		graph.setComplexity(graphComplexity);
//
//		graphPerformance = BigDecimal
//							.valueOf(graphPerformance / graphControllersAmount)
//							.setScale(2, RoundingMode.HALF_UP)
//							.floatValue();
//
//		graph.setPerformance(graphPerformance);
//
//		System.out.println("Calculating graph cohesion and coupling");
//
//		Map<String, List<Controller>> clusterControllers = Utils.getClusterControllers(
//			(List<Cluster>) graphClusters.values(),
//			controllers
//		);
//
//		for (Cluster cluster : graphClusters.values()) {
//			calculateClusterComplexityAndCohesion(
//				cluster,
//				clusterControllers
//			);
//
//			graphCohesion += cluster.getCohesion();
//
//			calculateClusterCoupling(
//				cluster,
//				graphClusters
//			);
//
//			graphCoupling += cluster.getCoupling();
//		}
//
//		clusterControllers = null; // memory release
//
//		int graphClustersAmount = graphClusters.size();
//
//		graphCohesion = BigDecimal
//							.valueOf(graphCohesion / graphClustersAmount)
//							.setScale(2, RoundingMode.HALF_UP)
//							.floatValue();
//
//		graph.setCohesion(graphCohesion);
//
//		graphCoupling = BigDecimal
//							.valueOf(graphCoupling / graphClustersAmount)
//							.setScale(2, RoundingMode.HALF_UP)
//							.floatValue();
//
//		graph.setCoupling(graphCoupling);
//    }

    public static float calculateControllerComplexityAndClusterDependencies(
	 	Decomposition decomposition,
		String controllerName,
	 	Map<String, Set<Cluster>> controllerClusters,
		DirectedAcyclicGraph<Decomposition.LocalTransaction, DefaultEdge> localTransactionsGraph
	) {
		Set<Decomposition.LocalTransaction> allLocalTransactions = Decomposition.getAllLocalTransactions(localTransactionsGraph);

		if (controllerClusters.get(controllerName).size() == 1) {
			return 0;

		} else {
			// < entity + mode, List<controllerName>> controllersThatTouchSameEntities for a given mode
			Map<String, List<String>> cache = new HashMap<>();

			float controllerComplexity = 0;

			for (Decomposition.LocalTransaction lt : allLocalTransactions) {
				// ClusterDependencies
				short clusterID = lt.getClusterID();
				if (clusterID != -1) { // not root node
					Cluster fromCluster = decomposition.getCluster(String.valueOf(clusterID));

					List<Decomposition.LocalTransaction> nextLocalTransactions = Decomposition.getNextLocalTransactions(
						localTransactionsGraph,
						lt
					);

					for (Decomposition.LocalTransaction nextLt : nextLocalTransactions)
						fromCluster.addCouplingDependencies(
							String.valueOf(nextLt.getClusterID()),
							nextLt.getFirstAccessedEntityIDs()
						);

					Set<String> controllersThatTouchSameEntities = new HashSet<>();
					Set<AccessDto> clusterAccesses = lt.getClusterAccesses();

					for (AccessDto a : clusterAccesses) {
						short entityID = a.getEntityID();
						byte mode = a.getMode();

						String key = String.join("-", String.valueOf(entityID), String.valueOf(mode));
						List<String> controllersThatTouchThisEntityAndMode = cache.get(key);

						if (controllersThatTouchThisEntityAndMode == null) {
							controllersThatTouchThisEntityAndMode = costOfAccess(
								controllerName,
								entityID,
								mode,
								decomposition.getControllers().values(),
								controllerClusters
							);

							cache.put(key, controllersThatTouchThisEntityAndMode);
						}

						controllersThatTouchSameEntities.addAll(controllersThatTouchThisEntityAndMode);
					}

					controllerComplexity += controllersThatTouchSameEntities.size();
				}
			}

			return controllerComplexity;
		}
	}

	private static List<String> costOfAccess(
		String controllerName,
		short entityID,
		byte mode,
		Collection<Controller> controllers,
		Map<String, Set<Cluster>> controllerClusters
	) {
		List<String> controllersThatTouchThisEntityAndMode = new ArrayList<>();

		for (Controller otherController : controllers) {
			String otherControllerName = otherController.getName();

			if (!otherControllerName.equals(controllerName) && controllerClusters.containsKey(otherControllerName)) {
				Byte savedMode = otherController.getEntities().get(entityID);

				if (
					savedMode != null &&
					savedMode != mode &&
					controllerClusters.get(otherControllerName).size() > 1
				) {
					controllersThatTouchThisEntityAndMode.add(otherControllerName);
				}
			}
		}

		return controllersThatTouchThisEntityAndMode;
	}

    public static void calculateClusterComplexityAndCohesion(
    	Cluster cluster,
		Map<String, Set<Controller>> clusterControllers
	) {
		Set<Controller> controllersThatAccessThisCluster = clusterControllers.get(cluster.getName());

		float complexity = 0;
		float cohesion = 0;

		for (Controller controller : controllersThatAccessThisCluster) {
			// complexity calculus
			complexity += controller.getComplexity();

			// cohesion calculus
			float numberEntitiesTouched = 0;

			Set<Short> controllerEntities = controller.getEntities().keySet();

			for (short entityID : controllerEntities) {
				if (cluster.containsEntity(entityID))
					numberEntitiesTouched++;
			}

			cohesion += numberEntitiesTouched / cluster.getEntities().size();
		}

		// complexity calculus
		complexity /= controllersThatAccessThisCluster.size();
		complexity = BigDecimal
						.valueOf(complexity)
						.setScale(2, RoundingMode.HALF_UP)
						.floatValue();

		cluster.setComplexity(complexity);

		// cohesion calculus
		cohesion /= controllersThatAccessThisCluster.size();
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	public static void calculateClusterCoupling(
		Cluster c1,
		Map<String, Cluster> clusters
	) {
    	float coupling = 0;
		Map<String, Set<Short>> couplingDependencies = c1.getCouplingDependencies();

    	for (String c2 : couplingDependencies.keySet())
    		coupling += (float) couplingDependencies.get(c2).size() / clusters.get(c2).getEntities().size();

    	int graphClustersAmount = clusters.size();

		coupling = graphClustersAmount == 1 ? 0 : coupling / (graphClustersAmount - 1);
		coupling = BigDecimal
					.valueOf(coupling)
					.setScale(2, RoundingMode.HALF_UP)
					.floatValue();

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