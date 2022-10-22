package pt.ist.socialsoftware.mono2micro.operation.formCluster;

import pt.ist.socialsoftware.mono2micro.operation.Operation;

import java.util.List;
import java.util.Map;

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
