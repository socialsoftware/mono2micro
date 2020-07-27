package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Controller {
	private String name;
	private float complexity;
	private Map<String, String> entities = new HashMap<>();
	private String entitiesSeq = "[]";
	private List<LocalTransaction> functionalityRedesign = new ArrayList<>();

	public Controller() {
	}

	public Controller(String name) {
        this.name = name;
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

	public String getEntitiesSeq() {
		return entitiesSeq;
	}

	public void setEntitiesSeq(String entitiesSeq) {
		this.entitiesSeq = entitiesSeq;
	}

	public void addEntitiesSeq(JSONArray entitiesSeq) throws JSONException {
		this.setEntitiesSeq(entitiesSeq.toString());
	}

	public List<LocalTransaction> getFunctionalityRedesign() {
		return functionalityRedesign;
	}

	public void setFunctionalityRedesign(List<LocalTransaction> functionalityRedesign) {
		this.functionalityRedesign = functionalityRedesign;
	}

	public void addFunctionalityRedesign() throws JSONException {
		JSONArray sequence = new JSONArray(this.entitiesSeq);

		for(int i=0; i < sequence.length(); i++){
			LocalTransaction lt = new LocalTransaction(Integer.toString(i),
					sequence.getJSONObject(i).getString("cluster"),
					sequence.getJSONObject(i).getString("sequence"),
					new ArrayList<Integer>());

			this.functionalityRedesign.add(lt);

			if(i > 0) {
				this.functionalityRedesign.get(i-1).getRemoteInvocations().add(i);
			}
		}
	}

	public List<LocalTransaction> addCompensating(String clusterName, String entities, String fromID) {
		int maxID = this.functionalityRedesign.stream().map(lt -> Integer.parseInt(lt.getId())).max(Integer::compare).get();
		LocalTransaction newLT = new LocalTransaction(String.valueOf(maxID+1),clusterName,entities,new ArrayList<Integer>());

		LocalTransaction caller = this.functionalityRedesign.stream().filter(lt -> lt.getId().equals(fromID)).findFirst().orElse(null);

		if(caller != null){
			this.functionalityRedesign.add(newLT);
			caller.getRemoteInvocations().add(maxID+1);
			return this.functionalityRedesign;
		} else {
			return null;
		}
	}
}
