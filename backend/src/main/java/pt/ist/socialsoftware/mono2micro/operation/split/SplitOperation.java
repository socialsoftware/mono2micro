package pt.ist.socialsoftware.mono2micro.operation.split;

import pt.ist.socialsoftware.mono2micro.operation.Operation;

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
