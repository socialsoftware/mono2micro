package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

public class Metrics {
	//TODO: Optimize
    public static float calculateControllerComplexityAndClusterDependencies(
	 	Decomposition decomposition,
		String controllerName,
	 	Map<String, Set<Cluster>> controllerClusters,
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
	) {
		Set<LocalTransaction> allLocalTransactions = Decomposition.getAllLocalTransactions(localTransactionsGraph);

		if (controllerClusters.get(controllerName).size() == 1) {
			return 0;

		} else {
			// < entity + mode, List<controllerName>> controllersThatTouchSameEntities for a given mode
			Map<String, List<String>> cache = Collections.synchronizedMap(new HashMap<>());

			AtomicReference<Float> controllerComplexity = new AtomicReference<>((float) 0);
			allLocalTransactions.stream().forEach(lt -> {
				// ClusterDependencies
				short clusterID = lt.getClusterID();
				if (clusterID != -1) { // not root node
					Cluster fromCluster = decomposition.getCluster(clusterID);

					List<LocalTransaction> nextLocalTransactions = Decomposition.getNextLocalTransactions(
							localTransactionsGraph,
							lt
					);

					for (LocalTransaction nextLt : nextLocalTransactions)
						fromCluster.addCouplingDependencies(
								nextLt.getClusterID(),
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

					controllerComplexity.updateAndGet(v -> (float) (v + controllersThatTouchSameEntities.size()));
				}
			});
//			System.out.println("Complexity for " + controllerName + " is " + controllerComplexity.get());
			return controllerComplexity.get();
		}
	}
//Start: 12:46:18
	private static List<String> costOfAccess(
		String controllerName,
		short entityID,
		byte mode,
		Collection<Controller> controllers,
		Map<String, Set<Cluster>> controllerClusters
	) {
		List<String> controllersThatTouchThisEntityAndMode = Collections.synchronizedList(new ArrayList<>());
		// Parallelize
		controllers.stream().forEach(otherController -> {
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
		});


		return controllersThatTouchThisEntityAndMode;
	}

    public static void calculateClusterComplexityAndCohesion(
    	Cluster cluster,
		Map<Short, Set<Controller>> clusterControllers
	) {
		Set<Controller> controllersThatAccessThisCluster = clusterControllers.get(cluster.getID());

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
		if (controllersThatAccessThisCluster.size() == 0) {
			complexity = 0;
		} else {
			complexity /= controllersThatAccessThisCluster.size();
		}
		complexity = BigDecimal
						.valueOf(complexity)
						.setScale(2, RoundingMode.HALF_UP)
						.floatValue();

		cluster.setComplexity(complexity);

		// cohesion calculus
		if (controllersThatAccessThisCluster.size() == 0) {
			cohesion = 0;
		} else {
			cohesion /= controllersThatAccessThisCluster.size();
		}
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	public static void calculateClusterCoupling(
		Cluster c1,
		Map<Short, Cluster> clusters
	) {
    	float coupling = 0;
		Map<Short, Set<Short>> couplingDependencies = c1.getCouplingDependencies();

    	for (Short c2 : couplingDependencies.keySet())
    		coupling += (float) couplingDependencies.get(c2).size() / clusters.get(c2).getEntities().size();

    	int graphClustersAmount = clusters.size();

		coupling = graphClustersAmount == 1 ? 0 : coupling / (graphClustersAmount - 1);
		coupling = BigDecimal
					.valueOf(coupling)
					.setScale(2, RoundingMode.HALF_UP)
					.floatValue();

		c1.setCoupling(coupling);
	}

	public static void calculateRedesignComplexities(
			Controller controller,
			String redesignName,
			Decomposition decomposition
	){
		Utils.GetControllersClustersAndClustersControllersResult result =
				Utils.getControllersClustersAndClustersControllers(
						decomposition.getClusters().values(),
						decomposition.getControllers().values()
				);

		ControllerType type = controller.defineControllerType();

		if(type == ControllerType.QUERY)
			calculateQueryRedesignComplexity(controller, redesignName, decomposition);
		else
			calculateSAGASRedesignComplexities(
					controller,
					redesignName,
					decomposition,
					result.controllersClusters
			);
	}

	private static void calculateQueryRedesignComplexity(
			Controller controller,
			String redesignName,
			Decomposition decomposition
	) {
		FunctionalityRedesign functionalityRedesign = controller.getFunctionalityRedesign(redesignName);
		functionalityRedesign.setInconsistencyComplexity(0);

		Set<Short> entitiesRead = controller.entitiesTouchedInAGivenMode((byte) 1);

		for (Controller otherController : decomposition.getControllers().values()) {
			if (!otherController.getName().equals(controller.getName()) &&
				otherController.defineControllerType() == ControllerType.SAGA){

				Set<Short> entitiesWritten = otherController.entitiesTouchedInAGivenMode((byte) 2);
				entitiesWritten.retainAll(entitiesRead);
				Set<Short> clustersInCommon = otherController.clustersOfGivenEntities(entitiesWritten);

				if(clustersInCommon.size() > 1){
					functionalityRedesign.setInconsistencyComplexity(
						functionalityRedesign.getInconsistencyComplexity() + clustersInCommon.size()
					);
				}
			}
		}
	}

	private static void calculateSAGASRedesignComplexities(
			Controller controller,
			String redesignName,
			Decomposition decomposition,
			Map<String, Set<Cluster>> controllerClusters
	){
		FunctionalityRedesign functionalityRedesign = controller.getFunctionalityRedesign(redesignName);
		functionalityRedesign.setFunctionalityComplexity(0);
		functionalityRedesign.setSystemComplexity(0);

		for (int i = 0; i < functionalityRedesign.getRedesign().size(); i++) {
			LocalTransaction lt = functionalityRedesign.getRedesign().get(i);

			if(lt.getId() != 0){
				for(AccessDto accessDto : lt.getClusterAccesses()) {
					short entity = accessDto.getEntityID();
					byte mode = accessDto.getMode();

					if(mode >= 2){ // 2 -> W, 3 -> RW
						if(lt.getType() == LocalTransactionTypes.COMPENSATABLE) {
							functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
							calculateSystemComplexity(
									entity,
									controller,
									functionalityRedesign,
									decomposition,
									controllerClusters
							);
						}
					}

					if (mode != 2) { // 2 -> W - we want all the reads
						functionalityComplexityCostOfRead(
								entity,
								controller,
								functionalityRedesign,
								decomposition,
								controllerClusters
						);
					}
				}
			}
		}
	}

	private static void calculateSystemComplexity(
			short entity,
			Controller controller,
			FunctionalityRedesign functionalityRedesign,
			Decomposition decomposition,
			Map<String, Set<Cluster>> controllerClusters
	) {
		for (Controller otherController : decomposition.getControllers().values()) {
			if (!otherController.getName().equals(controller.getName()) &&
				otherController.containsEntity(entity) &&
					otherController.getEntities().get(entity) != 2 &&
					controllerClusters.get(otherController.getName()).size() > 1) {
				functionalityRedesign.setSystemComplexity(functionalityRedesign.getSystemComplexity() + 1);
			}
		}
	}

	private static void functionalityComplexityCostOfRead(
			short entity,
			Controller controller,
			FunctionalityRedesign functionalityRedesign,
			Decomposition decomposition,
			Map<String, Set<Cluster>> controllerClusters
	) {
		for (Controller otherController : decomposition.getControllers().values()) {
			if (!otherController.getName().equals(controller.getName()) &&
				otherController.containsEntity(entity) &&
					controllerClusters.get(otherController.getName()).size() > 1) {

				if(otherController.getFunctionalityRedesigns().size() == 1 &&
					otherController.getEntities().get(entity) >= 2){
					functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
				}
				else if(otherController.getFunctionalityRedesigns().size() > 1 &&
					otherController.frUsedForMetrics().semanticLockEntities().contains(entity)){
					functionalityRedesign.setFunctionalityComplexity(functionalityRedesign.getFunctionalityComplexity() + 1);
				}

			}
		}
	}
}
