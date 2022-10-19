package pt.ist.socialsoftware.mono2micro.operation.formCluster;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.split.AccessesSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.AccessesTransferOperation;

import java.util.Collection;
import java.util.stream.Collectors;

public class AccessesFormClusterOperation extends FormClusterOperation {
    public AccessesFormClusterOperation() {}

    public AccessesFormClusterOperation(FormClusterOperation formClusterOperation) {
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
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.removeFunctionalitiesWithEntityIDs(
                        decomposition,
                        getEntities().values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
                )
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        getEntities().forEach((clusterName, entitiesID) -> {
            Cluster toCluster = decomposition.getClusters().get(clusterName);
            if (toCluster == null) // If there is no cluster, the operation is a split, if there is, it is a transfer
                new AccessesSplitOperation(getNewCluster(), clusterName, entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))).executeOperation(decomposition);
            else
                new AccessesTransferOperation(getNewCluster(), toCluster.getName(), entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))).executeOperation(decomposition);
        });

        decomposition.removeCluster(getNewCluster());
    }
}
