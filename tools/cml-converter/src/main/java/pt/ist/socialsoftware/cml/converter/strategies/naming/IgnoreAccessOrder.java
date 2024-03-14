package pt.ist.socialsoftware.cml.converter.strategies.naming;

import pt.ist.socialsoftware.cml.converter.domain.EntityAccess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Use when resulting entity names should not take into account access type or access order.
 * All entity access types are replaced with a generic access (ac).
 */
public class IgnoreAccessOrder extends IgnoreAccessTypes {

    private final Set<String> operationNameComponents;

    public IgnoreAccessOrder(List<EntityAccess> entityAccesses) {
        super(entityAccesses);
        this.operationNameComponents = new HashSet<>();
    }

    @Override
    public void createOperationNameComponent(EntityAccess entityAccess) {
        operationNameComponents.add(resolveOperationNameComponent(entityAccess));
    }

    @Override
    public String resolveOperationName() {
        for (String operationNameComponent : operationNameComponents) {
            appendToOperationName("_");
            appendToOperationName(operationNameComponent);
        }
        return super.resolveOperationName();
    }
}
