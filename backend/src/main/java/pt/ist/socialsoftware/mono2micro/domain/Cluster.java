package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private String name;
	private List<String> entities;
	private float complexity;
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

	public List<String> getEntities() {
		return this.entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
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
		this.entities.add(entity);
	}

	public void removeEntity(String entity) {
		this.entities.remove(entity);
	}

	public boolean containsEntity(String entity) {
		return this.entities.contains(entity);
	}

	public void calculateComplexity(List<Controller> controllers) {
		float complexity = 0;
		int totalControllers = 0;
		for (Controller controller : controllers) {
			boolean localAccess = false;
			boolean globalAccess = false;
			for (String controllerEntity : controller.getEntities()) {
				if (this.entities.contains(controllerEntity))
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
			for (String controllerEntity : controller.getEntities()) {
				if (this.entities.contains(controllerEntity))
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
