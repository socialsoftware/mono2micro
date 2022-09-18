package pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;

import java.util.stream.Collectors;

public class MergeOperation extends Operation {
    public static final String ACCESSES_SCIPY_MERGE = "AccessesSciPyMerge";

    private String newCluster;
    private String cluster1Name;
    private String cluster2Name;
    private String cluster1Entities;
    private String cluster2Entities;

    public MergeOperation() {}

    public MergeOperation(AccessesSciPyDecomposition decomposition, String clusterNameName, String otherClusterName, String newName) {
        Cluster cluster1 = decomposition.getCluster(clusterNameName);
        Cluster cluster2 = decomposition.getCluster(otherClusterName);

        cluster1Name = cluster1.getName();
        cluster2Name = cluster2.getName();
        cluster1Entities = cluster1.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
        cluster2Entities = cluster2.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));

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

    public String getCluster1Name() {
        return cluster1Name;
    }

    public void setCluster1Name(String cluster1Name) {
        this.cluster1Name = cluster1Name;
    }

    public String getCluster2Name() {
        return cluster2Name;
    }

    public void setCluster2Name(String cluster2Name) {
        this.cluster2Name = cluster2Name;
    }

    public String getCluster1Entities() {
        return cluster1Entities;
    }

    public void setCluster1Entities(String cluster1Entities) {
        this.cluster1Entities = cluster1Entities;
    }

    public String getCluster2Entities() {
        return cluster2Entities;
    }

    public void setCluster2Entities(String cluster2Entities) {
        this.cluster2Entities = cluster2Entities;
    }
}
