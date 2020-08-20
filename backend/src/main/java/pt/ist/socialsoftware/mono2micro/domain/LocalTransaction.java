package pt.ist.socialsoftware.mono2micro.domain;


import java.util.ArrayList;
import java.util.List;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;

public class LocalTransaction {

    private String name;
    private String id;
    private String cluster;
    private String accessedEntities = "";
    private List<Integer> remoteInvocations = new ArrayList<>();
    private LocalTransactionTypes type;

    public LocalTransaction(){}

    public LocalTransaction(String id, String cluster, String entities, List<Integer> remoteInvocations, String name){
        this.id = id;
        this.cluster = cluster;
        this.accessedEntities = entities;
        this.remoteInvocations = remoteInvocations;
        this.type = LocalTransactionTypes.COMPENSATABLE;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getAccessedEntities() {
        return accessedEntities;
    }

    public void setAccessedEntities(String accessedEntities) {
        this.accessedEntities = accessedEntities;
    }

    public List<Integer> getRemoteInvocations() {
        return remoteInvocations;
    }

    public void setRemoteInvocations(List<Integer> remoteInvocations) {
        this.remoteInvocations = remoteInvocations;
    }

    public LocalTransactionTypes getType() {
        return type;
    }

    public void setType(LocalTransactionTypes type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
