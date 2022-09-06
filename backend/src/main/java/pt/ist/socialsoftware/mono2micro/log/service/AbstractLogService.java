package pt.ist.socialsoftware.mono2micro.log.service;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;

public abstract class AbstractLogService {

    public abstract void addOperation(Decomposition decomposition, Operation operation);

    public abstract void undoOperation(Decomposition decomposition);

    public abstract void redoOperation(Decomposition decomposition);
}