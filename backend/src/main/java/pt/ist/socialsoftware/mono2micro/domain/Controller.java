package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ControllerDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.ControllerSerializer;

import java.util.*;

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonSerialize(using = ControllerSerializer.class)
@JsonDeserialize(using = ControllerDeserializer.class)
public class Controller {
	public static class LocalTransaction {
		private int id; // transaction id
		private String clusterName; // actually is just an Id
		private List<AccessDto> clusterAccesses;

		public LocalTransaction() {}

		public LocalTransaction(int id) {
			this.id = id;
		}

		public LocalTransaction(
			int id,
			String clusterName,
			List<AccessDto> clusterAccesses
		) {
			this.id = id;
			this.clusterName = clusterName;
			this.clusterAccesses = clusterAccesses;
		}

		public LocalTransaction(LocalTransaction lt) {
			this.id = lt.getId();
			this.clusterName = lt.getClusterName();
			this.clusterAccesses = new ArrayList<>(lt.getClusterAccesses());
		}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public String getClusterName() { return clusterName; }
		public void setClusterName(String clusterName) { this.clusterName = clusterName; }
		public List<AccessDto> getClusterAccesses() { return clusterAccesses; }
		public void setClusterAccesses(List<AccessDto> clusterAccesses) { this.clusterAccesses = clusterAccesses; }
		public void addClusterAccess(AccessDto a) { this.clusterAccesses.add(a); }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LocalTransaction that = (LocalTransaction) o;

			return id == that.id;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	private String name;
	private float complexity;
	private Map<String, String> entities = new HashMap<>(); // <entity, mode>
	private DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph;
	private String entitiesSeq = "[]";
	private List<pt.ist.socialsoftware.mono2micro.domain.LocalTransaction> functionalityRedesign = new ArrayList<>();

	public Controller() {}

	public Controller(String name) {
        this.name = name;
		localTransactionsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        localTransactionsGraph.addVertex(new LocalTransaction(0, null, new ArrayList<>()));
	}

	public Controller(
		String name,
		float complexity,
		Map<String, String> entities,
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

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public Map<String,String> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<String,String> entities) {
		this.entities = entities;
	}

	public String getEntitiesSeq() { return entitiesSeq; }

	public void setEntitiesSeq(String entitiesSeq) { this.entitiesSeq = entitiesSeq; }

	public void addEntity(String entity, String mode) {
		if (this.entities.containsKey(entity) && !this.entities.get(entity).equals(mode)) {
			this.entities.put(entity, "RW");
		} else if (!this.entities.containsKey(entity)) {
			this.entities.put(entity, mode);
		}
	}

	public boolean containsEntity(String entity) {
		return this.entities.containsKey(entity);
	}

	public DirectedAcyclicGraph<LocalTransaction, DefaultEdge> getLocalTransactionsGraph() { return localTransactionsGraph; }

	public void addLocalTransactionSequence(List<LocalTransaction> localTransactions) {
		LocalTransaction current_lt = new LocalTransaction(0);

		for (int i = 0; i < localTransactions.size(); i++) {
			List<LocalTransaction> childrenLts = successorListOf(this.localTransactionsGraph, current_lt);

			int childrenLtsSize = childrenLts.size();

			if (childrenLtsSize == 0) {
				createNewBranch(localTransactions, current_lt, i);
				return;
			}

			for (int j = 0; j < childrenLtsSize; j++) {
				LocalTransaction childLt = childrenLts.get(j);

				if (localTransactions.get(i).equals(childLt)) {
					current_lt = childLt;
					break;

				} else {
					if (j == childrenLtsSize - 1) {
						createNewBranch(localTransactions, current_lt, i);
						return;
					}
				}
			}
		}
	}

	private void createNewBranch(
		List<LocalTransaction> localTransactions,
		LocalTransaction current_lt,
		int i
	) {
		for (int k = i; k < localTransactions.size(); k++) {
			this.localTransactionsGraph.addVertex(localTransactions.get(k));
			this.localTransactionsGraph.addEdge(current_lt, localTransactions.get(k));
			current_lt = localTransactions.get(k);
		}
	}

	public Set<LocalTransaction> getAllLocalTransactions() {
		return localTransactionsGraph.vertexSet();
	}

	public List<LocalTransaction> getNextLocalTransactions(LocalTransaction lt) {
		return successorListOf(localTransactionsGraph, lt);
	}

	public void setLocalTransactionsGraph(DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
		this.localTransactionsGraph = localTransactionsGraph;
    }
    
	public void addEntitiesSeq(JSONArray entitiesSeq) throws JSONException {
		this.setEntitiesSeq(entitiesSeq.toString());
	}

	public List<pt.ist.socialsoftware.mono2micro.domain.LocalTransaction> getFunctionalityRedesign() {
		return functionalityRedesign;
	}

	public void setFunctionalityRedesign(List<pt.ist.socialsoftware.mono2micro.domain.LocalTransaction> functionalityRedesign) {
		this.functionalityRedesign = functionalityRedesign;
	}

	public void addFunctionalityRedesign() throws JSONException {
		JSONArray sequence = new JSONArray(this.entitiesSeq);

		for(int i=0; i < sequence.length(); i++){
			pt.ist.socialsoftware.mono2micro.domain.LocalTransaction lt = new pt.ist.socialsoftware.mono2micro.domain.LocalTransaction(
				Integer.toString(i),
				sequence.getJSONObject(i).getString("cluster"),
				sequence.getJSONObject(i).getString("sequence"),
				new ArrayList<>()
			);

			this.functionalityRedesign.add(lt);

			if(i > 0) {
				this.functionalityRedesign.get(i-1).getRemoteInvocations().add(i);
			}
		}
	}

	public List<pt.ist.socialsoftware.mono2micro.domain.LocalTransaction> addCompensating(String clusterName, String entities, String fromID) {
		int maxID = this.functionalityRedesign.stream().map(lt -> Integer.parseInt(lt.getId())).max(Integer::compare).get();

		pt.ist.socialsoftware.mono2micro.domain.LocalTransaction newLT = new pt.ist.socialsoftware.mono2micro.domain.LocalTransaction(
			String.valueOf(maxID+1),
			clusterName,
			entities,
			new ArrayList<>()
		);

		pt.ist.socialsoftware.mono2micro.domain.LocalTransaction caller = this.functionalityRedesign
			.stream()
			.filter(lt -> lt.getId().equals(fromID))
			.findFirst()
			.orElse(null);

		if(caller != null){
			this.functionalityRedesign.add(newLT);
			caller.getRemoteInvocations().add(maxID+1);
			return this.functionalityRedesign;
		} else {
			return null;
		}
	}
}
