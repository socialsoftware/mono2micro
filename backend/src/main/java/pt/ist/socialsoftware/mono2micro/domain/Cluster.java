package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;

public class Cluster {
	private String name;
	private float complexity;
	private float cohesion;
	private float coupling;
	private Map<String, Set<String>> couplingDependencies;
	private Set<String> entities = new HashSet<>(); // entity names

	public Cluster() { }

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

	public float getCoupling() {
		return coupling;
	}

	public void setCoupling(float coupling) {
		this.coupling = coupling;
	}

	public Map<String, Set<String>> getCouplingDependencies() { return couplingDependencies; }

	public void setCouplingDependencies(Map<String, Set<String>> couplingDependencies) { this.couplingDependencies = couplingDependencies; }

	public Set<String> getEntities() { return entities; }

	public void setEntities(Set<String> entities) {
		this.entities = entities;
	}
	public void addEntity(String entity) { this.entities.add(entity); }

	public void removeEntity(String entityName) { this.entities.remove(entityName); }

	public boolean containsEntity(String entityName) { return this.entities.contains(entityName); }

	public void addCouplingDependency(String toCluster, String toEntity) {
		if (this.couplingDependencies.containsKey(toCluster)) {
			this.couplingDependencies.get(toCluster).add(toEntity);
		} else {
			Set<String> dependencies = new HashSet<>();
			dependencies.add(toEntity);
			this.couplingDependencies.put(toCluster, dependencies);
		}
	}
}
