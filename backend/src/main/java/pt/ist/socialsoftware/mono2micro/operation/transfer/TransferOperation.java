package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TransferOperation extends Operation {
    public static final String TRANSFER_OPERATION = "TransferOperation";
    protected String fromCluster;
    protected String toCluster;
    protected String entities;

    public TransferOperation() {}

    public TransferOperation(TransferOperation operation) {
        this.fromCluster = operation.getFromCluster();
        this.toCluster = operation.getToCluster();
        this.entities = operation.getEntities();
    }

    public TransferOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
    }

    protected void transfer(Decomposition decomposition) {
        Cluster from = decomposition.getCluster(fromCluster);
        Cluster to = decomposition.getCluster(toCluster);
        Set<Short> entitiesList = Arrays.stream(entities.split(",")).map(Short::valueOf).collect(Collectors.toSet());

        for (Short entityID : entitiesList) {
            Element entity = from.getElementByID(entityID);
            if (entity != null) {
                to.addElement(entity);
                from.removeElement(entity);
            }
        }

        for (Cluster cluster : decomposition.getClusters().values())
            cluster.transferCouplingDependencies(entitiesList, fromCluster, toCluster);
    }

    @Override
    public String getOperationType() {
        return TRANSFER_OPERATION;
    }

    @Override
    public void execute(Decomposition decomposition) {
        executeOperation(decomposition);
        super.execute(decomposition);
    }

    @Override
    public void executeOperation(Decomposition decomposition) {
        transfer(decomposition);
        decomposition.getRepresentationInformations().forEach(representationInformation ->
                representationInformation.removeFunctionalitiesWithEntityIDs(
                        decomposition,
                        Arrays.stream(getEntities().split(",")).map(Short::valueOf).collect(Collectors.toSet())
                )
        );
    }

    @Override
    public void undo(Decomposition decomposition) {
        new TransferOperation(getToCluster(), getFromCluster(), getEntities()).executeOperation(decomposition);
    }

    public String getFromCluster() {
        return fromCluster;
    }

    public void setFromCluster(String fromCluster) {
        this.fromCluster = fromCluster;
    }

    public String getToCluster() {
        return toCluster;
    }

    public void setToCluster(String toCluster) {
        this.toCluster = toCluster;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}