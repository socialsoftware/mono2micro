package pt.ist.socialsoftware.mono2micro.operation.split;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.merge.AccessesMergeOperation;

import java.util.stream.Collectors;

public class AccessesSplitOperation extends SplitOperation {

    public AccessesSplitOperation() {}

    public AccessesSplitOperation(String originalCluster, String newCluster, String entities) {
        this.originalCluster = originalCluster;
        this.newCluster = newCluster;
        this.entities = entities;
    }

    public AccessesSplitOperation(SplitOperation operation) {
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
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.removeFunctionalitiesWithEntityIDs(
                        decomposition,
                        decomposition.getCluster(getNewCluster()).getElements().stream().map(Element::getId).collect(Collectors.toSet())
                )
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        new AccessesMergeOperation(getOriginalCluster(), getNewCluster(), getOriginalCluster()).executeOperation(decomposition);
    }
}
