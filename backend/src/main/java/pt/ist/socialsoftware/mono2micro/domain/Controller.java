package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.socialsoftware.mono2micro.utils.Pair;

public class Controller {
	private String name;
	private float complexity;
	private float complexityRW;
	private float complexitySeq;
	private float complexity2;
	private Map<String, String> entities = new HashMap<>();
	private List<Pair<String, String>> entitiesSeq = new ArrayList<>();

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

	public float getComplexityRW() {
		return complexityRW;
	}

	public void setComplexityRW(float complexityRW) {
		this.complexityRW = complexityRW;
	}

	public float getComplexitySeq() {
		return complexitySeq;
	}

	public void setComplexitySeq(float complexitySeq) {
		this.complexitySeq = complexitySeq;
	}

	public float getComplexity2() {
		return complexity2;
	}

	public void setComplexity2(float complexity2) {
		this.complexity2 = complexity2;
	}

	public Map<String,String> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<String,String> entities) {
		this.entities = entities;
	}

	public void addEntity(String entity, String mode) {
		if (this.entities.containsKey(entity) &&
			mode.equals("W") &&
			this.entities.get(entity).equals("R")) {
				this.entities.put(entity, "RW");
			}
		else if (!this.entities.containsKey(entity)) {
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
		for (Pair<String,String> entityPair : this.entitiesSeq) {
			String entitySeq = entityPair.getFirst();
			String modeSeq = entityPair.getSecond();

			if (entitySeq.equals(entity) && modeSeq.equals(mode))
				return;

			if (entitySeq.equals(entity) && modeSeq.equals("W"))
				return;
		}

		this.entitiesSeq.add(new Pair<>(entity, mode));
	}
}
