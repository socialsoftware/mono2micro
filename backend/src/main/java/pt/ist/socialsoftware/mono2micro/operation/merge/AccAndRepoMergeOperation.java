package pt.ist.socialsoftware.mono2micro.operation.merge;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.rename.AccAndRepoRenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.AccAndRepoSplitOperation;

import java.util.stream.Collectors;

public class AccAndRepoMergeOperation extends MergeOperation {
    public AccAndRepoMergeOperation() {}

    public AccAndRepoMergeOperation(String cluster1Name, String cluster2Name, String newName) {
        this.cluster1Name = cluster1Name;
        this.cluster2Name = cluster2Name;
        this.newName = newName;
    }

    public AccAndRepoMergeOperation(MergeOperation mergeOperation) {
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
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.removeFunctionalitiesWithEntityIDs(
                        decomposition,
                        decomposition.getCluster(getNewName()).getElements().stream().map(Element::getId).collect(Collectors.toSet())
                )
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        if (getCluster1Name().equals(getNewName())) {
            new AccAndRepoSplitOperation(getNewName(), getCluster2Name(), getCluster2Entities()).executeOperation(decomposition);
            new AccAndRepoRenameOperation(getNewName(), getCluster1Name()).executeOperation(decomposition);
        } else {
            new AccAndRepoSplitOperation(getNewName(), getCluster1Name(), getCluster1Entities()).executeOperation(decomposition);
            new AccAndRepoRenameOperation(getNewName(), getCluster2Name()).executeOperation(decomposition);
        }
    }
}
