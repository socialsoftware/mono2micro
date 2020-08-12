package pt.ist.socialsoftware.mono2micro.domain;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class Controller {
	private String name;
	private float complexity;
	private Map<String, String> entities = new HashMap<>(); // <entity, mode>
	private String entitiesSeq = "[]";

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

	public void addEntitiesSeq(JSONArray entitiesSeq) {
		this.setEntitiesSeq(entitiesSeq.toString());
	}
}
