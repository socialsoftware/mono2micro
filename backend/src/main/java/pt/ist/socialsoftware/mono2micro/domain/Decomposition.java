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

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = DecompositionDeserializer.class)
public class Decomposition {

	public static class LocalTransaction {
		private int id; // transaction id to ensure that every node in the graph is unique
		private short clusterID; // changed to short instead of String to minimize the memory footprint
		private Set<AccessDto> clusterAccesses;
		private Set<Short> firstAccessedEntityIDs; // to calculate the coupling dependencies

		public LocalTransaction() {}

		public LocalTransaction(int id) {
			this.id = id;
		}

		public LocalTransaction(
			int id,
			short clusterID
		) {
			this.id = id;
			this.clusterID = clusterID;
		}

		public LocalTransaction(
			int id,
			short clusterID,
			Set<AccessDto> clusterAccesses,
			short firstAccessedEntityID
		) {
			this.id = id;
			this.clusterID = clusterID;
			this.clusterAccesses = clusterAccesses;
			this.firstAccessedEntityIDs = new HashSet<Short>() { { add(firstAccessedEntityID); } };
		}

		public LocalTransaction(LocalTransaction lt) {
			this.id = lt.getId();
			this.clusterID = lt.getClusterID();
			this.clusterAccesses = new HashSet<>(lt.getClusterAccesses());
			this.firstAccessedEntityIDs = new HashSet<>(lt.getFirstAccessedEntityIDs());
		}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public short getClusterID() { return clusterID; }
		public void setClusterID(short clusterID) { this.clusterID = clusterID; }
		public Set<AccessDto> getClusterAccesses() { return clusterAccesses; }
		public void setClusterAccesses(Set<AccessDto> clusterAccesses) { this.clusterAccesses = clusterAccesses; }
		public void addClusterAccess(AccessDto a) { this.clusterAccesses.add(a); }
		public Set<Short> getFirstAccessedEntityIDs() { return firstAccessedEntityIDs; }
		public void setFirstAccessedEntityIDs(Set<Short> firstAccessedEntityIDs) { this.firstAccessedEntityIDs = firstAccessedEntityIDs; }

		@Override
		public boolean equals(Object other) {
			if (other instanceof LocalTransaction) {
				LocalTransaction that = (LocalTransaction) other;
				return id == that.id;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	private String name;
	private String codebaseName;
	private String dendrogramName;
	private boolean expert;
	private float cutValue;
	private String cutType;
	private float silhouetteScore;
	private float complexity;
	private float performance;
	private float cohesion;
	private float coupling;
	private Map<String, Cluster> clusters = new HashMap<>(); // FIXME, should be Map<Short, Cluster>

	private Map<String, Controller> controllers = new HashMap<>(); // <controllerName, Controller>

	private Map<Short, String> entityIDToClusterName = new HashMap<>();

	public Decomposition() { }

	public String getCodebaseName() { return this.codebaseName; }

	public void setCodebaseName(String codebaseName) { this.codebaseName = codebaseName; }

	public String getDendrogramName() { return this.dendrogramName; }

	public void setDendrogramName(String dendrogramName) {
		this.dendrogramName = dendrogramName;
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

	public Map<Short, String> getEntityIDToClusterName() {
		return entityIDToClusterName;
	}

	public void setEntityIDToClusterName(Map<Short, String> entityIDToClusterName) { this.entityIDToClusterName = entityIDToClusterName; }

	public void putEntity(short entityID, String clusterName) {
		entityIDToClusterName.put(entityID, clusterName);
	}

	public Map<String, Cluster> getClusters() { return this.clusters; }

	public void setClusters(Map<String, Cluster> clusters) { this.clusters = clusters; }

	public Map<String, Controller> getControllers() { return controllers; }
	public void setControllers(Map<String, Controller> controllers) { this.controllers = controllers; }

	public boolean clusterExists(String clusterID) { return this.clusters.containsKey(clusterID); }

	public void addCluster(Cluster cluster) {
		Cluster c = this.clusters.putIfAbsent(cluster.getName(), cluster);

		if (c != null) throw new Error("Cluster with ID: " + cluster.getName() + " already exists");
	}

	public Cluster removeCluster(String clusterID) {
		Cluster c = this.clusters.remove(clusterID);

		if (c == null) throw new Error("Cluster with ID: " + clusterID + " not found");

		return c;
	}

	public Cluster getCluster(String clusterID) {
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
		public int performance;
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph;

		public GetLocalTransactionsGraphAndControllerPerformanceResult(
			int performance,
			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
		) {
			this.performance = performance;
			this.localTransactionsGraph = localTransactionsGraph;
		}
	}

	// FIXME MERGE getLocalTransactionsSequence AND calculateTracePerformance TO SPEED UP PROCESSING
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

		int controllerPerformance = 0;
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
							entityIDToClusterName,
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
							entityIDToClusterName,
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
							entityIDToClusterName,
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
		Codebase codebase, // requirements: datafilePath
		int tracesMaxLimit,
		Constants.TraceType traceType
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
		Map<String, Set<Controller>> clustersControllers = result1.clustersControllers;

		// COMPLEXITY AND PERFORMANCE CALCULATION
		CalculateComplexityAndPerformanceResult result2 = calculateComplexityAndPerformance(
			codebase,
			controllersClusters,
			traceType,
			tracesMaxLimit
		);

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

	public CalculateComplexityAndPerformanceResult calculateComplexityAndPerformance(
		Codebase codebase,
		Map<String, Set<Cluster>> controllersClusters,
		Constants.TraceType traceType,
		int tracesMaxLimit
	)
		throws IOException
	{
		ControllerTracesIterator iter = new ControllerTracesIterator(
			codebase.getDatafilePath(),
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

			int controllerPerformance = result2.performance;

			float controllerComplexity = Metrics.calculateControllerComplexityAndClusterDependencies(
				this,
				controllerName,
				controllersClusters,
				result2.localTransactionsGraph
			);

			// This needs to be done because the cluster complexity calculation depends on this result
			controller.setPerformance(controllerPerformance);
			controller.setComplexity(controllerComplexity);

			performance += controllerPerformance;
			complexity += controllerComplexity;
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
		Map<String, Set<Controller>> clustersControllers
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

	public void mergeClusters(
		String cluster1ID,
		String cluster2ID,
		String newName
	) {
		Cluster cluster1 = getCluster(cluster1ID);
		Cluster cluster2 = getCluster(cluster2ID);

		Cluster mergedCluster = new Cluster(newName);
		mergedCluster.setEntities(cluster1.getEntities());
		mergedCluster.setEntities(cluster2.getEntities());

		for(short entityID : cluster1.getEntities())
			entityIDToClusterName.replace(entityID, mergedCluster.getName());

		for(short entityID : cluster2.getEntities())
			entityIDToClusterName.replace(entityID, mergedCluster.getName());

		removeCluster(cluster1ID);
		removeCluster(cluster2ID);

		addCluster(mergedCluster);
	}

	public void renameCluster(
		String clusterID,
		String newID
	) {
		if (clusterID.equals(newID)) return;

		if (clusterExists(newID)) throw new KeyAlreadyExistsException("Cluster with ID: " + newID + " already exists");

		Cluster removedCluster = removeCluster(clusterID);

		removedCluster.setName(newID);

		for(short entityID : removedCluster.getEntities())
			entityIDToClusterName.replace(entityID, removedCluster.getName());

		addCluster(new Cluster(removedCluster));
	}

	public void splitCluster(
		String clusterID,
		String newID,
		String[] entities
	) {
		Cluster currentCluster = getCluster(clusterID);
		Cluster newCluster = new Cluster(newID);

		for (String stringifiedEntityID : entities) {
			short entityID = Short.parseShort(stringifiedEntityID);

			if (currentCluster.containsEntity(entityID)) {
				newCluster.addEntity(entityID);
				currentCluster.removeEntity(entityID);
				entityIDToClusterName.replace(entityID, newCluster.getName());
			}
		}

		addCluster(newCluster);
	}

	public void transferEntities(
		String fromClusterID,
		String toClusterID,
		String[] entities
	) {
		Cluster fromCluster = getCluster(fromClusterID);
		Cluster toCluster = getCluster(toClusterID);

		for (String stringifiedEntityID : entities) {
			short entityID = Short.parseShort(stringifiedEntityID);

			if (fromCluster.containsEntity(entityID)) {
				toCluster.addEntity(entityID);
				fromCluster.removeEntity(entityID);
				entityIDToClusterName.replace(entityID, toCluster.getName());
			}
		}
	}
}