package processors.fenixframework;

import processors.DomainEntityProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;
import collectors.DomainEntityCollector;

public class FenixFrameworkDomainEntityProcessor extends DomainEntityProcessor {

    private static final String DOMAIN_ENTITY_SUPERCLASS_SUFFIX = "_Base";

    public FenixFrameworkDomainEntityProcessor(DomainEntityCollector collector) {
        super(collector);
    }

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        CtTypeReference<?> superclass = candidate.getSuperclass();
        return superclass != null && superclass.getSimpleName().endsWith(DOMAIN_ENTITY_SUPERCLASS_SUFFIX);
    }
}
