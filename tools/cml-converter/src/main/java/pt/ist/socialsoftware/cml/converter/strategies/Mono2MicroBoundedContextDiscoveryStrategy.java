package pt.ist.socialsoftware.cml.converter.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.contextmapper.discovery.model.*;
import org.contextmapper.discovery.strategies.boundedcontexts.AbstractBoundedContextDiscoveryStrategy;
import pt.ist.socialsoftware.cml.converter.domain.*;
import pt.ist.socialsoftware.cml.converter.strategies.naming.FullAccessSequence;
import pt.ist.socialsoftware.cml.converter.strategies.naming.IgnoreAccessOrder;
import pt.ist.socialsoftware.cml.converter.strategies.naming.IgnoreAccessTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Mono2MicroBoundedContextDiscoveryStrategy extends AbstractBoundedContextDiscoveryStrategy {

    private static final String IGNORE_TYPES_OPERATION_DISCOVERY_COMMENT =
            "Operation names were generated to show the ordered sequence of accesses to entities when an operation is called.";
    private static final String IGNORE_ORDER_OPERATION_DISCOVERY_COMMENT =
            "Operation names were generated to show which entities are accessed when an operation is called.";
    private static final String BASIC_OPERATION_DISCOVERY_COMMENT =
            "Operation names were generated to show the ordered sequence of read/write accesses to entities when an operation is called.";

    private final File sourcePath;
    private final Map<String, DomainObject> domainObjectsByName;
    private final Map<String, BoundedContext> boundedContextsByName;
    private final int operationNamingMode;

    public Mono2MicroBoundedContextDiscoveryStrategy(File sourcePath, int operationNamingMode) {
        this.sourcePath = sourcePath;
        this.domainObjectsByName = new HashMap<>();
        this.boundedContextsByName = new HashMap<>();
        this.operationNamingMode = operationNamingMode;
    }

    @Override
    public Set<BoundedContext> discoverBoundedContexts() {
        Set<BoundedContext> boundedContexts = new HashSet<>();
        for (File m2mDecompositionFile : findDecompositionFiles()) {
            boundedContexts.addAll(discoverBoundedContexts(m2mDecompositionFile));
        }
        return boundedContexts;
    }

    private Set<BoundedContext> discoverBoundedContexts(File m2mDecompositionFile) {
        Set<BoundedContext> boundedContexts = new HashSet<>();
        Decomposition m2mDecomposition = parseFile(m2mDecompositionFile);
        for (Cluster m2mCluster : m2mDecomposition.getClusters()) {
            BoundedContext boundedContext = createBoundedContext(m2mCluster);
            boundedContexts.add(boundedContext);
            boundedContextsByName.put(m2mCluster.getName(), boundedContext);
        }
        for (BoundedContext boundedContext : boundedContextsByName.values()) {
            discoverCoordinations(boundedContext, m2mDecomposition);
        }
        for (Entity m2mEntity : m2mDecomposition.getEntities()) {
            discoverAttributes(domainObjectsByName.get(m2mEntity.getName()), m2mEntity.getFields());
        }
        return boundedContexts;
    }

    private BoundedContext createBoundedContext(Cluster m2mCluster) {
        BoundedContext boundedContext = new BoundedContext(m2mCluster.getName());
        boundedContext.addAggregate(createAggregate(m2mCluster));
        boundedContext.setApplication(createApplication(m2mCluster));
        return boundedContext;
    }

    private Aggregate createAggregate(Cluster m2mCluster) {
        Aggregate aggregate = new Aggregate(m2mCluster.getName());
        aggregate.addDomainObjects(discoverDomainObjects(m2mCluster));
        return aggregate;
    }

    private Set<DomainObject> discoverDomainObjects(Cluster m2mCluster) {
        Set<DomainObject> domainObjects = new HashSet<>();
        for (Entity m2mEntity : m2mCluster.getElements()) {
            DomainObject domainObject = createDomainObject(m2mEntity);
            domainObjects.add(domainObject);
            domainObjectsByName.put(m2mEntity.getName(), domainObject);
        }
        return domainObjects;
    }

    private DomainObject createDomainObject(Entity m2mEntity) {
        return new DomainObject(DomainObjectType.ENTITY, m2mEntity.getName());
    }

    private Application createApplication(Cluster m2mCluster) {
        Application application = new Application(m2mCluster.getName() + "_Application");
        application.addService(createApplicationService(m2mCluster));
        return application;
    }

    private Service createApplicationService(Cluster m2mCluster) {
        Service service = new Service(m2mCluster.getName() + "_Service");
        service.setDiscoveryComment(getApplicationServiceDiscoveryComment());
        return service;
    }

    protected String getApplicationServiceDiscoveryComment() {
        switch (operationNamingMode) {
            case 1: return IGNORE_TYPES_OPERATION_DISCOVERY_COMMENT;
            case 2: return IGNORE_ORDER_OPERATION_DISCOVERY_COMMENT;
            default: return BASIC_OPERATION_DISCOVERY_COMMENT;
        }
    }

    private void discoverCoordinations(BoundedContext boundedContext, Decomposition m2mDecomposition) {
        Set<Functionality> m2mFunctionalities = m2mDecomposition.getFunctionalities(boundedContext.getName());
        for (Functionality m2mFunctionality : m2mFunctionalities) {
            //if (!m2mFunctionality.getSimpleName().equals("CreateCourseExecution"))
            //    continue;
            boundedContext.getApplication().addCoordination(createCoordination(m2mFunctionality));
        }
    }

    private Coordination createCoordination(Functionality m2mFunctionality) {
        String coordinationName = m2mFunctionality.getSimpleName();
        Coordination coordination = new Coordination(coordinationName + "_Coordination");
        coordination.setSaga(true);
        coordination.addCoordinationSteps(discoverCoordinationSteps(m2mFunctionality.getSteps()));
        return coordination;
    }

    protected List<CoordinationStep> discoverCoordinationSteps(List<FunctionalityStep> m2mFunctionalitySteps) {
        List<CoordinationStep> coordinationSteps = new ArrayList<>();
        for (FunctionalityStep m2mFunctionalityStep : m2mFunctionalitySteps) {
            if (!m2mFunctionalityStep.getAccesses().isEmpty()) {
                coordinationSteps.add(createCoordinationStep(m2mFunctionalityStep));
            }
        }
        return coordinationSteps;
    }

    protected CoordinationStep createCoordinationStep(FunctionalityStep m2mFunctionalityStep) {
        BoundedContext boundedContext = boundedContextsByName.get(m2mFunctionalityStep.getCluster());
        Service service = discoverApplicationService(boundedContext);
        Method operation = discoverCoordinationStepOperation(service, m2mFunctionalityStep.getAccesses());
        return new CoordinationStep(boundedContext, service, operation);
    }

    protected Service discoverApplicationService(BoundedContext boundedContext) {
        String serviceName = boundedContext.getName() + "_Service";
        return boundedContext.getApplication().getServices().stream()
                .filter(service -> service.getName().equals(serviceName))
                .findFirst()
                .orElse(createApplicationService(serviceName));
    }

    protected Service createApplicationService(String serviceName) {
        return new Service(serviceName);
    }

    protected Method discoverCoordinationStepOperation(Service service, List<EntityAccess> m2mEntityAccesses) {
        String operationName = createOperationName(m2mEntityAccesses);
        return service.getOperations().stream()
                .filter(operation -> operation.getName().equals(operationName))
                .findFirst()
                .orElse(createCoordinationStepOperation(service, operationName));
    }

    protected Method createCoordinationStepOperation(Service service, String operationName) {
        Method operation = new Method(operationName);
        service.addOperation(operation);
        return operation;
    }

    protected String createOperationName(List<EntityAccess> m2mEntityAccesses) {
        switch (operationNamingMode) {
            case 1: return new IgnoreAccessTypes(m2mEntityAccesses).createOperationName();
            case 2: return new IgnoreAccessOrder(m2mEntityAccesses).createOperationName();
            default: return new FullAccessSequence(m2mEntityAccesses).createOperationName();
        }
    }

    protected void discoverAttributes(DomainObject domainObject, List<Field> m2mFields) {
        if (domainObject == null) {
            // Structural data may collect other domain objects not considered in the decomposition (ex: Embeddable types).
            // References to these will be mapped as primitive types.
            return;
        }
        for (Field m2mField : m2mFields) {
            DataType m2mDataType = m2mField.getType();
            domainObject.addAttribute(new Attribute(discoverType(m2mDataType, domainObject), m2mField.getName()));
        }
    }

    protected Type discoverType(DataType m2mDataType, DomainObject parentDomainObject) {
        Type type;

        if (domainObjectsByName.containsKey(m2mDataType.getName())) {
            type = createDomainObjectType(domainObjectsByName.get(m2mDataType.getName()), parentDomainObject.getParent());
        } else if (m2mDataType.isParameterizedType()) {
            // Only considering 1 parameter (List, Set, Collection)
            type = discoverType(m2mDataType.getParameters().get(0), parentDomainObject);
        } else {
            type = new Type(m2mDataType.getName());
        }

        if (m2mDataType.isCollectionType()) {
            type.setCollectionType(m2mDataType.getName());
        }

        return type;
    }

    private Type createDomainObjectType(DomainObject referencedDomainObject, Aggregate containingAggregate) {
        return referencedDomainObject.getParent().equals(containingAggregate) ?
                new Type(referencedDomainObject) :
                new Type(getOrCreateReferenceDomainObject(
                        referencedDomainObject.getName(), containingAggregate, referencedDomainObject.getParent()));
    }

    private DomainObject getOrCreateReferenceDomainObject(String name, Aggregate containingAggregate, Aggregate referencedAggregate) {
        String referenceName = name + "_Reference";

        if (domainObjectsByName.containsKey(referenceName)) {
            return domainObjectsByName.get(referenceName);
        }

        DomainObject domainObject = new DomainObject(DomainObjectType.ENTITY, name + "_Reference");
        domainObject.setDiscoveryComment(
                "This entity was created to reference the '" + name +
                        "' entity of the '" + referencedAggregate.getName() + "' aggregate.");
        containingAggregate.addDomainObject(domainObject);
        domainObjectsByName.put(referenceName, domainObject);

        return domainObject;
    }

    protected Decomposition parseFile(File m2mFile) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(m2mFile, Decomposition.class);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The file '" + m2mFile + "' does not exist!", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("The file '" + m2mFile + "' could not be parsed!", e);
        }
    }

    private Collection<File> findDecompositionFiles() {
        return FileUtils.listFiles(sourcePath, new NameFileFilter("m2m_contract.json"), TrueFileFilter.INSTANCE);
    }
}
