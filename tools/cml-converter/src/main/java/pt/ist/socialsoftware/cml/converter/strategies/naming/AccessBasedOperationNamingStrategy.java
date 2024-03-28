package pt.ist.socialsoftware.cml.converter.strategies.naming;

import pt.ist.socialsoftware.cml.converter.domain.EntityAccess;

import java.util.Collections;
import java.util.List;

/**
 * Use when resulting operation names should represent the sequence of entity accesses that occur within the operation.
 * The name is composed of segments seperated by '_', where each segment shows the access type (r, w, rw)
 * and the entity name.
 * Subclass for new naming heuristics based on entity access traces.
 */
public abstract class AccessBasedOperationNamingStrategy implements OperationNamingStrategy {

    private final List<EntityAccess> entityAccesses;
    private final StringBuilder operationName;

    public AccessBasedOperationNamingStrategy(List<EntityAccess> entityAccesses) {
        this.entityAccesses = entityAccesses;
        this.operationName = new StringBuilder();
    }

    protected List<EntityAccess> getEntityAccesses() {
        return Collections.unmodifiableList(entityAccesses);
    }

    protected void appendToOperationName(String operationNameComponent) {
        operationName.append(operationNameComponent);
    }

    @Override
    public String createOperationName() {
        EntityAccess lastEntityAccess = new EntityAccess();
        for (EntityAccess entityAccess : getEntityAccesses()) {
            if (!entityAccess.hasEntity(lastEntityAccess.getEntity())) {
                createOperationNameComponent(entityAccess);
            }
            lastEntityAccess = entityAccess;
        }
        return resolveOperationName();
    }
    protected void createOperationNameComponent(EntityAccess entityAccess) {
        operationName.append("_");
        operationName.append(resolveOperationNameComponent(entityAccess));
    }

    protected String resolveOperationName() {
        return operationName.substring(1);
    }

    protected abstract String resolveOperationNameComponent(EntityAccess entityAccess);
}
