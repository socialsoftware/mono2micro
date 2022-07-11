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

    public void deleteAll() {
        historyRepository.deleteAll();
    }

    public void deleteDecompositionHistory(Decomposition decomposition) {
        historyRepository.deleteByName(decomposition.getName());
    }

    public abstract void addHistoryEntry(Decomposition decomposition, HistoryEntry historyEntry);

    public abstract void undoOperation(Decomposition decomposition);
}