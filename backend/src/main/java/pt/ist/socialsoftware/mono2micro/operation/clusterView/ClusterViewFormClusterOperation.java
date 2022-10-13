package pt.ist.socialsoftware.mono2micro.operation.clusterView;

import pt.ist.socialsoftware.mono2micro.operation.FormClusterOperation;

import java.util.List;
import java.util.Map;

public class ClusterViewFormClusterOperation extends FormClusterOperation {
    public static final String CLUSTER_VIEW_FORM = "ClusterViewForm";
    private Map<String, List<Short>> entities;

    public ClusterViewFormClusterOperation() {}

    @Override
    public String getOperationType() {
        return CLUSTER_VIEW_FORM;
    }

    public Map<String, List<Short>> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, List<Short>> entities) {
        this.entities = entities;
    }
}
