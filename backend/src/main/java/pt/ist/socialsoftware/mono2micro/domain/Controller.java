package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;

public class Controller {
	private String name;
	private float complexity;
	private Map<String, String> entities = new HashMap<>();
	private String entitiesSeq = "[]";
	private List<FunctionalityRedesign> functionalityRedesigns = new ArrayList<>();

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

	public void addEntitiesSeq(JSONArray entitiesSeq){
		this.setEntitiesSeq(entitiesSeq.toString());
	}

	public List<FunctionalityRedesign> getFunctionalityRedesigns() {
		return functionalityRedesigns;
	}

	public void setFunctionalityRedesigns(List<FunctionalityRedesign> functionalityRedesigns) {
		this.functionalityRedesigns = functionalityRedesigns;
	}

	public void createFunctionalityRedesign(String name) throws JSONException {
		FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);

		JSONArray sequence = new JSONArray(this.entitiesSeq);
		LocalTransaction lt = new LocalTransaction(Integer.toString(-1), this.name,
				"", new ArrayList<>(), this.name );
		lt.getRemoteInvocations().add(0);
		functionalityRedesign.getRedesign().add(lt);

		for(int i=0; i < sequence.length(); i++){
			lt = new LocalTransaction(Integer.toString(i),
					sequence.getJSONObject(i).getString("cluster"),
					sequence.getJSONObject(i).getString("sequence"),
					new ArrayList<Integer>(),
					i + ": " + sequence.getJSONObject(i).getString("cluster"));

			functionalityRedesign.getRedesign().add(lt);

			if(i > 0) {
				functionalityRedesign.getRedesign().get(i).getRemoteInvocations().add(i);
			}
		}
		this.functionalityRedesigns.add(0,functionalityRedesign);
	}

	public FunctionalityRedesign getFunctionalityRedesign(String redesignName){
		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(redesignName)).findFirst().orElse(null);
	}

	public boolean changeFunctionalityRedesignName(String oldName, String newName){
		FunctionalityRedesign functionalityRedesign = this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(oldName)).findFirst().orElse(null);
		functionalityRedesign.setName(newName);
		return true;
	}

	public boolean checkNameValidity(String name){
		return this.functionalityRedesigns.stream().filter(fr -> fr.getName().equals(name)).findFirst().orElse(null) == null;
	}

	public void deleteRedesign(String redesignName){
		this.functionalityRedesigns.removeIf(fr -> fr.getName().equals(redesignName));
	}
}
