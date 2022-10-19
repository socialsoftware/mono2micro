package pt.ist.socialsoftware.mono2micro.operation.formCluster;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.DefaultCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormClusterOperation extends Operation {
    public static final String FORM_CLUSTER_OPERATION = "FormClusterOperation";
    protected String newCluster;
    protected Map<String, List<Short>> entities;

    public FormClusterOperation() {}

    public FormClusterOperation(FormClusterOperation operation) {
        this.newCluster = operation.getNewCluster();
        this.entities = operation.getEntities();
    }

    @Override
    public String getOperationType() {
        return FORM_CLUSTER_OPERATION;
    }

    protected void formCluster(Decomposition decomposition) {
        List<Short> entitiesIDs = entities.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        if (decomposition.clusterNameExists(newCluster)) {
            Cluster previousCluster = decomposition.getClusters().values().stream().filter(cluster -> cluster.getName().equals(newCluster)).findFirst()
                    .orElseThrow(() -> new KeyAlreadyExistsException("Cluster with name: " + newCluster + " already exists"));
            if (!entitiesIDs.containsAll(previousCluster.getElementsIDs()))
                throw new KeyAlreadyExistsException("Cluster with name: " + newCluster + " already exists");
        }

        Cluster createdCluster = new DefaultCluster(newCluster);

        for (Short entityID : entitiesIDs) {
            Cluster currentCluster = decomposition.getClusters().values().stream()
                    .filter(cluster -> cluster.containsElement(entityID)).findFirst()
                    .orElseThrow(() -> new RuntimeException("No cluster contains entity " + entityID));

            Element entity = currentCluster.getElementByID(entityID);
            createdCluster.addElement(entity);
            currentCluster.removeElement(entityID);
            for (Cluster cluster : decomposition.getClusters().values())
                cluster.transferCouplingDependencies(Collections.singleton(entityID), currentCluster.getName(), createdCluster.getName());

            if (currentCluster.getElements().size() == 0)
                decomposition.removeCluster(currentCluster.getName());
        }
        decomposition.addCluster(createdCluster);
    }

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }

    public Map<String, List<Short>> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, List<Short>> entities) {
        this.entities = entities;
    }
}
