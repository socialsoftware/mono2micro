package pt.ist.socialsoftware.mono2micro.operation.split;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergePartitionsOperation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.stream.Collectors;

public class SplitPartitionsOperation extends SplitOperation {
    public SplitPartitionsOperation() {}

    public SplitPartitionsOperation(SplitOperation operation) {
        super(operation);
    }

    public SplitPartitionsOperation(String originalCluster, String newCluster, String entities) {
        this.originalCluster = originalCluster;
        this.newCluster = newCluster;
        this.entities = entities;
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
        new MergePartitionsOperation(getOriginalCluster(), getNewCluster(), getOriginalCluster()).executeOperation(decomposition);
    }

    protected void split(Decomposition decomposition) {
        String[] entitiesList = entities.split(",");
        if (decomposition.clusterNameExists(newCluster)) throw new KeyAlreadyExistsException("Cluster with name: " + newCluster + " already exists");

        Cluster currentCluster = decomposition.getCluster(originalCluster);
        Cluster createdCluster = new Partition(newCluster);

        for (String stringifiedEntityID : entitiesList) {
            Element entity = currentCluster.getElementByID(Short.parseShort(stringifiedEntityID));

            if (entity != null) {
                createdCluster.addElement(entity);
                currentCluster.removeElement(entity);
            }
        }
        for (Cluster cluster : decomposition.getClusters().values()) {
            Partition partition = (Partition) cluster;
            partition.transferCouplingDependencies(createdCluster.getElementsIDs(), currentCluster.getName(), createdCluster.getName());
        }

        decomposition.addCluster(createdCluster);
    }
}
