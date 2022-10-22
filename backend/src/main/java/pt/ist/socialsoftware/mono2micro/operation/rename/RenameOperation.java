package pt.ist.socialsoftware.mono2micro.operation.rename;

import pt.ist.socialsoftware.mono2micro.operation.Operation;

public class RenameOperation extends Operation {
    public static final String RENAME_OPERATION = "RenameOperation";
    protected String clusterName;
    protected String newClusterName;

    public RenameOperation() {}

    public RenameOperation(RenameOperation renameOperation) {
        this.clusterName = renameOperation.getClusterName();
        this.newClusterName = renameOperation.getNewClusterName();
    }

    @Override
    public String getOperationType() {
        return RENAME_OPERATION;
    }

    public String getNewClusterName() {
        return newClusterName;
    }

    public void setNewClusterName(String newClusterName) {
        this.newClusterName = newClusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}