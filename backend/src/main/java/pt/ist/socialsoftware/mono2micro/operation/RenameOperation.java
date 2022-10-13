package pt.ist.socialsoftware.mono2micro.operation;

public class RenameOperation extends Operation {
    public static final String RENAME = "Rename";
    private String clusterName;
    private String newClusterName;

    public RenameOperation() {}

    public RenameOperation(String clusterName, String newClusterName) {
        this.clusterName = clusterName;
        this.newClusterName = newClusterName;
    }

    @Override
    public String getOperationType() {
        return RENAME;
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