package pt.ist.socialsoftware.cml.converter.strategies;

import org.contextmapper.discovery.model.*;
import org.contextmapper.discovery.strategies.names.BoundedContextNameMappingStrategy;

import java.util.HashMap;
import java.util.Map;

public class MostAccessedEntityBoundedContextNameMappingStrategy implements BoundedContextNameMappingStrategy {

    private BoundedContext boundedContext;
    private Map<DomainObject, Integer> entityCount;

    public MostAccessedEntityBoundedContextNameMappingStrategy(BoundedContext boundedContext) {
        this.boundedContext = boundedContext;
        this.entityCount = new HashMap<>();
    }

    @Override
    public String mapBoundedContextName(String s) {
        boundedContext.getApplication().getServices().forEach(this::countEntityAccesses);
        return s;
    }

    private void countEntityAccesses(Service service) {
        for (Method operation : service.getOperations()) {
            for (Access access : operation.getAccesses()) {
                Integer num = entityCount.get(access.getEntity());
                entityCount.put(access.getEntity(), ++num);
            }
        }
    }
}
