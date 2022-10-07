package pt.ist.socialsoftware.mono2micro.operation;

public abstract class TransferOperation extends Operation {
    protected String fromCluster;
    protected String toCluster;

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
}