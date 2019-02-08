package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Controller {
	private String name;
	private List<Entity> entities;

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

	public List<Entity> getEntities() {
		return this.entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

}
