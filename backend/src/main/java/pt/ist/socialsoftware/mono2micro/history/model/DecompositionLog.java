package pt.ist.socialsoftware.mono2micro.history.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document("decompositionlog")
public class DecompositionLog {
    @Id
    private String name;

    @DBRef(lazy = true)
    private Decomposition decomposition;

    private Long currentLogOperationDepth;

    private List<DecompositionOperation> logOperationList;

    public DecompositionLog() {}

    public DecompositionLog(Decomposition decomposition) {
        this.name = decomposition.getName();
        this.decomposition = decomposition;
        this.logOperationList = new ArrayList<>();
        this.currentLogOperationDepth = 0L;
    }

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

    public List<DecompositionOperation> getLogOperationList() {
        return logOperationList;
    }

    public DecompositionOperation getLogOperationByDepth(Long historyDepth) {
        return logOperationList.get(Math.toIntExact(historyDepth - 1));
    }

    public void setLogOperationsList(List<DecompositionOperation> logOperationList) {
        this.logOperationList = logOperationList;
    }

    public void addLogOperation(DecompositionOperation decompositionOperation) {
        decompositionOperation.setHistoryDepth(incrementCurrentLogDepth());
        // Remove conflicting Decomposition operations that have been overridden by the new operation
        setLogOperationsList(this.logOperationList.stream().filter(operation -> operation.getHistoryDepth() < getCurrentLogOperationDepth()).collect(Collectors.toList()));
        this.logOperationList.add(decompositionOperation);
    }

    public DecompositionOperation getCurrentLogOperation() {
        return logOperationList.get(Math.toIntExact(getCurrentLogOperationDepth() - 1));
    }
}