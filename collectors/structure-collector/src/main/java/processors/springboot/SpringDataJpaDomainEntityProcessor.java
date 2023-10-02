package processors.springboot;

import processors.DomainEntityProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import collectors.DomainEntityCollector;

public class SpringDataJpaDomainEntityProcessor extends DomainEntityProcessor {

    private static final String DOMAIN_ENTITY_ANNOTATION_REGEX = "(Entity)|(MappedSuperclass)|(Embeddable)";

    public SpringDataJpaDomainEntityProcessor(DomainEntityCollector collector) {
        super(collector);
    }

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        for (CtAnnotation<?> a : candidate.getAnnotations()) {
            if (a.getAnnotationType().getSimpleName().matches(DOMAIN_ENTITY_ANNOTATION_REGEX)) {
                return true;
            }
        }
        return false;
    }
}
