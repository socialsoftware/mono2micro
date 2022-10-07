package pt.ist.socialsoftware.mono2micro.operation;

public abstract class SplitOperation extends Operation {
    protected String originalCluster;
    protected String newCluster;

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
}
