package pt.ist.socialsoftware.mono2micro.domain;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class LocalTransaction {

    private String id;
    private String cluster;
    private String accessedEntities = "";
    private List<Integer> remoteInvocations = new ArrayList<>();

    public LocalTransaction(){}

    public LocalTransaction(String id, String cluster, String entities, List<Integer> remoteInvocations){
        this.id = id;
        this.cluster = cluster;
        this.accessedEntities = entities;
        this.remoteInvocations = remoteInvocations;
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
}
