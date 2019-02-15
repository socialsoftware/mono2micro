package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private String name;
	private List<Entity> entities;

	public Cluster() {

	}

	public Cluster(String name) {
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

	public Entity getEntity(String entityName) {
		for (Entity entity : this.entities)
			if (entity.getName().equals(entityName))
				return entity;
		return null;
	}

	public void removeEntity(String entityName) {
		for (Entity entity : this.entities) {
			if (entity.getName().equals(entityName)) {
				this.entities.remove(entity);
				break;
			}
		}
	}

	public boolean containsEntity(String entityName) {
		for (Entity entity : this.entities)
			if (entity.getName().equals(entityName))
				return true;
		return false;
	}

}
