package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private String name;
	private List<Entity> entities;
	private float complexity;
	private float complexityRW;
	private float cohesion;
	private float coupling;

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

	public float getCohesion() {
		return cohesion;
	}

	public void setCohesion(float cohesion) {
		this.cohesion = cohesion;
	}

	public float getCoupling() {
		return coupling;
	}

	public void setCoupling(float coupling) {
		this.coupling = coupling;
	}

	public void addEntity(String entity) {
		this.entities.add(new Entity(entity));
	}

	public boolean removeEntity(String entityName) {
		for (int i = 0; i < this.entities.size(); i++) {
			if (this.entities.get(i).getName().equals(entityName)) {
				this.entities.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean containsEntity(String entityName) {
		for (Entity entity : this.entities)
			if (entity.getName().equals(entityName))
				return true;
		return false;
	}

	public void calculateComplexity(List<Controller> controllers) {
		float complexity = 0;
		int totalControllers = 0;
		for (Controller controller : controllers) {
			boolean localAccess = false;
			boolean globalAccess = false;
			int accessAmount = 0;
			for (Entity controllerEntity : controller.getEntities()) {
				if (this.containsEntity(controllerEntity.getName()))
					localAccess = true;
				else
					globalAccess = true;
			}
			if (localAccess)
				totalControllers++;
			if (localAccess && globalAccess)
				complexity++;
		}
		complexity /= totalControllers;
		this.setComplexity(complexity);
	}

	public void calculateCohesion(List<Controller> controllers) {
		float cohesion = 0;
		int totalControllers = 0;
		for (Controller controller : controllers) {
			int numberEntitiesTouched = 0;
			for (Entity controllerEntity : controller.getEntities()) {
				if (this.containsEntity(controllerEntity.getName()))
					numberEntitiesTouched++;
			}
			if (numberEntitiesTouched > 0) {
				totalControllers++;
				cohesion = cohesion + ((float)numberEntitiesTouched / this.entities.size());
			}
		}
		cohesion /= totalControllers;
		this.setCohesion(cohesion);
	}
}
