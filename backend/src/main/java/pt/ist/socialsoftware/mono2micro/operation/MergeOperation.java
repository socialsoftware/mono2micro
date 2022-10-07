package pt.ist.socialsoftware.mono2micro.operation;

public abstract class MergeOperation extends Operation {
    protected String cluster1Name;
    protected String cluster2Name;
    protected String newName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getCluster1Name() {
        return cluster1Name;
    }

    public void setCluster1Name(String cluster1Name) {
        this.cluster1Name = cluster1Name;
    }

    public String getCluster2Name() {
        return cluster2Name;
    }

    public void setCluster2Name(String cluster2Name) {
        this.cluster2Name = cluster2Name;
    }
}
