package pt.ist.socialsoftware.mono2micro.domain;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.domain.metrics.MetricFactory;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.FunctionalityDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.FunctionalitySerializer;

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonSerialize(using = FunctionalitySerializer.class)
@JsonDeserialize(using = FunctionalityDeserializer.class)
public class Functionality {
	private String name;
	private FunctionalityType type;
	private List<Metric> metrics = new ArrayList<>();
	private Map<Short, Byte> entities = new HashMap<>(); // <entityID, mode>
	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();
	private Map<Short, Set<Short>> entitiesPerCluster = new HashMap<>();

	@JsonIgnore
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
	) {
		FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);
		functionalityRedesign.setUsedForMetrics(usedForMetrics);

		LocalTransaction graphRootLT = new LocalTransaction(0, (short) -1);

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
				childLT.setName(childLT.getId() + ": " + childLT.getClusterID());
			}

			functionalityRedesign.getRedesign().add(lt);
			if(lt.getId() != 0){
				for(AccessDto accessDto : lt.getClusterAccesses()){
					if(this.entitiesPerCluster.containsKey(lt.getClusterID())){
						this.entitiesPerCluster.get(lt.getClusterID()).add(accessDto.getEntityID());
					} else {
						Set<Short> entities = new HashSet<>();
						entities.add(accessDto.getEntityID());
						this.entitiesPerCluster.put(lt.getClusterID(), entities);
					}
				}
			}
		}

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

	public Set<Short> clustersOfGivenEntities(Set<Short> entities){
		Set<Short> clustersOfGivenEntities = new HashSet<>();
		for(Short clusterID : this.entitiesPerCluster.keySet()){
			for(Short entityID : entities){
				if(this.entitiesPerCluster.get(clusterID).contains(entityID))
					clustersOfGivenEntities.add(clusterID);
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

	public Map<Short, Set<Short>> getEntitiesPerCluster() {
		return entitiesPerCluster;
	}

	public void setEntitiesPerCluster(Map<Short, Set<Short>> entitiesPerCluster) {
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

	public void setupEntities(List<ReducedTraceElementDto> traceElements, Map<Short, Short> entityIDToClusterID) {
		Map<Short, Byte> entityIDToMode = new HashMap<>();
		short previousCluster = -2;
		boolean isFirstAccess = false;

		for (ReducedTraceElementDto rte : traceElements) {
			if (rte instanceof AccessDto) {
				AccessDto access = (AccessDto) rte;
				short entityID = access.getEntityID();
				byte mode = access.getMode();

				Short cluster = entityIDToClusterID.get(entityID);

				if (cluster == null) {
					System.err.println("Entity " + entityID + " is not assign to a cluster.");
					System.exit(-1);
				}

				if (!isFirstAccess) {
					entityIDToMode.put(entityID, mode);
					this.addEntity(entityID, mode);

				} else {
					if (cluster == previousCluster) {
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

				previousCluster = cluster;
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
			String inputFilePath,
			int tracesMaxLimit,
			Constants.TraceType traceType,
			Map<Short, Short> entityIDToClusterID
	) throws IOException {
		if (this.getTraces() == null) {
			FunctionalityTracesIterator iter = new FunctionalityTracesIterator(inputFilePath, tracesMaxLimit);
			iter.nextFunctionalityWithName(this.getName());
			iter.getFirstTrace();

			List<TraceDto> traceDtos = iter.getTracesByType(traceType);
			this.setTraces(traceDtos);
		}

		// Get traces according to trace type
		return createLocalTransactionGraph(entityIDToClusterID);
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> createLocalTransactionGraph(Map<Short, Short> entityIDToClusterID) {
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		localTransactionsGraph.addVertex(new LocalTransaction(0, (short) -1)); // Local transaction's root

		for (TraceDto t : this.getTraces()) {
			List<ReducedTraceElementDto> traceElements = t.getElements();
			this.setupEntities(traceElements, entityIDToClusterID); // Adds entities used by the functionality

			if (traceElements.size() > 0) {
				Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
						1,
						null,
						traceElements,
						entityIDToClusterID,
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

	public static List<LocalTransaction> getNextLocalTransactions(
			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph,
			LocalTransaction lt
	) {
		return successorListOf(localTransactionsGraph, lt);
	}
}