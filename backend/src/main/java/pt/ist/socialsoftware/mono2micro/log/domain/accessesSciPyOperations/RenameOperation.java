package pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;

public class RenameOperation extends Operation {
    public static final String ACCESSES_SCIPY_RENAME = "AccessesSciPyRename";

    private String newClusterName;

    private String previousClusterName;

    public RenameOperation() {}

    public RenameOperation(AccessesSciPyDecomposition decomposition, Short clusterID, String newClusterName) {
        Cluster cluster = decomposition.getCluster(clusterID);
        this.previousClusterName = cluster.getName();
        this.newClusterName = newClusterName;
    }

    @Override
    public String getOperationType() {
        return ACCESSES_SCIPY_RENAME;
    }

    public String getNewClusterName() {
        return newClusterName;
    }

    public void setNewClusterName(String newClusterName) {
        this.newClusterName = newClusterName;
    }

    public String getPreviousClusterName() {
        return previousClusterName;
    }

    public void setPreviousClusterName(String previousClusterName) {
        this.previousClusterName = previousClusterName;
    }
}