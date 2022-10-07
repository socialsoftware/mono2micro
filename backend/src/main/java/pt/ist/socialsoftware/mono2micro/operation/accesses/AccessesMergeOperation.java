package pt.ist.socialsoftware.mono2micro.operation.accesses;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.operation.MergeOperation;

import java.util.stream.Collectors;

public class AccessesMergeOperation extends MergeOperation {
    public static final String ACCESSES_MERGE = "AccessesMerge";
    private String cluster1Entities;
    private String cluster2Entities;

    public AccessesMergeOperation() {}

    public void addEntities(AccessesDecomposition accessesDecomposition) {
        Cluster cluster1 = accessesDecomposition.getCluster(this.cluster1Name);
        Cluster cluster2 = accessesDecomposition.getCluster(this.cluster2Name);
        cluster1Entities = cluster1.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
        cluster2Entities = cluster2.getElementsIDs().stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public String getOperationType() {
        return ACCESSES_MERGE;
    }

    public String getCluster1Entities() {
        return cluster1Entities;
    }

    public void setCluster1Entities(String cluster1Entities) {
        this.cluster1Entities = cluster1Entities;
    }

    public String getCluster2Entities() {
        return cluster2Entities;
    }

    public void setCluster2Entities(String cluster2Entities) {
        this.cluster2Entities = cluster2Entities;
    }
}
