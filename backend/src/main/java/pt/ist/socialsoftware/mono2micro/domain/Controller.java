package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Controller {
	private String name;
	private List<Entity> entities;
	private List<Pair<Entity,String>> entitiesRW;

	public Controller() {

	}

	public Controller(String name) {
        this.name = name;
		this.entities = new ArrayList<>();
		this.entitiesRW = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Pair<Entity,String>> getEntitiesRW() {
		return this.entitiesRW;
	}

	public void setEntitiesRW(List<Pair<Entity,String>> entitiesRW) {
		this.entitiesRW = entitiesRW;
	}

	public void addEntityRW(String entity, String mode) {
		Pair<Entity,String> newEntity = new Pair<>(new Entity(entity), mode);
		this.entitiesRW.add(newEntity);
	}

	public List<Entity> getEntities() {
		return this.entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public void addEntity(String entity) {
		this.entities.add(new Entity(entity));
	}

	public boolean containsEntity(String entityName) {
		for (Entity entity : this.entities)
			if (entity.getName().equals(entityName))
				return true;
		return false;
	}

}
