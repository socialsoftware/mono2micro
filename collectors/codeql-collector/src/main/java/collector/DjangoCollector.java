package collector;

import collector.django.DjangoFileParser;
import collector.utils.Method;

import java.util.ArrayList;

public class DjangoCollector extends AbstractStructuralCollector {

    public DjangoCollector(String codeQLDbPath, String projectName, boolean runQueries) {
        super(codeQLDbPath, projectName, runQueries, new DjangoFileParser());
        SPECIFIC_FRAMEWORK_PATH = Constants.DJANGO;
    }

    @Override
    protected void buildPrevCalleeQualiferMap() {
        // Not needed for this framework
    }

    @Override
    protected void checkForAccesses(String controllerMethodName, Method m) {
        accessMap.getOrDefault(m.getFullMethodName(), new ArrayList<>())
            .forEach(access -> addEntitySequenceAccess(controllerMethodName, access.getEntity().getId(), access.getMode()));
    }
}
