package pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionOperation;

public class SplitDecompositionOperation extends DecompositionOperation {
    public static final String ACCESSES_SCIPY_SPLIT = "AccessesSciPySplit";

    private String originalCluster;

    private String entities;

    private String newCluster;

    public SplitDecompositionOperation() {}

    public SplitDecompositionOperation(AccessesSciPyDecomposition decomposition, Short clusterID, String newName, String entities) {
        Cluster cluster = decomposition.getCluster(clusterID);
        this.originalCluster = cluster.getName();
        this.newCluster = newName;
        this.entities = entities;
    }

    @Override
    public String getOperationType() {
        return ACCESSES_SCIPY_SPLIT;
    }

    public String getOriginalCluster() {
        return originalCluster;
    }

    public void setOriginalCluster(String originalCluster) {
        this.originalCluster = originalCluster;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }
}
