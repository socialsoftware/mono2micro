package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.DecompositionDeserializer;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = DecompositionDeserializer.class)
public class Decomposition {
	private String name;
	private String codebaseName;
	private String strategyName;
	private boolean expert;
	private float silhouetteScore;
	private float complexity;
	private float performance;
	private float cohesion;
	private float coupling;
	//private List<Metric> metrics;

	//Metric tem referencia para model e outra para decomposition

	private Map<Short, Cluster> clusters = new HashMap<>();

	private Map<String, Controller> controllers = new HashMap<>(); // <controllerName, Controller>

	private Map<Short, Short> entityIDToClusterID = new HashMap<>();

	public Decomposition() { }

	public String getCodebaseName() { return this.codebaseName; }

	public void setCodebaseName(String codebaseName) { this.codebaseName = codebaseName; }

	public String getStrategyName() { return this.strategyName; }

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getName() { return this.name; }

	public void setName(String name) {
		this.name = name;
	}

	public boolean isExpert() {
		return expert;
	}

	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	public float getSilhouetteScore() {
		return this.silhouetteScore;
	}

	public void setSilhouetteScore(float silhouetteScore) {
		this.silhouetteScore = silhouetteScore;
	}

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public float getPerformance() { return performance; }

	public void setPerformance(float performance) { this.performance = performance; }

	public float getCohesion() {
		return cohesion;
	}

	public void setCohesion(float cohesion) {
		this.cohesion = cohesion;
	}

	public float getCoupling() {
		return coupling;
	}

	public void setCoupling(float coupling) {
		this.coupling = coupling;
	}

	public Map<Short, Short> getEntityIDToClusterID() {
		return entityIDToClusterID;
	}

	public void setEntityIDToClusterID(Map<Short, Short> entityIDToClusterID) { this.entityIDToClusterID = entityIDToClusterID; }

	public void putEntity(short entityID, Short clusterID) {
		entityIDToClusterID.put(entityID, clusterID);
	}

	public Map<Short, Cluster> getClusters() { return this.clusters; }

	public void setClusters(Map<Short, Cluster> clusters) { this.clusters = clusters; }

	public Map<String, Controller> getControllers() { return controllers; }

	public void setControllers(Map<String, Controller> controllers) { this.controllers = controllers; }

	public boolean controllerExists(String controllerName) { return this.controllers.containsKey(controllerName); }

	public Controller getController(String controllerName) {
		Controller c = this.controllers.get(controllerName);

		if (c == null) throw new Error("Controller with name: " + controllerName + " not found");

		return c;
	}

	public boolean clusterNameExists(String clusterName) {
		for (Map.Entry<Short, Cluster> cluster :this.clusters.entrySet())
			if (cluster.getValue().getName().equals(clusterName))
				return true;
		return false;
	}

	public void addCluster(Cluster cluster) {
		Cluster c = this.clusters.putIfAbsent(cluster.getID(), cluster);

		if (c != null) throw new Error("Cluster with ID: " + cluster.getID() + " already exists");
	}

	public Cluster removeCluster(Short clusterID) {
		Cluster c = this.clusters.remove(clusterID);

		if (c == null) throw new Error("Cluster with ID: " + clusterID + " not found");

		return c;
	}

	public Cluster getCluster(Short clusterID) {
		Cluster c = this.clusters.get(clusterID);

		if (c == null) throw new Error("Cluster with ID: " + clusterID + " not found");

		return c;
	}

	public int maxClusterSize() {
		int max = 0;

		for (Cluster cluster : this.clusters.values()) {
			if (cluster.getEntities().size() > max)
				max = cluster.getEntities().size();
		}

		return max;
	}

	public static Set<LocalTransaction> getAllLocalTransactions(
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
	) {
		return localTransactionsGraph.vertexSet();
	}

	public static List<LocalTransaction> getNextLocalTransactions(
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
		LocalTransaction lt
	) {
		return successorListOf(localTransactionsGraph, lt);
	}

	public static void addLocalTransactionsSequenceToGraph(
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
		List<LocalTransaction> localTransactionSequence
	) {
		LocalTransaction graphCurrentLT = new LocalTransaction(0, (short) -1); // root

		for (int i = 0; i < localTransactionSequence.size(); i++) {
			List<LocalTransaction> graphChildrenLTs = getNextLocalTransactions(
				localTransactionsGraph,
				graphCurrentLT
			);

			int graphChildrenLTsSize = graphChildrenLTs.size();

			if (graphChildrenLTsSize == 0) {
				createNewBranch(
					localTransactionsGraph,
					localTransactionSequence,
					graphCurrentLT,
					i
				);

				return;
			}

			for (int j = 0; j < graphChildrenLTsSize; j++) {
				LocalTransaction graphChildLT = graphChildrenLTs.get(j);
				LocalTransaction sequenceCurrentLT = localTransactionSequence.get(i);

				if (sequenceCurrentLT.getClusterID() == graphChildLT.getClusterID()) {
					graphChildLT.getClusterAccesses().addAll(sequenceCurrentLT.getClusterAccesses());
					graphChildLT.getFirstAccessedEntityIDs().addAll(sequenceCurrentLT.getFirstAccessedEntityIDs());

					graphCurrentLT = graphChildLT;
					break;

				} else {
					if (j == graphChildrenLTsSize - 1) {
						createNewBranch(
							localTransactionsGraph,
							localTransactionSequence,
							graphCurrentLT,
							i
						);

						return;
					}
				}
			}
		}
	}

	private static void createNewBranch(
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
		List<LocalTransaction> localTransactions,
		LocalTransaction currentLT,
		int i
	) {
		for (int k = i; k < localTransactions.size(); k++) {
			LocalTransaction lt = localTransactions.get(k);

			localTransactionsGraph.addVertex(lt);
			localTransactionsGraph.addEdge(currentLT, lt);
			currentLT = lt;
		}
	}

	public static class GetLocalTransactionsGraphAndControllerPerformanceResult {
		public float performance;
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph;

		public GetLocalTransactionsGraphAndControllerPerformanceResult(
			float performance,
			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
		) {
			this.performance = performance;
			this.localTransactionsGraph = localTransactionsGraph;
		}
	}

	public GetLocalTransactionsGraphAndControllerPerformanceResult getLocalTransactionsGraphAndControllerPerformance(
		ControllerTracesIterator iter,
		String controllerName,
		Constants.TraceType traceType
	)
		throws IOException
	{
		TraceDto t;
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		localTransactionsGraph.addVertex( // root
			new LocalTransaction(
				0,
				(short) -1
			)
		);

		int localTransactionsCounter = 1; // 1 because the root was already added with ID 0

		iter.nextControllerWithName(controllerName);

		float controllerPerformance = 0;
		int tracesCounter = 0;

		switch (traceType) {
			case LONGEST:
				t = iter.getLongestTrace();

				if (t != null) {
					List<ReducedTraceElementDto> traceElements = t.getElements();

					if (traceElements != null && traceElements.size() > 0) {
						Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
							localTransactionsCounter,
							null,
							traceElements,
								entityIDToClusterID,
							new HashMap<>(),
							0,
							traceElements.size()
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							result.localTransactionsSequence
						);

						controllerPerformance += result.performance;
					}
				}

				tracesCounter++;

				break;

			case WITH_MORE_DIFFERENT_ACCESSES:
				t = iter.getTraceWithMoreDifferentAccesses();

				if (t != null) {
					List<ReducedTraceElementDto> traceElements = t.getElements();

					if (traceElements != null && traceElements.size() > 0) {
						Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
							localTransactionsCounter,
							null,
							traceElements,
								entityIDToClusterID,
							new HashMap<>(),
							0,
							traceElements.size()
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							result.localTransactionsSequence
						);

						controllerPerformance += result.performance;
					}
				}

				tracesCounter++;

				break;

			// FIXME not going to fix this since time is scarce
//			case REPRESENTATIVE:
//				Set<String> tracesIds = iter.getRepresentativeTraces();
//				// FIXME probably here we create a second controllerTracesIterator
//				iter.reset();
//
//				while (iter.hasMoreTraces()) {
//					t = iter.nextTrace();
//					traceAccesses = t.expand(2);
//
//					if (tracesIds.contains(String.valueOf(t.getId())) && traceAccesses.size() > 0) {
//						List<LocalTransaction> localTransactionSequence = getLocalTransactionsSequence(
//							localTransactionsCounter,
//							entityIDToClusterName,
//							traceAccesses
//						);
//
//						addLocalTransactionsSequenceToGraph(
//							localTransactionsGraph,
//							localTransactionSequence
//						);
//
//						localTransactionsCounter += localTransactionSequence.size();
//
//						Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.calculateTracePerformance(
//							t.getElements(),
//							entityIDToClusterName,
//							0,
//							t.getElements() == null ? 0 : t.getElements().size()
//						);
//
//						controllerPerformance += result.performance;
//					}
//				}
//
//				break;

			default:
				while (iter.hasMoreTraces()) {
					tracesCounter++;

					t = iter.nextTrace();

					List<ReducedTraceElementDto> traceElements = t.getElements();

					if (traceElements != null && traceElements.size() > 0) {
						Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
							localTransactionsCounter,
							null,
							traceElements,
								entityIDToClusterID,
							new HashMap<>(),
							0,
							traceElements.size()
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							result.localTransactionsSequence
						);

						controllerPerformance += result.performance;
					}
				}
		}

		controllerPerformance /= tracesCounter;

		return new GetLocalTransactionsGraphAndControllerPerformanceResult(
			controllerPerformance,
			localTransactionsGraph
		);
	}

	// FIXME this method can be used differently depending on the use case
	// FIXME if a new metric needs to be calculated and the analyser has nothing to do with it
	// FIXME then separate this method into 2 similar ones: one for the dendrogram and the other for the analyser
	// FIXME and only then add the new calculation to the (new) respective method
	// FIXME By doing the above, extra performance overhead won't be added to the analyser
	public void calculateMetrics(
		String inputFilePath,
		int tracesMaxLimit,
		Constants.TraceType traceType,
		boolean isAnalyser
	)
		throws Exception
	{
		System.out.println("Calculating metrics...");

		Collection<Cluster> clusters = this.getClusters().values();

		Utils.GetControllersClustersAndClustersControllersResult result1 =
			Utils.getControllersClustersAndClustersControllers(
				clusters,
				this.getControllers().values()
			);

		Map<String, Set<Cluster>> controllersClusters = result1.controllersClusters;
		Map<Short, Set<Controller>> clustersControllers = result1.clustersControllers;

		// COMPLEXITY AND PERFORMANCE CALCULATION
		CalculateComplexityAndPerformanceResult result2;

		if(isAnalyser) {
			result2 = calculateComplexityAndPerformance(
					inputFilePath,
					controllersClusters,
					traceType,
					tracesMaxLimit
			);
		} else {
			result2 = calculateComplexityAndPerformanceAndRedesignMetrics(
					inputFilePath,
					controllersClusters,
					traceType,
					tracesMaxLimit
			);
		}

		this.setPerformance(result2.performance);
		this.setComplexity(result2.complexity);

		// COUPLING AND COHESION CALCULATION
		CalculateCouplingAndCohesionResult result3 = calculateCouplingAndCohesion(
			clusters,
			clustersControllers
		);

		this.setCohesion(result3.cohesion);
		this.setCoupling(result3.coupling);
	}

	public static class CalculateComplexityAndPerformanceResult {
		public float complexity;
		public float performance;

		public CalculateComplexityAndPerformanceResult(
			float complexity,
			float performance
		) {
			this.complexity = complexity;
			this.performance = performance;
		}
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> getControllerLocalTransactionsGraph(
		String inputFilePath,
		String controllerName,
		Constants.TraceType traceType,
		int tracesMaxLimit
	)
		throws IOException
	{
		if (!controllerExists(controllerName))
			throw new Error("Controller: " + controllerName + " does not exist");

		ControllerTracesIterator iter = new ControllerTracesIterator(
			inputFilePath,
			tracesMaxLimit
		);

		GetLocalTransactionsGraphAndControllerPerformanceResult result = getLocalTransactionsGraphAndControllerPerformance(
			iter,
			controllerName,
			traceType
		);

		return result.localTransactionsGraph;

	}

	public CalculateComplexityAndPerformanceResult calculateComplexityAndPerformance(
		String inputFilePath,
		Map<String, Set<Cluster>> controllersClusters,
		Constants.TraceType traceType,
		int tracesMaxLimit
	)
		throws IOException
	{
		ControllerTracesIterator iter = new ControllerTracesIterator(
			inputFilePath,
			tracesMaxLimit
		);

		float complexity = 0;
		float performance = 0;

		System.out.println("Calculating graph complexity and performance...");

		for (Controller controller : this.getControllers().values()) {
			String controllerName = controller.getName();

			GetLocalTransactionsGraphAndControllerPerformanceResult result2 = getLocalTransactionsGraphAndControllerPerformance(
				iter,
				controllerName,
				traceType
			);

			float controllerPerformance = result2.performance;

			float controllerComplexity = Metrics.calculateControllerComplexityAndClusterDependencies(
				this,
				controllerName,
				controllersClusters,
				result2.localTransactionsGraph
			);


			performance += controllerPerformance;
			complexity += controllerComplexity;

			// This needs to be done because the cluster complexity calculation depends on this result
			controller.setPerformance(
				BigDecimal.valueOf(controllerPerformance).setScale(2, RoundingMode.HALF_UP).floatValue()
			);

			controller.setComplexity(controllerComplexity);
		}

		int graphControllersAmount = controllersClusters.size();

		complexity = BigDecimal
			.valueOf(complexity / graphControllersAmount)
			.setScale(2, RoundingMode.HALF_UP)
			.floatValue();

		performance = BigDecimal
			.valueOf(performance / graphControllersAmount)
			.setScale(2, RoundingMode.HALF_UP)
			.floatValue();

		return new CalculateComplexityAndPerformanceResult(
			complexity,
			performance
		);
	}

	public CalculateComplexityAndPerformanceResult calculateComplexityAndPerformanceAndRedesignMetrics(
			String inputFilePath,
			Map<String, Set<Cluster>> controllersClusters,
			Constants.TraceType traceType,
			int tracesMaxLimit
	)
			throws IOException
	{
		ControllerTracesIterator iter = new ControllerTracesIterator(
				inputFilePath,
				tracesMaxLimit
		);

		float complexity = 0;
		float performance = 0;

		System.out.println("Calculating graph complexity and performance...");

		for (Controller controller : this.getControllers().values()) {
			String controllerName = controller.getName();

			GetLocalTransactionsGraphAndControllerPerformanceResult result2 = getLocalTransactionsGraphAndControllerPerformance(
					iter,
					controllerName,
					traceType
			);

			float controllerPerformance = result2.performance;

			float controllerComplexity = Metrics.calculateControllerComplexityAndClusterDependencies(
					this,
					controllerName,
					controllersClusters,
					result2.localTransactionsGraph
			);


			performance += controllerPerformance;
			complexity += controllerComplexity;

			// This needs to be done because the cluster complexity calculation depends on this result
			controller.setPerformance(
					BigDecimal.valueOf(controllerPerformance).setScale(2, RoundingMode.HALF_UP).floatValue()
			);

			controller.setComplexity(controllerComplexity);

			controller.createFunctionalityRedesign(
					Constants.DEFAULT_REDESIGN_NAME,
					true,
					result2.localTransactionsGraph
			);

			Metrics.calculateRedesignComplexities(
					controller,
					Constants.DEFAULT_REDESIGN_NAME,
					this
			);
		}

		int graphControllersAmount = controllersClusters.size();

		complexity = BigDecimal
				.valueOf(complexity / graphControllersAmount)
				.setScale(2, RoundingMode.HALF_UP)
				.floatValue();

		performance = BigDecimal
				.valueOf(performance / graphControllersAmount)
				.setScale(2, RoundingMode.HALF_UP)
				.floatValue();

		return new CalculateComplexityAndPerformanceResult(
				complexity,
				performance
		);
	}

	public static class CalculateCouplingAndCohesionResult {
		public float coupling;
		public float cohesion;

		public CalculateCouplingAndCohesionResult(
			float coupling,
			float cohesion
		) {
			this.coupling = coupling;
			this.cohesion = cohesion;
		}
	}

	public CalculateCouplingAndCohesionResult calculateCouplingAndCohesion(
		Collection<Cluster> clusters,
		Map<Short, Set<Controller>> clustersControllers
	) {
		System.out.println("Calculating graph cohesion and coupling...");

		float cohesion = 0;
		float coupling = 0;

		for (Cluster cluster : clusters) {
			Metrics.calculateClusterComplexityAndCohesion(
				cluster,
				clustersControllers
			);

			Metrics.calculateClusterCoupling(
				cluster,
				this.getClusters()
			);

			coupling += cluster.getCoupling();
			cohesion += cluster.getCohesion();
		}

		int graphClustersAmount = clusters.size();

		cohesion = BigDecimal
			.valueOf(cohesion / graphClustersAmount)
			.setScale(2, RoundingMode.HALF_UP)
			.floatValue();

		coupling = BigDecimal
			.valueOf(coupling / graphClustersAmount)
			.setScale(2, RoundingMode.HALF_UP)
			.floatValue();

		return new CalculateCouplingAndCohesionResult(
			coupling,
			cohesion
		);
	}

	public short getNewClusterID() {
		return (short) (Collections.max(clusters.keySet()) + 1);
	}

	public void mergeClusters(
		Short cluster1ID,
		Short cluster2ID,
		String newName
	) {
		Cluster cluster1 = getCluster(cluster1ID);
		Cluster cluster2 = getCluster(cluster2ID);

		Cluster mergedCluster = new Cluster(getNewClusterID(), newName);

		for(short entityID : cluster1.getEntities()) {
			entityIDToClusterID.replace(entityID, mergedCluster.getID());
			removeControllerWithEntity(entityID);
		}

		for(short entityID : cluster2.getEntities()) {
			entityIDToClusterID.replace(entityID, mergedCluster.getID());
			removeControllerWithEntity(entityID);
		}

		Set<Short> allEntities = new HashSet<>(cluster1.getEntities());
		allEntities.addAll(cluster2.getEntities());
		mergedCluster.setEntities(allEntities);

		removeCluster(cluster1ID);
		removeCluster(cluster2ID);

		transferCouplingDependencies(cluster1.getEntities(), cluster1.getID(), mergedCluster.getID());
		transferCouplingDependencies(cluster2.getEntities(), cluster2.getID(), mergedCluster.getID());

		addCluster(mergedCluster);
	}

	public void renameCluster(
		Short clusterID,
		String newName
	) {
		if (clusterNameExists(newName)) throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

		Cluster removedCluster = removeCluster(clusterID);

		removedCluster.setName(newName);

		addCluster(new Cluster(removedCluster));
	}

	public void splitCluster(
		Short clusterID,
		String newName,
		String[] entities
	) {
		Cluster currentCluster = getCluster(clusterID);
		Cluster newCluster = new Cluster(getNewClusterID(), newName);

		for (String stringifiedEntityID : entities) {
			short entityID = Short.parseShort(stringifiedEntityID);

			if (currentCluster.containsEntity(entityID)) {
				newCluster.addEntity(entityID);
				currentCluster.removeEntity(entityID);
				entityIDToClusterID.replace(entityID, newCluster.getID());
				removeControllerWithEntity(entityID);
			}
		}
		transferCouplingDependencies(newCluster.getEntities(), currentCluster.getID(), newCluster.getID());
		addCluster(newCluster);
	}

	//TODO: if possible, use something more fine grained
	private void removeControllerWithEntity(short entityID) {
		this.setControllers(this.getControllers().entrySet()
						.stream()
						.filter(controllerEntry -> !controllerEntry.getValue().containsEntity(entityID))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	private void transferCouplingDependencies(Set<Short> entities, short currentClusterID, short newClusterID) {
		this.getCluster(currentClusterID).clearCouplingDependencies(); //Removes dependencies since access sequence changes for this cluster
		for (Cluster cluster : this.getClusters().values())
			cluster.transferCouplingDependencies(entities, currentClusterID, newClusterID);
	}

	public void transferEntities(
		Short fromClusterID,
		Short toClusterID,
		String[] entitiesString
	) {
		Cluster fromCluster = getCluster(fromClusterID);
		Cluster toCluster = getCluster(toClusterID);
		Set<Short> entities = Arrays.stream(entitiesString).map(Short::valueOf).collect(Collectors.toSet());

		for (Short entityID : entities) {

			if (fromCluster.containsEntity(entityID)) {
				toCluster.addEntity(entityID);
				fromCluster.removeEntity(entityID);
				entityIDToClusterID.replace(entityID, toCluster.getID());
				removeControllerWithEntity(entityID);
			}
		}
		transferCouplingDependencies(entities, fromClusterID, toClusterID);
	}
}