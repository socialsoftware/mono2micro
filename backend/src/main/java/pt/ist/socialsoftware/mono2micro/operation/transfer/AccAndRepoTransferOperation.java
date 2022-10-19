package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AccAndRepoTransferOperation extends TransferOperation {
    public AccAndRepoTransferOperation() {}

    public AccAndRepoTransferOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
    }

    public AccAndRepoTransferOperation(TransferOperation transferOperation) {
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
        new AccAndRepoTransferOperation(getToCluster(), getFromCluster(), getEntities()).executeOperation(decomposition);
    }
}
