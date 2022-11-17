package pt.ist.socialsoftware.mono2micro.operation.merge;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenamePartitionsOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitPartitionsOperation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MergePartitionsOperation extends  MergeOperation {
    public MergePartitionsOperation() {}
    public MergePartitionsOperation(MergeOperation operation) {
        super(operation);
    }

    public MergePartitionsOperation(String cluster1Name, String cluster2Name, String newName) {
        this.cluster1Name = cluster1Name;
        this.cluster2Name = cluster2Name;
        this.newName = newName;
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
            new SplitPartitionsOperation(getNewName(), getCluster2Name(), getCluster2Entities()).executeOperation(decomposition);
            new RenamePartitionsOperation(getNewName(), getCluster1Name()).executeOperation(decomposition);
        } else {
            new SplitPartitionsOperation(getNewName(), getCluster1Name(), getCluster1Entities()).executeOperation(decomposition);
            new RenamePartitionsOperation(getNewName(), getCluster2Name()).executeOperation(decomposition);
        }
    }

    protected void merge(Decomposition decomposition) {
        Cluster cluster1 = decomposition.getCluster(cluster1Name);
        Cluster cluster2 = decomposition.getCluster(cluster2Name);
        if (decomposition.clusterNameExists(newName) && !cluster1.getName().equals(newName) && !cluster2.getName().equals(newName))
            throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster mergedCluster = new Partition(newName);

        Set<Element> allEntities = new HashSet<>(cluster1.getElements());
        allEntities.addAll(cluster2.getElements());
        mergedCluster.setElements(allEntities);

        for (Cluster cluster : decomposition.getClusters().values()) {
            Partition partition = (Partition) cluster;
            partition.transferCouplingDependencies(cluster1.getElementsIDs(), cluster1.getName(), mergedCluster.getName());
            partition.transferCouplingDependencies(cluster2.getElementsIDs(), cluster2.getName(), mergedCluster.getName());
        }

        decomposition.removeCluster(cluster1Name);
        decomposition.removeCluster(cluster2Name);
        decomposition.addCluster(mergedCluster);
    }
}
