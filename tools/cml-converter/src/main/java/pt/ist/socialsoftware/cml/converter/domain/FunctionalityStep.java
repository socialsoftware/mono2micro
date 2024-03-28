package pt.ist.socialsoftware.cml.converter.domain;

import java.util.ArrayList;
import java.util.List;

public class FunctionalityStep {
    private String cluster;
    private List<EntityAccess> accesses;

    public FunctionalityStep() {
        this.accesses = new ArrayList<>();
    }

    public String getCluster() {
        return cluster;
    }

    public List<EntityAccess> getAccesses() {
        return accesses;
    }
}
