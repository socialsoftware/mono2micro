package pt.ist.socialsoftware.mono2micro.operation.rename;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class RepositoryRenameOperation extends RenameOperation {
    public RepositoryRenameOperation() {}

    public RepositoryRenameOperation(String clusterName, String newClusterName) {
        this.clusterName = clusterName;
        this.newClusterName = newClusterName;
    }

    public RepositoryRenameOperation(RenameOperation renameOperation) {
        super(renameOperation);
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        rename(decomposition);
    }

    @Override
    public void undo(Decomposition decomposition) {
        new RepositoryRenameOperation(getNewClusterName(), getClusterName()).executeOperation(decomposition);
    }
}
