package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ControllerDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.ControllerSerializer;
import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonSerialize(using = ControllerSerializer.class)
@JsonDeserialize(using = ControllerDeserializer.class)
public class Controller {
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
	private float complexity;
	private int performance; // number of hops between clusters
	private Map<Short, String> entities = new HashMap<>(); // <entityID, mode>
	private DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph;
	@JsonIgnore
	private int localTransactionCounter;
//	private String entitiesSeq = "[]";
//	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();

	public Controller() {}

	public Controller(String name) {
        this.name = name;
		localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        localTransactionsGraph.addVertex( // root
        	new LocalTransaction(
        		0,
				(short) -1
			)
		);
        this.localTransactionCounter = 1;
	}

	public Controller(
		String name,
		float complexity,
		Map<Short, String> entities,
		DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
	) {
		this.name = name;
		this.complexity = complexity;
		this.entities = entities;
		this.localTransactionsGraph = localTransactionsGraph;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public int getLocalTransactionCounter() { return localTransactionCounter; }

	public void setLocalTransactionCounter(int localTransactionCounter) { this.localTransactionCounter = localTransactionCounter; }

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public int getPerformance() {
		return performance;
	}

	public void setPerformance(int performance) {
		this.performance = performance;
	}

	public Map<Short, String> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<Short, String> entities) {
		this.entities = entities;
	}

//	public String getEntitiesSeq() { return entitiesSeq; }
//
//	public void setEntitiesSeq(String entitiesSeq) { this.entitiesSeq = entitiesSeq; }

	public void addEntity(short entityID, String mode) {
		if (this.entities.containsKey(entityID) && !this.entities.get(entityID).equals(mode)) {
			this.entities.put(entityID, "RW");
		} else if (!this.entities.containsKey(entityID)) {
			this.entities.put(entityID, mode);
		}
	}

	public boolean containsEntity(short entity) {
		return this.entities.containsKey(entity);
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> getLocalTransactionsGraph() { return localTransactionsGraph; }

	public void addLocalTransactionSequence(List<LocalTransaction> localTransactionSequence) {
		this.setLocalTransactionCounter(localTransactionSequence.size());

		LocalTransaction graphCurrentLT = new LocalTransaction(0, (short) -1); // root

		for (int i = 0; i < localTransactionSequence.size(); i++) {
			List<LocalTransaction> graphChildrenLTs = getNextLocalTransactions(graphCurrentLT);

			int graphChildrenLTsSize = graphChildrenLTs.size();

			if (graphChildrenLTsSize == 0) {
				createNewBranch(
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

	private void createNewBranch(
		List<LocalTransaction> localTransactions,
		LocalTransaction currentLT,
		int i
	) {
		for (int k = i; k < localTransactions.size(); k++) {
			LocalTransaction lt = localTransactions.get(k);

			this.localTransactionsGraph.addVertex(lt);
			this.localTransactionsGraph.addEdge(currentLT, lt);
			currentLT = lt;
		}
	}

	public Set<LocalTransaction> getAllLocalTransactions() {
		return localTransactionsGraph.vertexSet();
	}

	public List<LocalTransaction> getNextLocalTransactions(LocalTransaction lt) {
		return successorListOf(localTransactionsGraph, lt);
	}

//	public List<FunctionalityRedesign> getFunctionalityRedesigns() {
//		return functionalityRedesigns;
//	}
//
//	public void setFunctionalityRedesigns(List<FunctionalityRedesign> functionalityRedesigns) {
//		this.functionalityRedesigns = functionalityRedesigns;
//	}

	public void setLocalTransactionsGraph(DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
		this.localTransactionsGraph = localTransactionsGraph;
    }
    
//	public void addEntitiesSeq(JSONArray entitiesSeq) throws JSONException {
//		this.setEntitiesSeq(entitiesSeq.toString());
//	}

//	public void createFunctionalityRedesign(String name, boolean usedForMetrics) throws JSONException {
//		FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);
//		functionalityRedesign.setUsedForMetrics(usedForMetrics);
//
//		JSONArray sequence = new JSONArray(this.entitiesSeq);
//		pt.ist.socialsoftware.mono2micro.domain.LocalTransaction lt = new pt.ist.socialsoftware.mono2micro.domain.LocalTransaction(
//				Integer.toString(-1),
//				this.name,
//				"",
//				new ArrayList<>(),
//				this.name
//		);
//
//		lt.getRemoteInvocations().add(0);
//		functionalityRedesign.getRedesign().add(lt);
//
//		for(int i=0; i < sequence.length(); i++){
//			lt = new pt.ist.socialsoftware.mono2micro.domain.LocalTransaction(
//				Integer.toString(i),
//				sequence.getJSONObject(i).getString("cluster"),
//				sequence.getJSONObject(i).getString("sequence"),
//				new ArrayList<>(),
//					i + ": " + sequence.getJSONObject(i).getString("cluster")
//			);
//
//			functionalityRedesign.getRedesign().add(lt);
//
//			if(i > 0) {
//				functionalityRedesign.getRedesign().get(i).getRemoteInvocations().add(i);
//			}
//		}
//		this.functionalityRedesigns.add(0,functionalityRedesign);
//	}
//
//	public FunctionalityRedesign getFunctionalityRedesign(String redesignName){
//		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(redesignName)).findFirst().orElse(null);
//	}
//
//	public boolean changeFunctionalityRedesignName(String oldName, String newName){
//		FunctionalityRedesign functionalityRedesign = this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(oldName)).findFirst().orElse(null);
//		functionalityRedesign.setName(newName);
//		return true;
//	}
//
//	public FunctionalityRedesign frUsedForMetrics(){
//		for(FunctionalityRedesign fr : this.getFunctionalityRedesigns()){
//			if(fr.isUsedForMetrics()) return fr;
//		}
//		return null;
//	}
//
//	public boolean checkNameValidity(String name){
//		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(name)).findFirst().orElse(null) == null;
//	}
//
//	public void deleteRedesign(String redesignName){
//		if(this.functionalityRedesigns.removeIf(fr -> fr.getName().equals(redesignName))){
//			this.functionalityRedesigns.get(0).setUsedForMetrics(true);
//		}
//	}
//
//	public void changeFRUsedForMetrics(String redesignName){
//		for(FunctionalityRedesign fr : this.getFunctionalityRedesigns()) {
//			if (fr.isUsedForMetrics())
//				fr.setUsedForMetrics(false);
//			else if (fr.getName().equals(redesignName))
//				fr.setUsedForMetrics(true);
//		}
//	}
}
