package pt.ist.socialsoftware.mono2micro.history.model;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.List;
import java.util.stream.Collectors;

@Document("decompositionhistory")
public class DecompositionHistory {
    private String codebaseName;

    private String strategyName;

    private String decompositionName;

    private Long currentHistoryEntryDepth;

    private List<HistoryEntry> historyEntryList;

    public DecompositionHistory() {}

    public DecompositionHistory(Decomposition decomposition) {
        this.codebaseName = decomposition.getCodebaseName();
        this.strategyName = decomposition.getStrategyName();
        this.decompositionName = decomposition.getName();
        this.currentHistoryEntryDepth = 0L;
    }

    public DecompositionHistory(String codebaseName, String strategyName, String decompositionName) {
        this.codebaseName = codebaseName;
        this.strategyName = strategyName;
        this.decompositionName = decompositionName;
        this.currentHistoryEntryDepth = 0L;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getDecompositionName() {
        return decompositionName;
    }

    public void setDecompositionName(String decompositionName) {
        this.decompositionName = decompositionName;
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