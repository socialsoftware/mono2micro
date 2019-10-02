package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cluster {
	private String name;
	private float complexity;
	private float complexityRW;
	private float complexitySeq;
	private float cohesion;
	private Map<String,Float> coupling;
	private Map<String,Float> couplingRW;
	private Map<String,Float> couplingSeq;
	private List<Entity> entities = new ArrayList<>();

	public Cluster() {
	}

	public Cluster(String name) {
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

	public float getCohesion() {
		return cohesion;
	}

	public void setCohesion(float cohesion) {
		this.cohesion = cohesion;
	}

	public Map<String,Float> getCoupling() {
		return coupling;
	}

	public void setCoupling(Map<String,Float> coupling) {
		this.coupling = coupling;
	}

	public Map<String,Float> getCouplingRW() {
		return couplingRW;
	}

	public void setCouplingRW(Map<String,Float> couplingRW) {
		this.couplingRW = couplingRW;
	}

	public Map<String,Float> getCouplingSeq() {
		return couplingSeq;
	}

	public void setCouplingSeq(Map<String,Float> couplingSeq) {
		this.couplingSeq = couplingSeq;
	}

	public List<Entity> getEntities() {
		return this.entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public List<String> getEntityNames() {
		return this.entities
			.stream()
			.map(e -> e.getName())
			.collect(Collectors.toList());
	}

	public Entity getEntity(String entityName) {
		return this.entities
			.stream()
			.filter(e -> e.getName().equals(entityName))
			.findAny()
			.orElse(null);
	}

	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

	public void removeEntity(String entityName) {
		this.entities.removeIf(e -> e.getName().equals(entityName));
	}

	public boolean containsEntity(String entityName) {
		return this.entities
			.stream()
			.anyMatch(e -> e.getName().equals(entityName));
	}

	public void calculateCohesion(List<Controller> clusterControllers) {
		float cohesion = 0;
		for (Controller controller : clusterControllers) {
			float numberEntitiesTouched = 0;
			for (String controllerEntity : controller.getEntities().keySet()) {
				if (this.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}
			cohesion += numberEntitiesTouched / this.entities.size();
		}
		cohesion /= clusterControllers.size();
		this.setCohesion(cohesion);
	}
}
