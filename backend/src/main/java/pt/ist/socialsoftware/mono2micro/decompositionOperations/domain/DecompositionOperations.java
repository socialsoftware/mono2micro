package pt.ist.socialsoftware.mono2micro.decompositionOperations.domain;

import org.springframework.data.annotation.Id;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import java.util.List;

public abstract class DecompositionOperations {
    @Id
    String name;

    Long currentLogOperationDepth;

    List<Operation> logOperationList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getType();

    public Long getCurrentLogOperationDepth() {
        return currentLogOperationDepth;
    }

    public void setCurrentLogOperationDepth(Long currentLogOperationDepth) {
        this.currentLogOperationDepth = currentLogOperationDepth;
    }

    public Long incrementCurrentLogDepth() {
        return ++this.currentLogOperationDepth;
    }

    public Long decrementCurrentLogDepth() {
        return --this.currentLogOperationDepth;
    }

    public List<Operation> getLogOperationList() {
        return logOperationList;
    }

    public int getMaxLogDepth() {
        return this.logOperationList.size();
    }

    public Operation getLogOperationByDepth(Long historyDepth) {
        return logOperationList.get(Math.toIntExact(historyDepth - 1));
    }

    public void setLogOperationsList(List<Operation> logOperationList) {
        this.logOperationList = logOperationList;
    }

    public Operation getCurrentLogOperation() {
        return logOperationList.get(Math.toIntExact(getCurrentLogOperationDepth()) - 1);
    }
}