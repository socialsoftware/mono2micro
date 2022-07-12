package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionLog;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionOperation;
import pt.ist.socialsoftware.mono2micro.history.repository.HistoryRepository;

public abstract class AbstractHistoryService {

    @Autowired
    HistoryRepository historyRepository;

    public void deleteAll() {
        historyRepository.deleteAll();
    }

    public void deleteDecompositionHistory(DecompositionLog decompositionLog) {
        historyRepository.deleteByName(decompositionLog.getName());
    }

    public abstract void addHistoryEntry(Decomposition decomposition, DecompositionOperation decompositionOperation);

    public abstract void undoOperation(Decomposition decomposition);
}