package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
	private String name;
	private Map<String,String> entities;
	private List<Pair<String,String>> entitiesSeq;

	public Controller() {

	}

	public Controller(String name) {
        this.name = name;
		this.entities = new HashMap<>();
		this.entitiesSeq = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String,String> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<String,String> entities) {
		this.entities = entities;
	}

	public void addEntity(String entity, String mode) {
		if (this.entities.containsKey(entity)) {
			if (!this.entities.get(entity).contains(mode)) 
				this.entities.put(entity, "RW");
		} else {
			this.entities.put(entity, mode);
		}
	}

	public boolean containsEntity(String entity) {
		return this.entities.containsKey(entity);
	}

	public List<Pair<String,String>> getEntitiesSeq() {
		return this.entitiesSeq;
	}

	public void setEntitiesSeq(List<Pair<String,String>> entitiesSeq) {
		this.entitiesSeq = entitiesSeq;
	}

	public void addEntitySeq(String entity, String mode) {
		this.entitiesSeq.add(new Pair<>(entity, mode));
	}
}
