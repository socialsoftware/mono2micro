package pt.ist.socialsoftware.mono2micro.operation.transfer;

import pt.ist.socialsoftware.mono2micro.operation.Operation;

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

    @Override
    public String getOperationType() {
        return TRANSFER_OPERATION;
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