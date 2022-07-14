package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionLog;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionOperation;
import pt.ist.socialsoftware.mono2micro.history.repository.LogRepository;

public abstract class AbstractLogService {

    @Autowired
    LogRepository logRepository;

    public void deleteAll() {
        logRepository.deleteAll();
    }

    public void deleteDecompositionHistory(DecompositionLog decompositionLog) {
        logRepository.deleteByName(decompositionLog.getName());
    }

    public abstract void addDecompositionOperation(Decomposition decomposition, DecompositionOperation decompositionOperation);

    public abstract void undoOperation(Decomposition decomposition);
}