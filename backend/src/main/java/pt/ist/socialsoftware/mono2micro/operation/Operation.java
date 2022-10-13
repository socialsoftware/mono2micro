package pt.ist.socialsoftware.mono2micro.operation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewTransferOperation;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RenameOperation.class, name = RenameOperation.RENAME),
        @JsonSubTypes.Type(value = ClusterViewMergeOperation.class, name = ClusterViewMergeOperation.CLUSTER_VIEW_MERGE),
        @JsonSubTypes.Type(value = ClusterViewSplitOperation.class, name = ClusterViewSplitOperation.CLUSTER_VIEW_SPLIT),
        @JsonSubTypes.Type(value = ClusterViewTransferOperation.class, name = ClusterViewTransferOperation.CLUSTER_VIEW_TRANSFER),
        @JsonSubTypes.Type(value = ClusterViewFormClusterOperation.class, name = ClusterViewFormClusterOperation.CLUSTER_VIEW_FORM),
})
public abstract class Operation {
    protected Long historyDepth;

    public abstract String getOperationType();

    public Long getHistoryDepth() {
        return historyDepth;
    }

    public void setHistoryDepth(Long historyDepth) {
        this.historyDepth = historyDepth;
    }
}
