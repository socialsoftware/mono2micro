package pt.ist.socialsoftware.mono2micro.functionality.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.io.IOUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.json.JSONException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.FileManager;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.metrics.MetricFactory;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.*;

import static org.jgrapht.Graphs.successorListOf;

// TODO talvez isto possa ser removido
@Document("functionality")
public class Functionality {
	@Id
	private String id;
	private String name;
	private FunctionalityType type;
	private List<Metric> metrics = new ArrayList<>();
	private Map<Short, Byte> entities = new HashMap<>(); // <entityID, mode>
	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();
	private Map<String, Set<Short>> entitiesPerCluster = new HashMap<>();

	@JsonIgnore
	@Transient
	private List<TraceDto> traces;

	public Functionality() {}

	public Functionality(String name) {
        this.name = name;
	}

	public Functionality(Functionality functionality) { // Useful when redesigns are not needed
		this.name = functionality.getName();
		this.type = functionality.getType();
		this.metrics = functionality.getMetrics();
		this.entities = functionality.getEntities();
		this.entitiesPerCluster = functionality.getEntitiesPerCluster();
	}

	private static final String[] availableMetrics = {
			Metric.MetricType.COMPLEXITY,
			Metric.MetricType.PERFORMANCE
	};

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

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(Metric metric) {
		this.metrics.add(metric);
	}

	public Metric searchMetricByType(String metricType) {
		for (Metric metric: this.getMetrics())
			if (metric.getType().equals(metricType))
				return metric;
		return null;
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

	public List<FunctionalityRedesign> getFunctionalityRedesigns() { return functionalityRedesigns; }

	public void setFunctionalityRedesigns(List<FunctionalityRedesign> functionalityRedesigns) {
		this.functionalityRedesigns = functionalityRedesigns;
	}


	public FunctionalityRedesign createFunctionalityRedesign(
		String name,
		boolean usedForMetrics,
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
	) throws IOException {
		FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);
		functionalityRedesign.setUsedForMetrics(usedForMetrics);

		LocalTransaction graphRootLT = new LocalTransaction(0, "-1");

		graphRootLT.setName(this.name);

		Iterator<LocalTransaction> iterator = new BreadthFirstIterator<>(
			localTransactionsGraph,
			graphRootLT
		);

		while (iterator.hasNext()) {
			LocalTransaction lt = iterator.next();
			lt.setRemoteInvocations(new ArrayList<>());

			List<LocalTransaction> graphChildrenLTs = successorListOf(
				localTransactionsGraph,
				lt
			);

			for (LocalTransaction childLT : graphChildrenLTs) {
				lt.addRemoteInvocations(childLT.getId());
				childLT.setName(childLT.getId() + ": " + childLT.getClusterName());
			}

			functionalityRedesign.getRedesign().add(lt);
			if(lt.getId() != 0){
				for(AccessDto accessDto : lt.getClusterAccesses()){
					if(this.entitiesPerCluster.containsKey(lt.getClusterName())){
						this.entitiesPerCluster.get(lt.getClusterName()).add(accessDto.getEntityID());
					} else {
						Set<Short> entities = new HashSet<>();
						entities.add(accessDto.getEntityID());
						this.entitiesPerCluster.put(lt.getClusterName(), entities);
					}
				}
			}
		}

		System.out.println(IOUtils.toString(FileManager.getInstance().getFunctionalityRedesignAsJSON(functionalityRedesign), StandardCharsets.UTF_8));
		this.functionalityRedesigns.add(0, functionalityRedesign);
		return functionalityRedesign;
	}

	public FunctionalityRedesign getFunctionalityRedesign(String redesignName){
		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(redesignName)).findFirst().orElse(null);
	}

	public boolean changeFunctionalityRedesignName(String oldName, String newName){
		FunctionalityRedesign functionalityRedesign = this.functionalityRedesigns
			.stream()
			.filter(fr -> fr.getName().equals(oldName))
			.findFirst()
			.orElse(null);

		functionalityRedesign.setName(newName);
		return true;
	}

	public FunctionalityRedesign frUsedForMetrics(){
		for(FunctionalityRedesign fr : this.getFunctionalityRedesigns()){
			if(fr.isUsedForMetrics()) return fr;
		}
		return null;
	}

	public boolean checkNameValidity(String name){
		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(name)).findFirst().orElse(null) == null;
	}

	public void deleteRedesign(String redesignName){
		if(this.functionalityRedesigns.removeIf(fr -> fr.getName().equals(redesignName))){
			this.functionalityRedesigns.get(0).setUsedForMetrics(true);
		}
	}

	public void changeFRUsedForMetrics(String redesignName){
		for(FunctionalityRedesign fr : this.getFunctionalityRedesigns()) {
			if (fr.isUsedForMetrics())
				fr.setUsedForMetrics(false);
			else if (fr.getName().equals(redesignName))
				fr.setUsedForMetrics(true);
		}
	}

	public FunctionalityType getType() {
		return type;
	}

	public void setType(FunctionalityType type) {
		this.type = type;
	}

	public Set<Short> entitiesTouchedInAGivenMode(byte mode){
		Set<Short> entitiesTouchedInAGivenMode = new HashSet<>();
		for(Short entity : this.entities.keySet()){
			if(this.entities.get(entity) == 3 || this.entities.get(entity) == mode) // 3 -> RW
				entitiesTouchedInAGivenMode.add(entity);
		}
		return entitiesTouchedInAGivenMode;
	}

	public Set<String> clustersOfGivenEntities(Set<Short> entities){
		Set<String> clustersOfGivenEntities = new HashSet<>();
		for(String clusterName : this.entitiesPerCluster.keySet()){
			for(Short entityID : entities){
				if(this.entitiesPerCluster.get(clusterName).contains(entityID))
					clustersOfGivenEntities.add(clusterName);
			}
		}
		return clustersOfGivenEntities;
	}

	public FunctionalityType defineFunctionalityType(){
		if(this.type != null) return this.type;

		if(!this.entities.isEmpty()){
			for(Short entity : this.entities.keySet()){
				if(this.entities.get(entity) >= 2) { // 2 -> W , 3 -> RW
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

	public void calculateMetrics(Decomposition decomposition) throws Exception {
		for(String metricType: availableMetrics) {
			Metric metric = searchMetricByType(metricType);
			if (metric == null) {
				metric = MetricFactory.getFactory().getMetric(metricType);
				this.addMetric(metric);
			}
			metric.calculateMetric(decomposition, this);
		}
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
}