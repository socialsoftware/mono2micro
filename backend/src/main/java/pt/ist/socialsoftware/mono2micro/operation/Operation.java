package pt.ist.socialsoftware.mono2micro.operation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesTransferOperation;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RenameOperation.class, name = RenameOperation.RENAME),
        @JsonSubTypes.Type(value = AccessesMergeOperation.class, name = AccessesMergeOperation.ACCESSES_MERGE),
        @JsonSubTypes.Type(value = AccessesSplitOperation.class, name = AccessesSplitOperation.ACCESSES_SPLIT),
        @JsonSubTypes.Type(value = AccessesTransferOperation.class, name = AccessesTransferOperation.ACCESSES_TRANSFER),
        @JsonSubTypes.Type(value = AccessesFormClusterOperation.class, name = AccessesFormClusterOperation.ACCESSES_FORM),
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
