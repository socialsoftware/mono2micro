package pt.ist.socialsoftware.mono2micro.operation.accesses;

import pt.ist.socialsoftware.mono2micro.operation.TransferOperation;

public class AccessesTransferOperation extends TransferOperation {
    public static final String ACCESSES_TRANSFER = "AccessesTransfer";
    private String entities;

    public AccessesTransferOperation() {}

    @Override
    public String getOperationType() {
        return ACCESSES_TRANSFER;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}
