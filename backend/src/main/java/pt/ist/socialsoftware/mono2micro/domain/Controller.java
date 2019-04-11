package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Controller {
	private String name;
	private List<String> entities;
	private float complexity;

	public Controller() {

	}

	public Controller(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getComplexity() {
		return this.complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public List<String> getEntities() {
		return this.entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public void addEntity(String entity) {
		this.entities.add(entity);
	}

}
