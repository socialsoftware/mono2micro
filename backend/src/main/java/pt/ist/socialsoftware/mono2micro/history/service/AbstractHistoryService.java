package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionHistory;
import pt.ist.socialsoftware.mono2micro.history.model.HistoryEntry;
import pt.ist.socialsoftware.mono2micro.history.repository.HistoryRepository;

import java.util.ArrayList;

public abstract class AbstractHistoryService {

    @Autowired
    HistoryRepository historyRepository;

    public DecompositionHistory getDecompositionHistory(Decomposition decomposition) {
        return historyRepository.findByDecomposition(decomposition.getCodebaseName(), decomposition.getStrategyName(), decomposition.getName());
    }

    public void deleteAll() {
        historyRepository.deleteAll();
    }

    public void deleteAllCodebaseHistory(String codebaseName) {
        historyRepository.deleteByCodebase(codebaseName);
    }

    public void deleteAllStrategyHistory(String codebaseName, String strategyName) {
        historyRepository.deleteByStrategy(codebaseName, strategyName);
    }

    public void deleteDecompositionHistory(String codebaseName, String strategyName, String decompositionName) {
        historyRepository.deleteByDecomposition(codebaseName, strategyName, decompositionName);
    }

    public void deleteDecompositionHistory(Decomposition decomposition) {
        historyRepository.deleteByDecomposition(decomposition.getCodebaseName(), decomposition.getStrategyName(), decomposition.getName());
    }

    public void addHistoryEntry(Decomposition decomposition, HistoryEntry historyEntry) {
        DecompositionHistory decompositionHistory = getDecompositionHistory(decomposition);
        if (decompositionHistory == null) {
            decompositionHistory = new DecompositionHistory(decomposition);
            decompositionHistory.setHistoryEntryList(new ArrayList<>());
        }
        decompositionHistory.addHistoryEntry(historyEntry);
        historyRepository.save(decompositionHistory);
    }

    public abstract void undoOperation(Decomposition decomposition);
}