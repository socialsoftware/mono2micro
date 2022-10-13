package pt.ist.socialsoftware.mono2micro.history.domain;

import org.springframework.data.annotation.Id;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import java.util.List;

public abstract class History {
    @Id
    String name;

    Long currentHistoryOperationDepth;

    List<Operation> historyOperationList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getType();

    public abstract void deleteProperties();
    public abstract void removeOverriddenOperations(Long newHistoryOperationDepth);

    public Long getCurrentHistoryOperationDepth() {
        return currentHistoryOperationDepth;
    }

    public void setCurrentHistoryOperationDepth(Long currentHistoryOperationDepth) {
        this.currentHistoryOperationDepth = currentHistoryOperationDepth;
    }

    public Long incrementCurrentHistoryDepth() {
        return ++this.currentHistoryOperationDepth;
    }

    public Long decrementCurrentHistoryDepth() {
        return --this.currentHistoryOperationDepth;
    }

    public List<Operation> getHistoryOperationList() {
        return historyOperationList;
    }

    public void addOperation(Operation operation) {
        this.historyOperationList.add(operation);
    }

    public int getMaxHistoryDepth() {
        return this.historyOperationList.size();
    }

    public Operation getHistoryOperationByDepth(Long historyDepth) {
        return historyOperationList.get(Math.toIntExact(historyDepth - 1));
    }

    public void setHistoryOperationsList(List<Operation> historyOperationList) {
        this.historyOperationList = historyOperationList;
    }

    public Operation getCurrentHistoryOperation() {
        return historyOperationList.get(Math.toIntExact(getCurrentHistoryOperationDepth()) - 1);
    }
}