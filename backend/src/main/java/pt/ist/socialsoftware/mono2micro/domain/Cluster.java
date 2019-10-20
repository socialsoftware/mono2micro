package pt.ist.socialsoftware.mono2micro.domain;

import java.util.*;
import java.util.stream.Collectors;

public class Cluster {
	private String name;
	private float complexity;
	private float cohesion;
	private float coupling;
	private Map<String, Set<String>> couplingDependencies;
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

	public float getCoupling() {
		return coupling;
	}

	public void setCoupling(float coupling) {
		this.coupling = coupling;
	}

	public Map<String, Set<String>> getCouplingDependencies() {
		return couplingDependencies;
	}

	public void setCouplingDependencies(Map<String, Set<String>> couplingDependencies) {
		this.couplingDependencies = couplingDependencies;
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
