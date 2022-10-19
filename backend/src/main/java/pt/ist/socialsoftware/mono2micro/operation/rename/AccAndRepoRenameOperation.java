package pt.ist.socialsoftware.mono2micro.operation.rename;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class AccAndRepoRenameOperation extends RenameOperation {
    public AccAndRepoRenameOperation() {}

    public AccAndRepoRenameOperation(String clusterName, String newClusterName) {
        this.clusterName = clusterName;
        this.newClusterName = newClusterName;
    }

    public AccAndRepoRenameOperation(RenameOperation renameOperation) {
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
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.renameClusterInFunctionalities(getClusterName(), getNewClusterName())
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        new AccAndRepoRenameOperation(getNewClusterName(), getClusterName()).executeOperation(decomposition);
    }
}
