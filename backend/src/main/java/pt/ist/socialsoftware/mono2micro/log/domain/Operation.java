package pt.ist.socialsoftware.mono2micro.log.domain;

public abstract class Operation {
    private Long historyDepth;

    public abstract String getOperationType();

    public Long getHistoryDepth() {
        return historyDepth;
    }

    public void setHistoryDepth(Long historyDepth) {
        this.historyDepth = historyDepth;
    }
}