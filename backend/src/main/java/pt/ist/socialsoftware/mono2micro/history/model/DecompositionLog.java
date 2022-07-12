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

    private Long currentHistoryOperationDepth;

    private List<DecompositionOperation> decompositionOperationList;

    public DecompositionLog() {}

    public DecompositionLog(Decomposition decomposition) {
        this.name = decomposition.getName();
        this.decomposition = decomposition;
        this.decompositionOperationList = new ArrayList<>();
        this.currentHistoryOperationDepth = 0L;
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

    public Long getCurrentHistoryOperationDepth() {
        return currentHistoryOperationDepth;
    }

    public void setCurrentHistoryOperationDepth(Long currentHistoryOperationDepth) {
        this.currentHistoryOperationDepth = currentHistoryOperationDepth;
    }

    public Long incrementCurrentHistoryEntry() {
        return ++this.currentHistoryOperationDepth;
    }

    public Long decrementCurrentHistoryEntry() {
        return --this.currentHistoryOperationDepth;
    }

    public List<DecompositionOperation> getHistoryEntryList() {
        return decompositionOperationList;
    }

    public DecompositionOperation getHistoryEntryByDepth(Long historyDepth) {
        return decompositionOperationList.get(Math.toIntExact(historyDepth - 1));
    }

    public void setHistoryEntryList(List<DecompositionOperation> decompositionOperationList) {
        this.decompositionOperationList = decompositionOperationList;
    }

    public void addHistoryEntry(DecompositionOperation decompositionOperation) {
        decompositionOperation.setHistoryDepth(incrementCurrentHistoryEntry());
        // Remove conflicting HistoryEntries that have been overridden by the new operation
        setHistoryEntryList(this.decompositionOperationList.stream().filter(entry -> entry.getHistoryDepth() < getCurrentHistoryOperationDepth()).collect(Collectors.toList()));
        this.decompositionOperationList.add(decompositionOperation);
    }

    public DecompositionOperation getCurrentHistoryEntry() {
        return decompositionOperationList.get(Math.toIntExact(getCurrentHistoryOperationDepth() - 1));
    }
}