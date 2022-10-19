package pt.ist.socialsoftware.mono2micro.operation.merge;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.rename.RepositoryRenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.RepositorySplitOperation;

public class RepositoryMergeOperation extends MergeOperation {
    public RepositoryMergeOperation() {}

    public RepositoryMergeOperation(String cluster1Name, String cluster2Name, String newName) {
        this.cluster1Name = cluster1Name;
        this.cluster2Name = cluster2Name;
        this.newName = newName;
    }

    public RepositoryMergeOperation(MergeOperation mergeOperation) {
        super(mergeOperation);
    }

    @Override
    public void execute(Decomposition decomposition) {
        storeState(decomposition); // needed so that when doing undo, the original entities are restored
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        merge(decomposition);
    }

    @Override
    public void undo(Decomposition decomposition) {
        if (getCluster1Name().equals(getNewName())) {
            new RepositorySplitOperation(getNewName(), getCluster2Name(), getCluster2Entities()).executeOperation(decomposition);
            new RepositoryRenameOperation(getNewName(), getCluster1Name()).executeOperation(decomposition);
        } else {
            new RepositorySplitOperation(getNewName(), getCluster1Name(), getCluster1Entities()).executeOperation(decomposition);
            new RepositoryRenameOperation(getNewName(), getCluster2Name()).executeOperation(decomposition);
        }
    }
}
