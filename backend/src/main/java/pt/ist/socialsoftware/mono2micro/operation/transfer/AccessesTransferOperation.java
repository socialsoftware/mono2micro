package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AccessesTransferOperation extends TransferOperation {
    public AccessesTransferOperation() {}

    public AccessesTransferOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
    }

    public AccessesTransferOperation(TransferOperation transferOperation) {
        super(transferOperation);
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        transfer(decomposition);
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.removeFunctionalitiesWithEntityIDs(
                        decomposition,
                        Arrays.stream(getEntities().split(",")).map(Short::valueOf).collect(Collectors.toSet())
                )
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        new AccessesTransferOperation(getToCluster(), getFromCluster(), getEntities()).executeOperation(decomposition);
    }
}
