package pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MergeOperation extends Operation {
    public static final String ACCESSES_SCIPY_MERGE = "AccessesSciPyMerge";

    private String newCluster;

    private Map<String, String> previousClusters;

    public MergeOperation() {}

    public MergeOperation(AccessesSciPyDecomposition decomposition, Short clusterNameID, Short otherClusterID, String newName) {
        this.previousClusters = new HashMap<>();
        Cluster cluster1 = decomposition.getCluster(clusterNameID);
        Cluster cluster2 = decomposition.getCluster(otherClusterID);

        String cluster1Entities = cluster1.getEntities().stream().map(Object::toString).collect(Collectors.joining(","));
        String cluster2Entities = cluster2.getEntities().stream().map(Object::toString).collect(Collectors.joining(","));
        previousClusters.put(cluster1.getName(), cluster1Entities);
        previousClusters.put(cluster2.getName(), cluster2Entities);

        this.newCluster = newName;
    }

    @Override
    public String getOperationType() {
        return ACCESSES_SCIPY_MERGE;
    }

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }

    public Map<String, String> getPreviousClusters() {
        return previousClusters;
    }

    public void setPreviousClusters(Map<String, String> previousClusters) {
        this.previousClusters = previousClusters;
    }
}
