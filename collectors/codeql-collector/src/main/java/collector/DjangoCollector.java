package collector;

import collector.utils.Function;

import java.util.ArrayList;

public class DjangoCollector extends AbstractStructuralCollector {

    public DjangoCollector(Configuration config) {
        super(config);
    }

    @Override
    protected void checkForAccesses(String controllerMethodName, Function m) {
        accessMap.getOrDefault(m.getFunctionId(), new ArrayList<>())
            .forEach(access -> addEntitySequenceAccess(controllerMethodName, access.getEntity().getId(), access.getMode()));
    }
}
