package pt.ist.socialsoftware.mono2micro.operation;

public abstract class FormClusterOperation extends Operation {
    protected String newCluster;

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }
}
