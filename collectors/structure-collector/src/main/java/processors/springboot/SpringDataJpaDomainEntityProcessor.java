package processors.springboot;

import collectors.SpoonCollector;
import domain.DomainEntity;
import domain.Field;
import domain.datatypes.DataTypeFactory;
import processors.ProcessorUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

/**
 * Responsible for finding and processing domain entity classes represented in SpringDataJPA during the Spoon AST traversal.
 */
public class SpringDataJpaDomainEntityProcessor extends AbstractProcessor<CtClass<?>> {

    private static final String DOMAIN_ENTITY_ANNOTATION_REGEX = "(Entity)|(MappedSuperclass)|(Embeddable)";

    private final SpoonCollector collector;

    public SpringDataJpaDomainEntityProcessor(SpoonCollector collector) {
        this.collector = collector;
    }

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        return ProcessorUtils.isAnnotatedWith(candidate, DOMAIN_ENTITY_ANNOTATION_REGEX);
    }

    @Override
    public void process(CtClass<?> ctClass) {
        DomainEntity domainEntity = new DomainEntity(ctClass.getSimpleName());
        processFields(domainEntity, ctClass.getFields());
        processSuperclass(domainEntity, ctClass.getSuperclass());
        collector.addDomainEntity(domainEntity);
    }

    protected void processFields(DomainEntity domainEntity, List<CtField<?>> ctFields) {
        for (CtField<?> ctField : ctFields) {
            domainEntity.addField(new Field(
                    ctField.getSimpleName(),
                    DataTypeFactory.getOrCreateDataType(ctField.getType())));
        }
    }

    protected void processSuperclass(DomainEntity domainEntity, CtTypeReference<?> ctSuperclass) {
        if (ctSuperclass != null) {
            domainEntity.setSuperclass(DataTypeFactory.getOrCreateDataType(ctSuperclass));
        }
    }

    @Override
    public void processingDone() {
        System.out.println("Number of Domain Entities: " + collector.getDomainEntitiesSize());
        for (DomainEntity domainEntity : collector.getDomainEntities()) {
            System.out.println(domainEntity.getName());
        }
    }
}
