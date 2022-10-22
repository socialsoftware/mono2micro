package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TransferPartitionsOperation extends TransferOperation {
    public TransferPartitionsOperation() {}
    public TransferPartitionsOperation(TransferOperation operation) {
        super(operation);
    }

    public TransferPartitionsOperation(String fromCluster, String toCluster, String entities) {
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.entities = entities;
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
        new TransferPartitionsOperation(getToCluster(), getFromCluster(), getEntities()).executeOperation(decomposition);
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
}
