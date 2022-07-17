package pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;

public class TransferOperation extends Operation {
    public static final String ACCESSES_SCIPY_TRANSFER = "AccessesSciPyTransfer";

    private String fromCluster;

    private String toCluster;

    private String entities;

    public TransferOperation() {}

    public TransferOperation(AccessesSciPyDecomposition decomposition, Short clusterID, Short toClusterID, String entities) {
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