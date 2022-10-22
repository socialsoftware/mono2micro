package pt.ist.socialsoftware.mono2micro.operation.merge;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.DefaultCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.Operation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MergeOperation extends Operation {
    public static final String MERGE_OPERATION = "MergeOperation";
    protected String cluster1Name;
    protected String cluster2Name;
    protected String newName;
    protected String cluster1Entities;
    protected String cluster2Entities;

    public MergeOperation() {}

    public MergeOperation(MergeOperation mergeOperation) {
        this.cluster1Name = mergeOperation.getCluster1Name();
        this.cluster2Name = mergeOperation.getCluster2Name();
        this.newName = mergeOperation.getNewName();
    }

    public MergeOperation(String cluster1Name, String cluster2Name, String newName) {
        this.cluster1Name = cluster1Name;
        this.cluster2Name = cluster2Name;
        this.newName = newName;
    }

    @Override
    public String getOperationType() {
        return MERGE_OPERATION;
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
            new SplitOperation(getNewName(), getCluster2Name(), getCluster2Entities()).executeOperation(decomposition);
            new RenameOperation(getNewName(), getCluster1Name()).executeOperation(decomposition);
        } else {
            new SplitOperation(getNewName(), getCluster1Name(), getCluster1Entities()).executeOperation(decomposition);
            new RenameOperation(getNewName(), getCluster2Name()).executeOperation(decomposition);
        }
    }

    protected void storeState(Decomposition decomposition) {
        if (cluster1Entities == null) { // First time doing the operation sets up the original cluster entities
            Cluster cluster1 = decomposition.getCluster(this.cluster1Name);
            Cluster cluster2 = decomposition.getCluster(this.cluster2Name);
            cluster1Entities = cluster1.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
            cluster2Entities = cluster2.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
        }
    }

    protected void merge(Decomposition decomposition) {
        Cluster cluster1 = decomposition.getCluster(cluster1Name);
        Cluster cluster2 = decomposition.getCluster(cluster2Name);
        if (decomposition.clusterNameExists(newName) && !cluster1.getName().equals(newName) && !cluster2.getName().equals(newName))
            throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster mergedCluster = new DefaultCluster(newName);

        Set<Element> allEntities = new HashSet<>(cluster1.getElements());
        allEntities.addAll(cluster2.getElements());
        mergedCluster.setElements(allEntities);

        for (Cluster cluster : decomposition.getClusters().values()) {
            cluster.transferCouplingDependencies(cluster1.getElementsIDs(), cluster1.getName(), mergedCluster.getName());
            cluster.transferCouplingDependencies(cluster2.getElementsIDs(), cluster2.getName(), mergedCluster.getName());
        }

        decomposition.removeCluster(cluster1Name);
        decomposition.removeCluster(cluster2Name);
        decomposition.addCluster(mergedCluster);
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getCluster1Name() {
        return cluster1Name;
    }

    public void setCluster1Name(String cluster1Name) {
        this.cluster1Name = cluster1Name;
    }

    public String getCluster2Name() {
        return cluster2Name;
    }

    public void setCluster2Name(String cluster2Name) {
        this.cluster2Name = cluster2Name;
    }

    public String getCluster1Entities() {
        return cluster1Entities;
    }

    public void setCluster1Entities(String cluster1Entities) {
        this.cluster1Entities = cluster1Entities;
    }

    public String getCluster2Entities() {
        return cluster2Entities;
    }

    public void setCluster2Entities(String cluster2Entities) {
        this.cluster2Entities = cluster2Entities;
    }
}
