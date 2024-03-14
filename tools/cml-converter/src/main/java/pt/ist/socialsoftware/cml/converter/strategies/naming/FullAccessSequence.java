package pt.ist.socialsoftware.cml.converter.strategies.naming;

import pt.ist.socialsoftware.cml.converter.domain.EntityAccess;

import java.util.List;

/**
 * Use when resulting entity names should not be refactored.
 */
public class FullAccessSequence extends AccessBasedOperationNamingStrategy {

    public FullAccessSequence(List<EntityAccess> entityAccesses) {
        super(entityAccesses);
    }

    @Override
    protected String resolveOperationNameComponent(EntityAccess entityAccess) {
        return entityAccess.getName();
    }
}
