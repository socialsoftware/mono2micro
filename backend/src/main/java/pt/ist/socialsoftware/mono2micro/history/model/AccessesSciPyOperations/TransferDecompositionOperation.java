package pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionOperation;

public class TransferDecompositionOperation extends DecompositionOperation {
    public static final String ACCESSES_SCIPY_TRANSFER = "AccessesSciPyTransfer";

    private String fromCluster;

    private String toCluster;

    private String entities;

    public TransferDecompositionOperation() {}

    public TransferDecompositionOperation(AccessesSciPyDecomposition decomposition, Short clusterID, Short toClusterID, String entities) {
        Cluster cluster = decomposition.getCluster(clusterID);
        Cluster toCluster = decomposition.getCluster(toClusterID);

        this.fromCluster = cluster.getName();
        this.toCluster = toCluster.getName();

        this.entities = entities;
    }

    @Override
    public String getOperationType() {
        return ACCESSES_SCIPY_TRANSFER;
    }

    public String getFromCluster() {
        return fromCluster;
    }

    public void setFromCluster(String fromCluster) {
        this.fromCluster = fromCluster;
    }

    public String getToCluster() {
        return toCluster;
    }

    public void setToCluster(String toCluster) {
        this.toCluster = toCluster;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}