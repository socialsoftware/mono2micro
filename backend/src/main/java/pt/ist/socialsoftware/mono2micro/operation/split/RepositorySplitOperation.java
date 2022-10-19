package pt.ist.socialsoftware.mono2micro.operation.split;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.merge.RepositoryMergeOperation;

public class RepositorySplitOperation extends SplitOperation {
    public RepositorySplitOperation() {}

    public RepositorySplitOperation(String originalCluster, String newCluster, String entities) {
        this.originalCluster = originalCluster;
        this.newCluster = newCluster;
        this.entities = entities;
    }

    public RepositorySplitOperation(SplitOperation operation) {
        super(operation);
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        split(decomposition);
    }

    @Override
    public void undo(Decomposition decomposition) {
        new RepositoryMergeOperation(getOriginalCluster(), getNewCluster(), getOriginalCluster()).executeOperation(decomposition);
    }
}
