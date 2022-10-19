package pt.ist.socialsoftware.mono2micro.operation.split;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.DefaultCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import javax.management.openmbean.KeyAlreadyExistsException;

public class SplitOperation extends Operation {
    public static final String SPLIT_OPERATION = "SplitOperation";
    protected String originalCluster;
    protected String newCluster;
    protected String entities;

    public SplitOperation() {}

    public SplitOperation(SplitOperation operation) {
        this.originalCluster = operation.getOriginalCluster();
        this.newCluster = operation.getNewCluster();
        this.entities = operation.getEntities();
    }

    @Override
    public String getOperationType() {
        return SPLIT_OPERATION;
    }

    protected void split(Decomposition decomposition) {
        String[] entitiesList = entities.split(",");
        if (decomposition.clusterNameExists(newCluster)) throw new KeyAlreadyExistsException("Cluster with name: " + newCluster + " already exists");

        Cluster currentCluster = decomposition.getCluster(originalCluster);
        Cluster createdCluster = new DefaultCluster(newCluster);

        for (String stringifiedEntityID : entitiesList) {
            Element entity = currentCluster.getElementByID(Short.parseShort(stringifiedEntityID));

            if (entity != null) {
                createdCluster.addElement(entity);
                currentCluster.removeElement(entity);
            }
        }
        for (Cluster cluster : decomposition.getClusters().values())
            cluster.transferCouplingDependencies(createdCluster.getElementsIDs(), currentCluster.getName(), createdCluster.getName());

        decomposition.addCluster(createdCluster);
    }

    public String getOriginalCluster() {
        return originalCluster;
    }

    public void setOriginalCluster(String originalCluster) {
        this.originalCluster = originalCluster;
    }

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}
