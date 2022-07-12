package pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

import pt.ist.socialsoftware.mono2micro.decomposition.dto.accessesSciPyDtos.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class LocalTransaction {

    private String name;
    private int id;
    private short clusterID;
    private Set<AccessDto> clusterAccesses;
    private List<Integer> remoteInvocations;
    private Set<Short> firstAccessedEntityIDs;
    private LocalTransactionTypes type = LocalTransactionTypes.COMPENSATABLE;

    public LocalTransaction(){}

    public LocalTransaction(
        int id,
        short clusterID,
        Set<AccessDto> clusterAccesses,
        List<Integer> remoteInvocations,
        String name
    ){
        this.id = id;
        this.name = name;
        this.clusterID = clusterID;
        this.clusterAccesses = clusterAccesses;
        this.remoteInvocations = remoteInvocations;
    }

    public LocalTransaction(
        int id,
        short clusterID
    ) {
        this.id = id;
        this.clusterID = clusterID;
    }

    public LocalTransaction(
        int id,
        short clusterID,
        Set<AccessDto> clusterAccesses,
        short firstAccessedEntityID
    ) {
        this.id = id;
        this.clusterID = clusterID;
        this.clusterAccesses = clusterAccesses;
        this.firstAccessedEntityIDs = new HashSet<Short>() {
            { add(firstAccessedEntityID); }
        };
    }

    public LocalTransaction(LocalTransaction lt) {
        this.id = lt.getId();
        this.clusterID = lt.getClusterID();
        this.clusterAccesses = new HashSet<>(lt.getClusterAccesses());
        this.firstAccessedEntityIDs = new HashSet<>(lt.getFirstAccessedEntityIDs());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getClusterID() {
        return clusterID;
    }

    public void setClusterID(short clusterID) {
        this.clusterID = clusterID;
    }

    public Set<AccessDto> getClusterAccesses() {
        return clusterAccesses;
    }

    public void setClusterAccesses(Set<AccessDto> clusterAccesses) {
        this.clusterAccesses = clusterAccesses;
    }

    public void addClusterAccess(AccessDto a) { this.clusterAccesses.add(a); }

    public Set<Short> getFirstAccessedEntityIDs() {
        return firstAccessedEntityIDs;
    }

    public void setFirstAccessedEntityIDs(Set<Short> firstAccessedEntityIDs) {
        this.firstAccessedEntityIDs = firstAccessedEntityIDs;
    }

    public List<Integer> getRemoteInvocations() {
        return remoteInvocations;
    }

    public void setRemoteInvocations(List<Integer> remoteInvocations) {
        this.remoteInvocations = remoteInvocations;
    }

    public void addRemoteInvocations(int remoteInvocation) {
        this.remoteInvocations.add(remoteInvocation);
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

    @Override
    public boolean equals(Object other) {
        if (other instanceof LocalTransaction) {
            LocalTransaction that = (LocalTransaction) other;
            return id == that.id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
