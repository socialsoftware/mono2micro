package processors.fenixframework;

import collectors.SpoonCollector;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

/**
 * Responsible for finding and processing domain entity classes represented with the FenixFramework during the Spoon AST traversal.
 */
public class FenixFrameworkDomainEntityProcessor extends AbstractProcessor<CtClass<?>> {

    private static final String DOMAIN_ENTITY_SUPERCLASS_SUFFIX = "_Base";
    private SpoonCollector collector;

    public FenixFrameworkDomainEntityProcessor(SpoonCollector collector) {
        this.collector = collector;
    }

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        CtTypeReference<?> superclass = candidate.getSuperclass();
        return superclass != null && superclass.getSimpleName().endsWith(DOMAIN_ENTITY_SUPERCLASS_SUFFIX);
    }

    @Override
    public void process(CtClass<?> ctClass) {

    }

    @Override
    public void processingDone() {
        super.processingDone();
    }
}
