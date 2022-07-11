package pt.ist.socialsoftware.mono2micro.history.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.List;
import java.util.stream.Collectors;

@Document("decompositionhistory")
public class DecompositionHistory {
    @Id
    private String name;

    @DBRef(lazy = true)
    private Decomposition decomposition;

    private Long currentHistoryEntryDepth;

    private List<HistoryEntry> historyEntryList;

    public DecompositionHistory() {}

    public DecompositionHistory(Decomposition decomposition) {
        this.name = decomposition.getName();
        this.decomposition = decomposition;
        this.currentHistoryEntryDepth = 0L;
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

    public Long getCurrentHistoryEntryDepth() {
        return currentHistoryEntryDepth;
    }

    public void setCurrentHistoryEntryDepth(Long currentHistoryEntryDepth) {
        this.currentHistoryEntryDepth = currentHistoryEntryDepth;
    }

    public Long incrementCurrentHistoryEntry() {
        return ++this.currentHistoryEntryDepth;
    }

    public Long decrementCurrentHistoryEntry() {
        return --this.currentHistoryEntryDepth;
    }

    public List<HistoryEntry> getHistoryEntryList() {
        return historyEntryList;
    }

    public HistoryEntry getHistoryEntryByDepth(Long historyDepth) {
        return historyEntryList.get(Math.toIntExact(historyDepth - 1));
    }

    public void setHistoryEntryList(List<HistoryEntry> historyEntryList) {
        this.historyEntryList = historyEntryList;
    }

    public void addHistoryEntry(HistoryEntry historyEntry) {
        historyEntry.setHistoryDepth(incrementCurrentHistoryEntry());
        // Remove conflicting HistoryEntries that have been overridden by the new operation
        setHistoryEntryList(this.historyEntryList.stream().filter(entry -> entry.getHistoryDepth() < getCurrentHistoryEntryDepth()).collect(Collectors.toList()));
        this.historyEntryList.add(historyEntry);
    }

    public HistoryEntry getCurrentHistoryEntry() {
        return historyEntryList.get(Math.toIntExact(getCurrentHistoryEntryDepth() - 1));
    }
}