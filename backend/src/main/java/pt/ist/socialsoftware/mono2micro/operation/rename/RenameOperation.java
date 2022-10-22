package pt.ist.socialsoftware.mono2micro.operation.rename;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Set;

public class RenameOperation extends Operation {
    public static final String RENAME_OPERATION = "RenameOperation";
    protected String clusterName;
    protected String newClusterName;

    public RenameOperation() {}

    public RenameOperation(String clusterName, String newClusterName) {
        this.clusterName = clusterName;
        this.newClusterName = newClusterName;
    }

    public RenameOperation(RenameOperation renameOperation) {
        this.clusterName = renameOperation.getClusterName();
        this.newClusterName = renameOperation.getNewClusterName();
    }

    @Override
    public String getOperationType() {
        return RENAME_OPERATION;
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        rename(decomposition);
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.renameClusterInFunctionalities(getClusterName(), getNewClusterName())
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        new RenameOperation(getNewClusterName(), getClusterName()).executeOperation(decomposition);
    }

    protected void rename(Decomposition decomposition) {
        if (decomposition.clusterNameExists(newClusterName) && !clusterName.equals(newClusterName))
            throw new KeyAlreadyExistsException("Cluster with name: " + newClusterName + " already exists");

        Cluster oldCluster = decomposition.removeCluster(clusterName);
        oldCluster.setName(newClusterName);
        decomposition.addCluster(oldCluster);

        // Change coupling dependencies
        decomposition.getClusters().forEach((s, cluster) -> {
            Set<Short> dependencies = cluster.getCouplingDependencies().get(clusterName);
            if (dependencies != null) {cluster.getCouplingDependencies().remove(clusterName); cluster.addCouplingDependencies(newClusterName, dependencies);}
        });
    }

    public String getNewClusterName() {
        return newClusterName;
    }

    public void setNewClusterName(String newClusterName) {
        this.newClusterName = newClusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}