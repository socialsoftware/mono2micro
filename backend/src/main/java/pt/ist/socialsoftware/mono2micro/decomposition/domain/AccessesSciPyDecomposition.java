package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.cluster.AccessesSciPyCluster;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.log.domain.AccessesSciPyLog;

import java.util.*;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Document("decomposition")
public class AccessesSciPyDecomposition extends Decomposition {
    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;

    @DBRef
    Similarity similarity;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, String> entityIDToClusterName = new HashMap<>();
    @DBRef(lazy = true)
    private AccessesSciPyLog decompositionLog;

    public AccessesSciPyDecomposition() {}

    public AccessesSciPyDecomposition(AccessesSciPyDecomposition decomposition) {
        this.name = decomposition.getName();
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.clusters = decomposition.getClusters();
        this.entityIDToClusterName = decomposition.getEntityIDToClusterName();
    }

    @Override
    public String getStrategyType() {
        return ACCESSES_SCIPY;
    }

    public Similarity getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
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

    public Map<Short, String> getEntityIDToClusterName() {
        return entityIDToClusterName;
    }

    public void setEntityIDToClusterName(Map<Short, String> entityIDToClusterName) { this.entityIDToClusterName = entityIDToClusterName; }

    public AccessesSciPyLog getLog() {
        return decompositionLog;
    }

    public void setLog(AccessesSciPyLog decompositionLog) {
        this.decompositionLog = decompositionLog;
    }

    public void putEntity(short entityID, String clusterName) {
        entityIDToClusterName.put(entityID, clusterName);
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public Map<String, Cluster> getClusters() { return this.clusters; }

    public void setClusters(Map<String, Cluster> clusters) { this.clusters = clusters; }

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

    public int maxClusterSize() {
        int max = 0;

        for (Cluster c : this.clusters.values()) {
            AccessesSciPyCluster cluster = (AccessesSciPyCluster) c;
            if (cluster.getEntities().size() > max)
                max = cluster.getEntities().size();
        }

        return max;
    }

    public void transferCouplingDependencies(Set<Short> entities, String currentClusterName, String newClusterName) {
        for (Cluster cluster : this.getClusters().values())
            ((AccessesSciPyCluster) cluster).transferCouplingDependencies(entities, currentClusterName, newClusterName);
    }
}
