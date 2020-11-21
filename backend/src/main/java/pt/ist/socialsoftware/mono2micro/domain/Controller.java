package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ControllerDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.ControllerSerializer;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonSerialize(using = ControllerSerializer.class)
@JsonDeserialize(using = ControllerDeserializer.class)
public class Controller {
	private String name;
	private ControllerType type;
	private float complexity;
	private float performance; // the average of the number of hops between clusters for all traces
	private Map<Short, Byte> entities = new HashMap<>(); // <entityID, mode>
//	@JsonIgnore
//	private String entitiesSeq = "[]";
//	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();

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

//	public String getEntitiesSeq() { return entitiesSeq; }
//
//	public void setEntitiesSeq(String entitiesSeq) { this.entitiesSeq = entitiesSeq; }

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

//	public List<FunctionalityRedesign> getFunctionalityRedesigns() {
//		return functionalityRedesigns;
//	}
//
//	public void setFunctionalityRedesigns(List<FunctionalityRedesign> functionalityRedesigns) {
//		this.functionalityRedesigns = functionalityRedesigns;
//	}

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
