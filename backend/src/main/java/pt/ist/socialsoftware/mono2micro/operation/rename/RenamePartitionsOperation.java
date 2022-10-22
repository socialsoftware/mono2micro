package pt.ist.socialsoftware.mono2micro.operation.rename;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Set;

public class RenamePartitionsOperation extends RenameOperation {
    public RenamePartitionsOperation() {}

    public RenamePartitionsOperation(RenameOperation operation) {
        super(operation);
    }

    public RenamePartitionsOperation(String clusterName, String newClusterName) {
        this.clusterName = clusterName;
        this.newClusterName = newClusterName;
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
        new RenamePartitionsOperation(getNewClusterName(), getClusterName()).executeOperation(decomposition);
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

}
