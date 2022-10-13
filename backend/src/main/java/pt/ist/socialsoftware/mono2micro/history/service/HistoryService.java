package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.history.domain.History;
import pt.ist.socialsoftware.mono2micro.history.repository.HistoryRepository;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

import java.util.HashMap;
import java.util.Map;

@Service
public class HistoryService {

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    HistoryRepository historyRepository;

    public void saveHistory(History operations) {
        historyRepository.save(operations);
    }

    public void deleteHistory(History history) {
        history.deleteProperties();
        historyRepository.deleteByName(history.getName());
    }

    public void addOperation(Decomposition decomposition, Operation operation) {
        History history = decomposition.getHistory();

        Long newHistoryOperationDepth = history.incrementCurrentHistoryDepth();
        operation.setHistoryDepth(newHistoryOperationDepth);

        // Remove conflicting Decomposition operations that have been overridden by the new operation
        history.removeOverriddenOperations(newHistoryOperationDepth);

        history.addOperation(operation);
        historyRepository.save(history);
    }
    public void undoOperation(Decomposition decomposition) {
        History history = decomposition.getHistory();
        if (history.getCurrentHistoryOperationDepth() == 0)
            throw new RuntimeException("No more operations to undo");

        decomposition.undoOperation(history.getCurrentHistoryOperation());

        history.decrementCurrentHistoryDepth();

        historyRepository.save(history);
        decompositionRepository.save(decomposition);
    }

    public void redoOperation(Decomposition decomposition) {
        History history = decomposition.getHistory();
        if (history.getCurrentHistoryOperationDepth() == history.getMaxHistoryDepth())
            throw new RuntimeException("No more operations to redo");
        history.incrementCurrentHistoryDepth();

        decomposition.redoOperation(history.getCurrentHistoryOperation());

        historyRepository.save(history);
        decompositionRepository.save(decomposition);
    }

    public Map<String, Boolean> canUndoRedo(Decomposition decomposition) {
        History history = decomposition.getHistory();
        Map<String, Boolean> canUndoRedo = new HashMap<>();
        if (history.getMaxHistoryDepth() == history.getCurrentHistoryOperationDepth())
            canUndoRedo.put("redo", false);
        else canUndoRedo.put("redo", true);
        if (history.getCurrentHistoryOperationDepth() == 0)
            canUndoRedo.put("undo", false);
        else canUndoRedo.put("undo", true);
        return canUndoRedo;
    }
}
