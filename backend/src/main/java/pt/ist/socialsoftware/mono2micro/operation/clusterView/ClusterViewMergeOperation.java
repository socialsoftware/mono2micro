package pt.ist.socialsoftware.mono2micro.operation.clusterView;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.MergeOperation;

import java.util.stream.Collectors;

public class ClusterViewMergeOperation extends MergeOperation {
    public static final String CLUSTER_VIEW_MERGE = "ClusterViewMerge";
    private String cluster1Entities;
    private String cluster2Entities;

    public ClusterViewMergeOperation() {}

    public ClusterViewMergeOperation(String cluster1Name, String cluster2Name, String newName) {
        this.cluster1Name = cluster1Name;
        this.cluster2Name = cluster2Name;
        this.newName = newName;
    }

    public void addEntities(Decomposition decomposition) {
        if (cluster1Entities == null) { // First time doing the operation sets up the original cluster entities
            Cluster cluster1 = decomposition.getCluster(this.cluster1Name);
            Cluster cluster2 = decomposition.getCluster(this.cluster2Name);
            cluster1Entities = cluster1.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
            cluster2Entities = cluster2.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
        }
    }

    @Override
    public String getOperationType() {
        return CLUSTER_VIEW_MERGE;
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
