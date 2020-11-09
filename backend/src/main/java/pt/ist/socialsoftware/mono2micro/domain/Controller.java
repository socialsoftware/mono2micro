package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.ControllerType;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ControllerDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.ControllerSerializer;

import static org.jgrapht.Graphs.successorListOf;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonSerialize(using = ControllerSerializer.class)
@JsonDeserialize(using = ControllerDeserializer.class)
public class Controller {
	private String name;
	private float complexity;
	private float performance; // the average of the number of hops between clusters for all traces
	private Map<Short, Byte> entities = new HashMap<>(); // <entityID, mode>
	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();
	private ControllerType type;
	private Map<Short, Set<Short>> entitiesPerCluster = new HashMap<>();

	public Controller() {}

	public Controller(String name) {
        this.name = name;
	}

	public Controller(
		String name,
		float complexity,
		Map<Short, Byte> entities
	) {
		this.name = name;
		this.complexity = complexity;
		this.entities = entities;
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

	public float getPerformance() {
		return performance;
	}

	public void setPerformance(float performance) {
		this.performance = performance;
	}

	public Map<Short, Byte> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<Short, Byte> entities) {
		this.entities = entities;
	}

	public void addEntity(
		short entityID,
		byte mode
	) {
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


	public void createFunctionalityRedesign(
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

		this.functionalityRedesigns.add(0, functionalityRedesign);
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

	public ControllerType getType() {
		return type;
	}

	public void setType(ControllerType type) {
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

	public ControllerType defineControllerType(){
		if(this.type != null) return this.type;

		if(!this.entities.isEmpty()){
			for(Short entity : this.entities.keySet()){
				if(this.entities.get(entity) >= 2) { // 2 -> W , 3 -> RW
					this.type = ControllerType.SAGA;
					return this.type;
				}

			}
			this.type = ControllerType.QUERY;
		}
		return this.type;
	}

	public boolean containsEntity(Short entity) {
		return this.entities.containsKey(entity);
	}
}
