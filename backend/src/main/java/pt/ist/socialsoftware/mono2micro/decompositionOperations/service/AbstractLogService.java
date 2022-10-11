package pt.ist.socialsoftware.mono2micro.decompositionOperations.service;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.operation.Operation;

public abstract class AbstractLogService {

    public abstract void addOperation(Decomposition decomposition, Operation operation);

    public abstract void undoOperation(Decomposition decomposition);

    public abstract void redoOperation(Decomposition decomposition);
}