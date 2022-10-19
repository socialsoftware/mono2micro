package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class RepositoryTransferOperation extends TransferOperation {
    public RepositoryTransferOperation() {}

    public RepositoryTransferOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
    }

    public RepositoryTransferOperation(TransferOperation transferOperation) {
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
    }

    @Override
    public void undo(Decomposition decomposition) {
        new RepositoryTransferOperation(getToCluster(), getFromCluster(), getEntities()).executeOperation(decomposition);
    }
}
