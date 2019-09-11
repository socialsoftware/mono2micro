package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.socialsoftware.mono2micro.utils.Pair;

public class Controller {
	private String name;
	private Map<String,String> entities = new HashMap<>();
	private List<Pair<String,String>> entitiesSeq = new ArrayList<>();
	private float complexity;
	private float complexityRW;
	private float complexitySeq;

	public Controller() {
	}

	public Controller(String name) {
        this.name = name;
	}

	public float getComplexitySeq() {
		return complexitySeq;
	}

	public void setComplexitySeq(float complexitySeq) {
		this.complexitySeq = complexitySeq;
	}

	public float getComplexityRW() {
		return complexityRW;
	}

	public void setComplexityRW(float complexityRW) {
		this.complexityRW = complexityRW;
	}

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
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
