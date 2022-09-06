package pt.ist.socialsoftware.mono2micro.log.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.List;

public abstract class Log {
    @Id
    String name;

    @DBRef(lazy = true)
    Decomposition decomposition;

    Long currentLogOperationDepth;

    List<Operation> logOperationList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Decomposition getDecomposition() {
        return decomposition;
    }

    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
    }

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