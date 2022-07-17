package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.log.domain.AccessesSciPyLog;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.metrics.MetricFactory;

import java.util.*;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Document("decomposition")
public class AccessesSciPyDecomposition extends Decomposition {
    private static final String[] availableMetrics = {
            Metric.MetricType.COMPLEXITY,
            Metric.MetricType.PERFORMANCE,
            Metric.MetricType.COHESION,
            Metric.MetricType.COUPLING,
    };
    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    private Map<Short, Cluster> clusters = new HashMap<>();
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, Short> entityIDToClusterID = new HashMap<>();
    @DBRef(lazy = true)
    private AccessesSciPyLog decompositionLog;

    public AccessesSciPyDecomposition() {}

    @Override
    public String getStrategyType() {
        return ACCESSES_SCIPY;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    public Map<Short, Short> getEntityIDToClusterID() {
        return entityIDToClusterID;
    }

    public void setEntityIDToClusterID(Map<Short, Short> entityIDToClusterID) { this.entityIDToClusterID = entityIDToClusterID; }

    public AccessesSciPyLog getLog() {
        return decompositionLog;
    }

    public void setLog(AccessesSciPyLog decompositionLog) {
        this.decompositionLog = decompositionLog;
    }

    public void putEntity(short entityID, Short clusterID) {
        entityIDToClusterID.put(entityID, clusterID);
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public Map<Short, Cluster> getClusters() { return this.clusters; }

    public void setClusters(Map<Short, Cluster> clusters) { this.clusters = clusters; }

    public Map<String, Functionality> getFunctionalities() { return functionalities; }

    public void setFunctionalities(Map<String, Functionality> functionalities) { this.functionalities = functionalities; }

    public void addFunctionality(Functionality functionality) {
        this.functionalities.put(functionality.getName().replaceAll("\\.", "_"), functionality);
    }

    public boolean functionalityExists(String functionalityName) {
        return this.functionalities.containsKey(functionalityName.replaceAll("\\.", "_"));
    }

    public Functionality getFunctionality(String functionalityName) {
        Functionality c = this.functionalities.get(functionalityName.replaceAll("\\.", "_"));

        if (c == null) throw new Error("Functionality with name: " + functionalityName + " not found");

        return c;
    }

    public boolean clusterNameExists(String clusterName) {
        for (Map.Entry<Short, Cluster> cluster :this.clusters.entrySet())
            if (cluster.getValue().getName().equals(clusterName))
                return true;
        return false;
    }

    public void addCluster(Cluster cluster) {
        Cluster c = this.clusters.putIfAbsent(cluster.getID(), cluster);

        if (c != null) throw new Error("Cluster with ID: " + cluster.getID() + " already exists");
    }

    public Cluster removeCluster(Short clusterID) {
        Cluster c = this.clusters.remove(clusterID);

        if (c == null) throw new Error("Cluster with ID: " + clusterID + " not found");

        return c;
    }

    public Cluster getCluster(Short clusterID) {
        Cluster c = this.clusters.get(clusterID);

        if (c == null) throw new Error("Cluster with ID: " + clusterID + " not found");

        return c;
    }

    public Cluster getClusterByName(String name) {
        return this.clusters.values().stream().filter(cluster -> cluster.getName().equals(name)).findFirst().orElse(null);
    }

    public int maxClusterSize() {
        int max = 0;

        for (Cluster cluster : this.clusters.values()) {
            if (cluster.getEntities().size() > max)
                max = cluster.getEntities().size();
        }

        return max;
    }

    public Metric searchMetricByType(String metricType) {
        for (Metric metric: this.getMetrics())
            if (metric.getType().equals(metricType))
                return metric;
        return null;
    }

    public void calculateMetrics() throws Exception {
        System.out.println("Calculating decomposition metrics...");

        for(String metricType: availableMetrics) {
            Metric metric = searchMetricByType(metricType);
            if (metric == null) {
                metric = MetricFactory.getFactory().getMetric(metricType);
                this.addMetric(metric);
            }
            metric.calculateMetric(this);
        }
    }

    @JsonIgnore
    public short getNewClusterID() {
        return (short) (Collections.max(clusters.keySet()) + 1);
    }

    public void transferCouplingDependencies(Set<Short> entities, short currentClusterID, short newClusterID) {
        for (Cluster cluster : this.getClusters().values())
            cluster.transferCouplingDependencies(entities, currentClusterID, newClusterID);
    }
}
