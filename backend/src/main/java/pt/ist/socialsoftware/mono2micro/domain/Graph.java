package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.GraphDeserializer;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = GraphDeserializer.class)
public class Graph {

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

	private String codebaseName;
	private String dendrogramName;
	private String name;
	private boolean expert;
	private float cutValue;
	private String cutType;
	private float silhouetteScore;
	private float complexity;
	private float performance;
	private float cohesion;
	private float coupling;
	private Map<String, Cluster> clusters = new HashMap<>(); // FIXME, should be Map<Short, Cluster>
	@JsonIgnore
	private final Map<Short, String> entityIDToClusterName = new HashMap<>();

	public Graph() { }

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

	public Map<String, Cluster> getClusters() { return this.clusters; }

	public void setClusters(Map<String, Cluster> clusters) { this.clusters = clusters; }

	public void addCluster(Cluster cluster) { this.clusters.put(cluster.getName(), cluster); }

	public int maxClusterSize() {
		int max = 0;

		for (Cluster cluster : this.clusters.values()) {
			if (cluster.getEntities().size() > max)
				max = cluster.getEntities().size();
		}

		return max;
	}

	public void putEntity(short entityID, String clusterName) {
		entityIDToClusterName.put(entityID, clusterName);
	}

	public static List<LocalTransaction> getLocalTransactionsSequence(
		int currentLocalTransactionId,
		Map<Short, String> entityIDToClusterName,
		List<AccessDto> accesses
	) {
		LocalTransaction lt = null;
		List<LocalTransaction> localTransactionsSequence = new ArrayList<>();
		Map<Short, Byte> entityIDToMode = new HashMap<>();

		String previousCluster = ""; // IntelliJ is afraid. poor him

		int localTransactionsCounter = currentLocalTransactionId;
//		JSONArray entitiesSeq = new JSONArray();
//		JSONObject clusterAccess = new JSONObject();

		for (int i = 0; i < accesses.size(); i++) {
			AccessDto access = accesses.get(i);
			short entityID = access.getEntityID();
			byte mode = access.getMode();
			String cluster;

			cluster = entityIDToClusterName.get(entityID);

			if (cluster == null) {
				System.err.println("Entity " + entityID + " is not assign to a cluster.");
				System.exit(-1);
			}

			if (i == 0) {
				lt = new LocalTransaction(
					localTransactionsCounter++,
					Short.parseShort(cluster),
					new HashSet<AccessDto>() { { add(access); } },
					entityID
				);

				entityIDToMode.put(entityID, mode);

//				clusterAccess.put("cluster", cluster);
//				clusterAccess.put("sequence", new JSONArray());
//				clusterAccess.getJSONArray("sequence").put(
//					new JSONArray().put(entity).put(mode)
//				);

			} else {

				if (cluster.equals(previousCluster)) {
					boolean hasCost = false;
					Byte savedMode = entityIDToMode.get(entityID);

					if (savedMode == null) {
						hasCost = true;

					} else {
						if (savedMode == 1 && mode == 2) // "R" -> 1, "W" -> 2
							hasCost = true;
					}

					if (hasCost) {
						lt.addClusterAccess(access);
						entityIDToMode.put(entityID, mode);

//						clusterAccess.getJSONArray("sequence").put(
//							new JSONArray().put(entity).put(mode)
//						);
					}

				} else {
					localTransactionsSequence.add(new LocalTransaction(lt));

					lt = new LocalTransaction(
						localTransactionsCounter++,
						Short.parseShort(cluster),
						new HashSet<AccessDto>() { { add(access); } },
						entityID
					);

					entityIDToMode.clear();
					entityIDToMode.put(entityID, mode);

//					entitiesSeq.put(clusterAccess);
//					clusterAccess = new JSONObject();
//					clusterAccess.put("cluster", cluster);
//					clusterAccess.put("sequence", new JSONArray());
//					clusterAccess.getJSONArray("sequence").put(
//						new JSONArray().put(entity).put(mode)
//					);
				}
			}

			previousCluster = cluster;
		}

//		entitiesSeq.put(clusterAccess);
//		controller.addEntitiesSeq(entitiesSeq);
//		controller.createFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME, true);

		if (lt != null && lt.getClusterAccesses().size() > 0)
			localTransactionsSequence.add(lt);

		return localTransactionsSequence;
	}

	public static class GetLocalTransactionsGraphAndControllerPerformanceResult {
		public int performance = 0;
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph;

		public GetLocalTransactionsGraphAndControllerPerformanceResult(
			int performance,
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
		List<AccessDto> traceAccesses;
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		localTransactionsGraph.addVertex( // root
			new LocalTransaction(
				0,
				(short) -1
			)
		);

		int localTransactionsCounter = 1;

		iter.nextControllerWithName(controllerName);

		int controllerPerformance = 0;
		int tracesCounter = 0;

		switch (traceType) {
			case LONGEST:
				t = iter.getLongestTrace();

				if (t != null) {
					traceAccesses = t.expand(2);

					if (traceAccesses.size() > 0) {
						List<LocalTransaction> localTransactionSequence = getLocalTransactionsSequence(
							localTransactionsCounter,
							entityIDToClusterName,
							traceAccesses
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							localTransactionSequence
						);

					}

					Utils.CalculateTracePerformanceResult result = Utils.calculateTracePerformance(
						t.getElements(),
						entityIDToClusterName,
						0,
						t.getElements() == null ? 0 : t.getElements().size()
					);

					controllerPerformance += result.performance;
				}

				break;

			case WITH_MORE_DIFFERENT_ACCESSES:
				t = iter.getTraceWithMoreDifferentAccesses();

				if (t != null) {
					traceAccesses = t.expand(2);

					if (traceAccesses.size() > 0) {
						List<LocalTransaction> localTransactionSequence = getLocalTransactionsSequence(
							localTransactionsCounter,
							entityIDToClusterName,
							traceAccesses
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							localTransactionSequence
						);
					}

					Utils.CalculateTracePerformanceResult result = Utils.calculateTracePerformance(
						t.getElements(),
						entityIDToClusterName,
						0,
						t.getElements() == null ? 0 : t.getElements().size()
					);

					controllerPerformance += result.performance;
				}

				break;

			case REPRESENTATIVE:
				Set<String> tracesIds = iter.getRepresentativeTraces();
				// FIXME probably here we create a second controllerTracesIterator
				iter.reset();

				while (iter.hasMoreTraces()) {
					t = iter.nextTrace();
					traceAccesses = t.expand(2);

					if (tracesIds.contains(String.valueOf(t.getId())) && traceAccesses.size() > 0) {
						List<LocalTransaction> localTransactionSequence = getLocalTransactionsSequence(
							localTransactionsCounter,
							entityIDToClusterName,
							traceAccesses
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							localTransactionSequence
						);

						localTransactionsCounter += localTransactionSequence.size();

						Utils.CalculateTracePerformanceResult result = Utils.calculateTracePerformance(
							t.getElements(),
							entityIDToClusterName,
							0,
							t.getElements() == null ? 0 : t.getElements().size()
						);

						controllerPerformance += result.performance;
					}
				}

				break;

			default:

				while (iter.hasMoreTraces()) {
					tracesCounter++;

					t = iter.nextTrace();
					traceAccesses = t.expand(2);

					if (traceAccesses.size() > 0) {
						List<LocalTransaction> localTransactionSequence = getLocalTransactionsSequence(
							localTransactionsCounter,
							entityIDToClusterName,
							traceAccesses
						);

						addLocalTransactionsSequenceToGraph(
							localTransactionsGraph,
							localTransactionSequence
						);

						localTransactionsCounter += localTransactionSequence.size();
					}

					Utils.CalculateTracePerformanceResult result = Utils.calculateTracePerformance(
						t.getElements(),
						entityIDToClusterName,
						0,
						t.getElements() == null ? 0 : t.getElements().size()
					);

					controllerPerformance += result.performance;
				}
		}

		controllerPerformance /= tracesCounter;

		return new GetLocalTransactionsGraphAndControllerPerformanceResult(
			controllerPerformance,
			localTransactionsGraph
		);
	}

	public void addStaticControllers(
		String profile,
		HashMap<String, ControllerDto> datafile
	)
		throws IOException
	{
		System.out.println("Adding static controllers...");

		HashMap<String, ControllerDto> datafileJSON;

		if (datafile == null)
			datafileJSON = CodebaseManager.getInstance().getDatafile(this.codebaseName);
		else
			datafileJSON = datafile;

		Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("profiles"); }}
		);

		List<String> profileControllers = codebase.getProfile(profile);

		for (String controllerName : profileControllers) {
			ControllerDto controllerDto = datafileJSON.get(controllerName);
			List<AccessDto> controllerAccesses = controllerDto.getControllerAccesses();

			Controller controller = new Controller(controllerName);
			if (controllerAccesses.size() > 0) {
//					Controller controller = new Controller(controllerName);
//					this.addController(controller); // FIXME

				List<LocalTransaction> localTransactionsSequence = getLocalTransactionsSequence(
					0, // FIXME probably graph must have a counter for local transactions
					entityIDToClusterName,
					controllerAccesses
				);
			}
		}
	}

	public void addDynamicControllers(
		String profile,
		int tracesMaxLimit,
		Constants.TraceType traceType
	)
		throws IOException
	{
		System.out.println("Adding dynamic controllers...");

		Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("profiles"); add("datafilePath"); }}
		);

		List<String> profileControllers = codebase.getProfile(profile);

		ControllerTracesIterator iter = new ControllerTracesIterator(
			codebase.getDatafilePath(),
			tracesMaxLimit
		);

		for (String controllerName : profileControllers) {
			GetLocalTransactionsGraphAndControllerPerformanceResult result = getLocalTransactionsGraphAndControllerPerformance(
				iter,
				controllerName,
				traceType
			);

			if (controller.getEntities().size() > 0)
				this.addController(controller);
		}
	}

	public void mergeClusters(
		String cluster1,
		String cluster2,
		String newName
	) throws Exception {
		Cluster mergedCluster = new Cluster(newName);

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster1)) {
				mergedCluster.setEntities(clusters.get(i).getEntities());

				clusters.remove(i);

				break;
			}
		}

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {
				mergedCluster.setEntities(clusters.get(i).getEntities());

				clusters.remove(i);

				break;
			}

		}
		this.addCluster(mergedCluster);
		this.calculateMetrics();
	}

	public void renameCluster(String clusterName, String newName) throws Exception {
		if (clusterName.equals(newName))
			return;

		for (Cluster cluster : this.clusters) {
			if (cluster.getName().equals(newName))
				throw new KeyAlreadyExistsException();
		}

		for (Cluster cluster : this.clusters) {
			if (cluster.getName().equals(clusterName)) {
				cluster.setName(newName);
				break;
			}
		}

		this.calculateMetrics();
	}

	public Cluster getCluster(String clusterName) {
		for (Cluster cluster : this.clusters)
			if (cluster.getName().equals(clusterName))
				return cluster;
		return null;
	}

	public String getClusterWithEntity(short entityID) {
		return entityIDToClusterName.get(entityID);
	}

	public void splitCluster(
		String clusterName,
		String newName,
		String[] entities
	)
		throws Exception
	{
		Cluster currentCluster = this.getCluster(clusterName);
		Cluster newCluster = new Cluster(newName);

		for (String stringifiedEntityID : entities) {
			short entityID = Short.parseShort(stringifiedEntityID);

			if (currentCluster.containsEntity(entityID)) {
				newCluster.addEntity(entityID);
				currentCluster.removeEntity(entityID);
			}
		}

		this.addCluster(newCluster);
		this.calculateMetrics();
	}

	public void transferEntities(
		String fromClusterName,
		String toClusterName,
		String[] entities
	)
		throws Exception
	{
		Cluster fromCluster = this.getCluster(fromClusterName);
		Cluster toCluster = this.getCluster(toClusterName);

		for (String stringifiedEntityID : entities) {
			short entityID = Short.parseShort(stringifiedEntityID);

			if (fromCluster.containsEntity(entityID)) {
				toCluster.addEntity(entityID);
				fromCluster.removeEntity(entityID);
			}
		}

		this.calculateMetrics();
	}

	public void calculateMetrics() throws Exception {
		CodebaseManager cm = CodebaseManager.getInstance();

		Codebase codebase = cm.getCodebaseWithFields(
			this.codebaseName,
			new HashSet<String>() {{ add("analysisType"); }}
		);

		Dendrogram d = cm.getCodebaseDendrogramWithFields(
			this.codebaseName,
			this.dendrogramName,
			new HashSet<String>() {{ add("profiles"); add("typeOfTraces"); add("tracesMaxLimit"); }}
		);

		if (codebase.isStatic()) {
			this.addStaticControllers(
				d.getProfiles(),
				null
			);

		} else {
			this.addDynamicControllers(
				d.getProfiles(),
				d.getTracesMaxLimit(),
				d.getTypeOfTraces()
			);
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateAnalyserStaticMetrics(
		String profile,
		HashMap<String, ControllerDto> datafileJSON
	)
		throws IOException
	{
		this.addStaticControllers(
			profile,
			datafileJSON
		);

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateAnalyserDynamicMetrics(
		String profile,
		int tracesMaxLimit,
		Constants.TraceType traceType
	)
		throws IOException
	{
		System.out.println("Calculating Analyser Dynamic metrics...");

		Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("profiles"); add("datafilePath"); }}
		);

		List<String> profileControllers = codebase.getProfile(profile);

		ControllerTracesIterator iter = new ControllerTracesIterator(
			codebase.getDatafilePath(),
			tracesMaxLimit
		);

		for (String controllerName : profileControllers) {
			GetLocalTransactionsGraphAndControllerPerformanceResult result = getLocalTransactionsGraphAndControllerPerformance(
				iter,
				controllerName,
				traceType
			);

		}
		
		
//		this.addDynamicControllers(
//			profile,
//			tracesMaxLimit,
//			traceType
//		);

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
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
}