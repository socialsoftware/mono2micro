package pt.ist.socialsoftware.mono2micro.cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SciPyCluster extends Cluster {
    Map<String, Object> metrics = new HashMap<>(); // Map<Metric type, Metric value>
    private Map<String, Set<Short>> couplingDependencies = new HashMap<>(); // <clusterName, Set<EntityID>>

    public SciPyCluster() { }

    public SciPyCluster(String name) {
        this.name = name;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(String type, Object value) {
        this.metrics.put(type, value);
    }

    public Map<String, Set<Short>> getCouplingDependencies() { return couplingDependencies; }

    public void setCouplingDependencies(Map<String, Set<Short>> couplingDependencies) { this.couplingDependencies = couplingDependencies; }

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