package pt.ist.socialsoftware.mono2micro.history.model;

public abstract class DecompositionOperation {
    private Long historyDepth;

    public abstract String getOperationType();

    public Long getHistoryDepth() {
        return historyDepth;
    }

    public void setHistoryDepth(Long historyDepth) {
        this.historyDepth = historyDepth;
    }
}