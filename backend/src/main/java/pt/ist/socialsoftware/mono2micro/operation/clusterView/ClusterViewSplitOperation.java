package pt.ist.socialsoftware.mono2micro.operation.clusterView;

import pt.ist.socialsoftware.mono2micro.operation.SplitOperation;

public class ClusterViewSplitOperation extends SplitOperation {
    public static final String CLUSTER_VIEW_SPLIT = "ClusterViewSplit";
    protected String entities;

    public ClusterViewSplitOperation() {}

    public ClusterViewSplitOperation(String originalCluster, String newCluster, String entities) {
        this.originalCluster = originalCluster;
        this.newCluster = newCluster;
        this.entities = entities;
    }

    @Override
    public String getOperationType() {
        return CLUSTER_VIEW_SPLIT;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}
