package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Controller {
	private String name;
	private List<Entity> entities;
	private List<Pair<Entity,String>> entitiesRW;
	private List<Pair<Entity,String>> entitiesRWseq;

	public Controller() {

	}

	public Controller(String name) {
        this.name = name;
		this.entities = new ArrayList<>();
		this.entitiesRW = new ArrayList<>();
		this.entitiesRWseq = new ArrayList<>();
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
		boolean containsEntity = false;
		for (Pair<Entity,String> p : this.entitiesRW) {
			if (p.getFirst().getName().equals(entity)) {
				containsEntity = true;
				if (p.getSecond().equals("R") && mode.equals("W")) p.setSecond("RW");
				if (p.getSecond().equals("W") && mode.equals("R")) p.setSecond("RW");
				break;
			}
		}

		if (!containsEntity) {
			Pair<Entity,String> newEntity = new Pair<>(new Entity(entity), mode);
			this.entitiesRW.add(newEntity);
		}
	}

	public List<Pair<Entity,String>> getEntitiesRWseq() {
		return this.entitiesRWseq;
	}

	public void setEntitiesRWseq(List<Pair<Entity,String>> entitiesRWseq) {
		this.entitiesRWseq = entitiesRWseq;
	}

	public void addEntityRWseq(String entity, String mode) {
		Pair<Entity,String> newEntity = new Pair<>(new Entity(entity), mode);
		this.entitiesRWseq.add(newEntity);
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
