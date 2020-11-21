package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ClusterDeserializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = ClusterDeserializer.class)
public class Cluster {
	private String name;
	private float complexity;
	private float cohesion;
	private float coupling;
	private Map<String, Set<Short>> couplingDependencies = new HashMap<>(); // <clusterID, Set<EntityID>>
	private Set<Short> entities = new HashSet<>(); // entity IDs

	public Cluster() { }

	public Cluster(String name) {
        this.name = name;
	}

	public Cluster(String name, Set<Short> entities) {
		this.name = name;
		this.entities = entities;
	}

	public Cluster(Cluster c) {
		this.name = c.getName();
		this.complexity = c.getComplexity();
		this.cohesion = c.getCohesion();
		this.coupling = c.getCoupling();
		this.couplingDependencies = c.getCouplingDependencies();
		this.entities = c.getEntities();
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

	public Map<String, Set<Short>> getCouplingDependencies() { return couplingDependencies; }

	public void setCouplingDependencies(Map<String, Set<Short>> couplingDependencies) { this.couplingDependencies = couplingDependencies; }

	public Set<Short> getEntities() { return entities; }

	public void setEntities(Set<Short> entities) {
		this.entities = entities;
	}
	public void addEntity(short entity) { this.entities.add(entity); }

	public void removeEntity(short entityID) { this.entities.remove(entityID); }

	public boolean containsEntity(short entityID) { return this.entities.contains(entityID); }

	public void addCouplingDependency(String toCluster, short entityID) {
		if (this.couplingDependencies.containsKey(toCluster)) {
			this.couplingDependencies.get(toCluster).add(entityID);
		} else {
			Set<Short> touchedEntityIDs = new HashSet<>();
			touchedEntityIDs.add(entityID);
			this.couplingDependencies.put(toCluster, touchedEntityIDs);
		}
	}

	public void addCouplingDependencies(String toCluster, Set<Short> entityIDs) {
		if (this.couplingDependencies.containsKey(toCluster)) {
			this.couplingDependencies.get(toCluster).addAll(entityIDs);
		} else {
			this.couplingDependencies.put(toCluster, entityIDs);
		}
	}
}
