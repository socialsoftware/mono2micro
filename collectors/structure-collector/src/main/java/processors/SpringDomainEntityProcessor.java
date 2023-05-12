package processors;

import domain.DomainEntity;
import domain.relationships.CompositionRelationship;
import domain.relationships.InheritanceRelationship;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

public class SpringDomainEntityProcessor extends AbstractProcessor<CtClass<?>> {

    private static final String DOMAIN_ENTITY_ANNOTATION_REGEX = "^(Entity)|(MappedSuperclass)|(Embeddable)$";

    private final ASTCache astCache;

    private DomainEntity currDomainEntity;

    public SpringDomainEntityProcessor(ASTCache astCache) {
        super();
        this.astCache = astCache;
        this.currDomainEntity = null;
    }

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        return ProcessorUtils.isAnnotatedWith(candidate, DOMAIN_ENTITY_ANNOTATION_REGEX);
    }

    @Override
    public void process(CtClass<?> element) {
        this.currDomainEntity = new DomainEntity(element);
        processFields(element.getFields());
        processSuperClass(element.getSuperclass());
        processSuperInterfaces(element.getSuperInterfaces());
        this.astCache.addDomainEntity(currDomainEntity);
    }
    private void processFields(List<CtField<?>> fields) {
        for (CtField<?> ctField : fields) {

            if (fieldHasTypeParameters(ctField)) {
                this.currDomainEntity.addRelationship(new CompositionRelationship(
                        ctField.getType().getActualTypeArguments().get(0).getSimpleName(),
                        ctField.getSimpleName(),
                        CompositionRelationship.ContainerType.parseString(ctField.getType().getSimpleName())));
            } else {
                this.currDomainEntity.addRelationship(new CompositionRelationship(
                        ctField.getType().getSimpleName(),
                        ctField.getSimpleName()));
            }
        }
    }

    private boolean fieldHasTypeParameters(CtField<?> ctField) {
        return ctField.getType().getActualTypeArguments().size() > 0;
    }

    // WARNING : Maps are not being considered, since CML cannot represent collections with more than 1 type parameter
    private String processFieldTypeParameters(CtTypeReference<?> ctFieldType) {
        for (CtTypeReference<?> typeArgument : ctFieldType.getActualTypeArguments()) {
            return typeArgument.getSimpleName();
        }
        return ctFieldType.getSimpleName();
    }

    private void processSuperClass(CtTypeReference<?> superClass) {
        if (superClass != null) {
            this.currDomainEntity.addRelationship(new InheritanceRelationship(
                    superClass.getSimpleName(),
                    InheritanceRelationship.InheritanceType.EXTENDS));
        }
    }

    private void processSuperInterfaces(Set<CtTypeReference<?>> superInterfaces) {
        for (CtTypeReference<?> superInterface : superInterfaces) {
            this.currDomainEntity.addRelationship(new InheritanceRelationship(
                    superInterface.getSimpleName(),
                    InheritanceRelationship.InheritanceType.IMPLEMENTS));
        }
    }

    @Override
    public void processingDone() {
        System.out.println("Number of Domain Entities: " + astCache.getDomainEntities().size());
        for (DomainEntity domainEntity : astCache.getDomainEntities().values()) {
            System.out.println(domainEntity.getSimpleName());
        }
    }
}
