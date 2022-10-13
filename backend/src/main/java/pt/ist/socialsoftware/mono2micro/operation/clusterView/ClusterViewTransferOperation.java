package pt.ist.socialsoftware.mono2micro.operation.clusterView;

import pt.ist.socialsoftware.mono2micro.operation.TransferOperation;

public class ClusterViewTransferOperation extends TransferOperation {
    public static final String CLUSTER_VIEW_TRANSFER = "ClusterViewTransfer";
    private String entities;

    public ClusterViewTransferOperation() {}

    public ClusterViewTransferOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
    }

    @Override
    public String getOperationType() {
        return CLUSTER_VIEW_TRANSFER;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}
