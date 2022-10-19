package pt.ist.socialsoftware.mono2micro.operation.formCluster;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.split.RepositorySplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.RepositoryTransferOperation;

import java.util.stream.Collectors;

public class RepositoryFormClusterOperation extends FormClusterOperation {
    public RepositoryFormClusterOperation() {}

    public RepositoryFormClusterOperation(FormClusterOperation formClusterOperation) {
        super(formClusterOperation);
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        formCluster(decomposition);
    }

    @Override
    public void undo(Decomposition decomposition) {
        getEntities().forEach((clusterName, entitiesID) -> {
            Cluster toCluster = decomposition.getClusters().get(clusterName);
            if (toCluster == null) // If there is no cluster, the operation is a split, if there is, it is a transfer
                new RepositorySplitOperation(getNewCluster(), clusterName, entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))).executeOperation(decomposition);
            else
                new RepositoryTransferOperation(getNewCluster(), toCluster.getName(), entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))).executeOperation(decomposition);
        });

        decomposition.removeCluster(getNewCluster());
    }
}
