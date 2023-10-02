package processors.fenixframework;

import domain.Field;
import domain.datatypes.DataTypeFactory;
import processors.DomainEntityProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import collectors.DomainEntityCollector;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected List<Field> processFields(CtClass<?> ctClass) {
        List<Field> fields = new ArrayList<>();
        for (CtMethod<?> ctMethod : ctClass.getSuperclass().getDeclaration().getMethods()) {
            if (ctMethod.getSimpleName().startsWith("get")) { // TODO might need to filter out API fields
                fields.add(processAccessorMethod(ctMethod));
            }
        }
        return fields;
    }

    private Field processAccessorMethod(CtMethod<?> ctMethod) {
        return new Field(getFieldName(ctMethod), DataTypeFactory.getOrCreateDataType(ctMethod.getType()));
    }

    private String getFieldName(CtMethod<?> ctMethod) {
        String fieldName = ctMethod.getSimpleName().substring(3);
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }
}
