package pt.ist.socialsoftware.cml.converter.strategies.naming;

import pt.ist.socialsoftware.cml.converter.domain.EntityAccess;

import java.util.List;

/**
 * Use when resulting operation names should not take into account the type of entity access (r, w, rw).
 * All entity access types are replaced with a generic access (ac).
 */
public class IgnoreAccessTypes extends AccessBasedOperationNamingStrategy {

    public IgnoreAccessTypes(List<EntityAccess> entityAccesses) {
        super(entityAccesses);
    }

    @Override
    public String resolveOperationNameComponent(EntityAccess entityAccess) {
        return "ac" + entityAccess.getEntity();
    }
}
