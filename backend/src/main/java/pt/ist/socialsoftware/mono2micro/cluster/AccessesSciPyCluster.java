package pt.ist.socialsoftware.mono2micro.cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccessesSciPyCluster extends Cluster {
    private double complexity;
    private double cohesion;
    private double coupling;
    private Map<String, Set<Short>> couplingDependencies = new HashMap<>(); // <clusterName, Set<EntityID>>
    private Set<Short> entities = new HashSet<>(); // entity IDs

    public AccessesSciPyCluster() { }

    public AccessesSciPyCluster(String name) {
        this.name = name;
    }

    public double getComplexity() {
        return complexity;
    }

    public void setComplexity(double complexity) {
        this.complexity = complexity;
    }

    public double getCohesion() {
        return cohesion;
    }

    public void setCohesion(double cohesion) {
        this.cohesion = cohesion;
    }

    public double getCoupling() {
        return coupling;
    }

    public void setCoupling(double coupling) {
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

    public void clearCouplingDependencies() {this.couplingDependencies.clear(); }

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

    public void transferCouplingDependencies(Set<Short> entities, String currentClusterName, String newClusterName) {
        Set<Short> dependencyEntities = this.couplingDependencies.get(currentClusterName);

        if (dependencyEntities == null) return;

        for (short entity : entities)
            if (dependencyEntities.remove(entity) && !newClusterName.equals(this.name))
                addCouplingDependency(newClusterName, entity);
        if (dependencyEntities.isEmpty())
            this.couplingDependencies.remove(currentClusterName);
    }
}