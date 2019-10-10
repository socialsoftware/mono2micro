package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cluster {
	private String name;
	private float complexity;
	private float cohesion;
	private Map<String,Float> coupling;
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
}
