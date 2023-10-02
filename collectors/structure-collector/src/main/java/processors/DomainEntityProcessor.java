package processors;

import domain.DomainEntity;
import domain.Field;
import domain.datatypes.DataType;
import domain.datatypes.DataTypeFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import collectors.DomainEntityCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract spoon AST processor that processes domain entity classes.
 * The conditions for a class to be considered a domain entity can be specified by
 * overriding {@link #isToBeProcessed} in a subclass.
 */
public abstract class DomainEntityProcessor extends AbstractProcessor<CtClass<?>> {

    private final DomainEntityCollector collector;
    private boolean fieldProcessing;
    private boolean inheritanceProcessing;

    public DomainEntityProcessor(DomainEntityCollector collector) {
        this.collector = collector;
        setFieldProcessing(true);
        setInheritanceProcessing(true);
    }

    public void setFieldProcessing(boolean fieldProcessing) {
        this.fieldProcessing = fieldProcessing;
    }

    public void setInheritanceProcessing(boolean inheritanceProcessing) {
        this.inheritanceProcessing = inheritanceProcessing;
    }

    @Override
    public void init() {
        super.init();
        System.out.println("Processing Domain Entities...");
    }

    @Override
    public abstract boolean isToBeProcessed(CtClass<?> candidate);

    @Override
    public void process(CtClass<?> ctClass) {
        collector.addDomainEntity(processDomainEntity(ctClass));
    }

    protected DomainEntity processDomainEntity(CtClass<?> ctClass) {
        DomainEntity domainEntity = new DomainEntity(ctClass.getSimpleName());

        if (this.fieldProcessing)
            domainEntity.addFields(processFields(ctClass));
        if (this.inheritanceProcessing)
            domainEntity.setSuperclass(processSuperclass(ctClass));

        return domainEntity;
    }

    protected List<Field> processFields(CtClass<?> ctClass) {
        List<Field> fields = new ArrayList<>();
        for (CtField<?> ctField : ctClass.getFields()) {
            fields.add(processField(ctField));
        }
        return fields;
    }

    protected Field processField(CtField<?> ctField) {
        return new Field(ctField.getSimpleName(), DataTypeFactory.getOrCreateDataType(ctField.getType()));
    }

    protected DataType processSuperclass(CtClass<?> ctClass) {
        return ctClass.getSuperclass() != null ? DataTypeFactory.getOrCreateDataType(ctClass.getSuperclass()) : null;
    }

    @Override
    public void processingDone() {
        System.out.println("Processed " + collector.getDomainEntitiesSize() + " Domain Entities:");
        for (DomainEntity domainEntity : collector.getDomainEntities()) {
            System.out.println(domainEntity.getName());
        }
    }
}
