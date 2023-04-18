package pt.ist.socialsoftware.mono2micro.functionality.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics.FunctionalityComplexityMetricCalculator;
import pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics.FunctionalityMetricCalculator;
import pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics.FunctionalityPerformanceMetricCalculator;
import pt.ist.socialsoftware.mono2micro.utils.*;

import static org.jgrapht.Graphs.successorListOf;

@Document("functionality")
public class Functionality {
	@Id
	private String id;
	private String name;
	private FunctionalityType type;
	private Map<String, Object> metrics = new HashMap<>();
	private Map<Short, Byte> entities = new HashMap<>(); // <entityID, mode>
	private Map<String, String> functionalityRedesigns = new HashMap<>(); // <redesignName, redesignFileName>
	private String functionalityRedesignNameUsedForMetrics;
	private Map<String, Set<Short>> entitiesPerCluster = new HashMap<>();

	@JsonIgnore
	@Transient
	private List<TraceDto> traces;

	public Functionality() {}

	public Functionality(String decompositionName, String name) {
		this.id = decompositionName + " & " + name.replace(".", "_");
        this.name = name;
	}

	public Functionality(String decompositionName, Functionality functionality) { // Useful when redesigns are not needed
		this.id = decompositionName + " & " + functionality.getName().replace(".", "_");
		this.name = functionality.getName();
		this.type = functionality.getType();
		this.metrics = functionality.getMetrics();
		this.entities = functionality.getEntities();
		this.entitiesPerCluster = functionality.getEntitiesPerCluster();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getMetrics() {
		return metrics;
	}

	public Object getMetric(String metricType) {
		return this.metrics.get(metricType);
	}

	public void setMetrics(Map<String, Object> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(String metricType, Object metricValue) {
		this.metrics.put(metricType, metricValue);
	}

	public Map<Short, Byte> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<Short, Byte> entities) {
		this.entities = entities;
	}

	public void addEntity(short entityID, byte mode) {
		Byte savedMode = this.entities.get(entityID);

		if (savedMode != null) {
			if (savedMode != mode && savedMode != 3) // "RW" -> 3
				this.entities.put(entityID, (byte) 3); // "RW" -> 3
		} else {
			this.entities.put(entityID, mode);
		}
	}

	public boolean containsEntity(short entity) {
		return this.entities.containsKey(entity);
	}

	public Map<String, String> getFunctionalityRedesigns() { return functionalityRedesigns; }

	public void setFunctionalityRedesigns(Map<String, String> functionalityRedesigns) {
		this.functionalityRedesigns = functionalityRedesigns;
	}

	public void addFunctionalityRedesign(String functionalityRedesign, String fileName) {
		this.functionalityRedesigns.put(functionalityRedesign, fileName);
	}

	public String getFunctionalityRedesignFileName(String redesignName) {
		return this.functionalityRedesigns.get(redesignName);
	}

	public String getFunctionalityRedesignNameUsedForMetrics() {
		return functionalityRedesignNameUsedForMetrics;
	}

	public void setFunctionalityRedesignNameUsedForMetrics(String functionalityRedesignNameUsedForMetrics) {
		this.functionalityRedesignNameUsedForMetrics = functionalityRedesignNameUsedForMetrics;
	}

	public boolean containsFunctionalityRedesignName(String name) {
		return this.functionalityRedesigns.containsKey(name);
	}

	public void removeFunctionalityRedesign(String redesignName) {
		this.functionalityRedesigns.remove(redesignName);
		if (this.functionalityRedesignNameUsedForMetrics.equals(redesignName))
			this.functionalityRedesignNameUsedForMetrics = new ArrayList<>(this.getFunctionalityRedesigns().keySet()).get(0);
	}

	public FunctionalityType getType() {
		return type;
	}

	public void setType(FunctionalityType type) {
		this.type = type;
	}

	public Set<Short> entitiesTouchedInAGivenMode(byte mode){
		Set<Short> entitiesTouchedInAGivenMode = new HashSet<>();
		for (Map.Entry<Short, Byte> entry: this.entities.entrySet()){
			if (entry.getValue() == 3 || entry.getValue() == mode) // 3 -> RW
				entitiesTouchedInAGivenMode.add(entry.getKey());
		}
		return entitiesTouchedInAGivenMode;
	}

	public Set<String> clustersOfGivenEntities(Set<Short> entities){
		Set<String> clustersOfGivenEntities = new HashSet<>();
		for (Map.Entry<String, Set<Short>> entry : this.entitiesPerCluster.entrySet()){
			for (Short entityID : entities){
				if (entry.getValue().contains(entityID))
					clustersOfGivenEntities.add(entry.getKey());
			}
		}
		return clustersOfGivenEntities;
	}

	public FunctionalityType defineFunctionalityType(){
		if (this.type != null) return this.type;

		if (!this.entities.isEmpty()){
			for (Map.Entry<Short, Byte> entry : this.entities.entrySet()){
				if (entry.getValue() >= 2) { // 2 -> W , 3 -> RW
					this.type = FunctionalityType.SAGA;
					return this.type;
				}

			}
			this.type = FunctionalityType.QUERY;
		}
		return this.type;
	}

	public boolean containsEntity(Short entity) {
		return this.entities.containsKey(entity);
	}

	public Map<String, Set<Short>> getEntitiesPerCluster() {
		return entitiesPerCluster;
	}

	public void setEntitiesPerCluster(Map<String, Set<Short>> entitiesPerCluster) {
		this.entitiesPerCluster = entitiesPerCluster;
	}

	public void setupEntities(List<ReducedTraceElementDto> traceElements, Map<Short, String> entityIDToClusterName) {
		Map<Short, Byte> entityIDToMode = new HashMap<>();
		String previousCluster = "-2";
		boolean isFirstAccess = false;

		for (ReducedTraceElementDto rte : traceElements) {
			if (rte instanceof AccessDto) {
				AccessDto access = (AccessDto) rte;
				short entityID = access.getEntityID();
				byte mode = access.getMode();

				String clusterName = entityIDToClusterName.get(entityID);

				if (clusterName == null) {
					System.err.println("Entity " + entityID + " is not assign to a cluster.");
					System.exit(-1);
				}

				if (!isFirstAccess) {
					entityIDToMode.put(entityID, mode);
					this.addEntity(entityID, mode);

				} else {
					if (clusterName.equals(previousCluster)) {
						Byte savedMode = entityIDToMode.get(entityID);

						if (savedMode == null || savedMode == 1 && mode == 2) { // "R" -> 1, "W" -> 2
							entityIDToMode.put(entityID, mode);
							this.addEntity(entityID, mode);
						}

					} else {
						this.addEntity(entityID, mode);

						entityIDToMode.clear();
						entityIDToMode.put(entityID, mode);
					}
				}

				previousCluster = clusterName;
				isFirstAccess = true;
			}
		}
	}

	public List<TraceDto> getTraces() {
		return traces;
	}

	public void setTraces(List<TraceDto> traces) {
		this.traces = traces;
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> createLocalTransactionGraphFromScratch(
			InputStream inputFilePath,
			int tracesMaxLimit,
			Constants.TraceType traceType,
			Map<Short, String> entityIDToClusterName
	) throws IOException, JSONException {
		if (this.getTraces() == null) {
			FunctionalityTracesIterator iter = new FunctionalityTracesIterator(inputFilePath, tracesMaxLimit);
			iter.getFunctionalityWithName(this.getName());

			List<TraceDto> traceDtos = iter.getTracesByType(traceType);
			this.setTraces(traceDtos);
		}

		// Get traces according to trace type
		return createLocalTransactionGraph(entityIDToClusterName);
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> createLocalTransactionGraph(Map<Short, String> entityIDToClusterName) {
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		localTransactionsGraph.addVertex(new LocalTransaction(0, "-1")); // Local transaction's root

		for (TraceDto t : this.getTraces()) {
			List<ReducedTraceElementDto> traceElements = t.getElements();
			this.setupEntities(traceElements, entityIDToClusterName); // Adds entities used by the functionality

			if (traceElements.size() > 0) {
				Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
						1,
						null,
						traceElements,
						entityIDToClusterName,
						new HashMap<>(),
						0,
						traceElements.size());

				addLocalTransactionsSequenceToGraph(
						localTransactionsGraph,
						result.localTransactionsSequence);
			}
		}

		return localTransactionsGraph;
	}

	public static void addLocalTransactionsSequenceToGraph(
			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
			List<LocalTransaction> localTransactionSequence
	) {
		LocalTransaction graphCurrentLT = new LocalTransaction(0, "-1"); // root

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

				if (sequenceCurrentLT.getClusterName().equals(graphChildLT.getClusterName())) {
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

	public static List<LocalTransaction> getNextLocalTransactions(
			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
			LocalTransaction lt
	) {
		return successorListOf(localTransactionsGraph, lt);
	}

	public void calculateMetrics(AccessesInformation accessesInformation, Decomposition decomposition) throws Exception {
		FunctionalityMetricCalculator[] metricObjects = new FunctionalityMetricCalculator[] {new FunctionalityComplexityMetricCalculator(), new FunctionalityPerformanceMetricCalculator()};

		for (FunctionalityMetricCalculator metric : metricObjects)
			this.metrics.put(metric.getType(), metric.calculateMetric(accessesInformation, decomposition, this));
	}
}