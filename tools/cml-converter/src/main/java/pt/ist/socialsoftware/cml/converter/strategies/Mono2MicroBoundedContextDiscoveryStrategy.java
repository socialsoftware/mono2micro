package pt.ist.socialsoftware.cml.converter.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.contextmapper.discovery.model.*;
import org.contextmapper.discovery.strategies.boundedcontexts.AbstractBoundedContextDiscoveryStrategy;
import pt.ist.socialsoftware.cml.converter.domain.*;
import pt.ist.socialsoftware.cml.converter.strategies.naming.AnonymousStepsOperationNamingStrategy;
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
    private final Map<String, Map<String, Integer>> externalAccesses; // BC.name -> (DO.name -> numAccesses)
    private final Map<String, Map<String, Integer>> localAccesses; // BC.name -> (DO.name -> numAccesses)

    private final Map<String, BoundedContext> boundedContextsByName;
    private final Set<String> coordinationNames;
    private static int repeatedCoordinationNameCounter = 1;
    private final int operationNamingMode;

    public Mono2MicroBoundedContextDiscoveryStrategy(File sourcePath, int operationNamingMode) {
        this.sourcePath = sourcePath;
        this.domainObjectsByName = new HashMap<>();
        this.boundedContextsByName = new HashMap<>();
        this.coordinationNames = new HashSet<>();
        this.operationNamingMode = operationNamingMode;
        this.externalAccesses = new HashMap<>();
        this.localAccesses = new HashMap<>();
    }

    @Override
    public Set<BoundedContext> discoverBoundedContexts() {
        Set<BoundedContext> boundedContexts = new HashSet<>();
        for (File m2mDecompositionFile : findDecompositionFiles()) {
            boundedContexts.addAll(discoverBoundedContexts(m2mDecompositionFile));
        }
        return boundedContexts;
    }

    protected Set<BoundedContext> discoverBoundedContexts(File m2mDecompositionFile) {
        Set<BoundedContext> boundedContexts = new HashSet<>();
        Decomposition m2mDecomposition = parseFile(m2mDecompositionFile);
        for (Cluster m2mCluster : m2mDecomposition.getClusters()) {
            externalAccesses.put(m2mCluster.getName(), new HashMap<>());
            localAccesses.put(m2mCluster.getName(), new HashMap<>());
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
        finalizeAccessMetrics();
        return boundedContexts;
    }

    protected BoundedContext createBoundedContext(Cluster m2mCluster) {
        BoundedContext boundedContext = new BoundedContext(m2mCluster.getName());
        boundedContext.addAggregate(createAggregate(m2mCluster));
        boundedContext.setApplication(createApplication(m2mCluster));
        return boundedContext;
    }

    protected Aggregate createAggregate(Cluster m2mCluster) {
        Aggregate aggregate = new Aggregate(m2mCluster.getName());
        aggregate.addDomainObjects(discoverDomainObjects(m2mCluster));
        return aggregate;
    }

    protected Set<DomainObject> discoverDomainObjects(Cluster m2mCluster) {
        Set<DomainObject> domainObjects = new HashSet<>();
        for (Entity m2mEntity : m2mCluster.getElements()) {
            domainObjects.add(createDomainObject(m2mEntity));
            externalAccesses.get(m2mCluster.getName()).put(m2mEntity.getName(), 0);
            localAccesses.get(m2mCluster.getName()).put(m2mEntity.getName(), 0);
        }
        return domainObjects;
    }

    protected DomainObject createDomainObject(Entity m2mEntity) {
        DomainObject domainObject = new DomainObject(DomainObjectType.ENTITY, m2mEntity.getName());
        domainObjectsByName.put(m2mEntity.getName(), domainObject);
        return domainObject;
    }

    protected Application createApplication(Cluster m2mCluster) {
        Application application = new Application(m2mCluster.getName() + "_Application");
        application.addService(createApplicationService(m2mCluster));
        return application;
    }

    protected Service createApplicationService(Cluster m2mCluster) {
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

    protected void discoverCoordinations(BoundedContext boundedContext, Decomposition m2mDecomposition) {
        Set<Functionality> m2mFunctionalities = m2mDecomposition.getFunctionalities(boundedContext.getName());
        for (Functionality m2mFunctionality : m2mFunctionalities) {
            if (m2mFunctionality.getSteps().size() == 1) { // Local functionality
                boundedContext.getApplication().addService(createFunctionalityService(m2mFunctionality));
                updateAccessMetrics(m2mFunctionality.getOrchestrator(), m2mFunctionality.getSteps().get(0));
            } else {
                boundedContext.getApplication().addCoordination(createCoordination(m2mFunctionality));
            }
        }
    }

    protected Service createFunctionalityService(Functionality m2mFunctionality) {
        Service service = new Service(m2mFunctionality.getSimpleName() + "_Service");
        service.addOperation(new Method(createOperationName(m2mFunctionality, m2mFunctionality.getSteps().get(0).getAccesses()) + "_OP"));
        return service;
    }

    protected Coordination createCoordination(Functionality m2mFunctionality) {
        Coordination coordination = new Coordination(createCoordinationName(m2mFunctionality));
        coordination.setSaga(true);
        coordination.addCoordinationSteps(discoverCoordinationSteps(m2mFunctionality));
        return coordination;
    }

    protected String createCoordinationName(Functionality m2mFunctionality) {
        String coordinationName = m2mFunctionality.getSimpleName() + "_Coordination";
        if (coordinationNames.contains(coordinationName)) {
            coordinationName += repeatedCoordinationNameCounter++;
        }
        coordinationNames.add(coordinationName);
        return coordinationName;
    }

    protected List<CoordinationStep> discoverCoordinationSteps(Functionality m2mFunctionality) {
        List<CoordinationStep> coordinationSteps = new ArrayList<>();
        for (FunctionalityStep m2mFunctionalityStep : m2mFunctionality.getSteps()) {
            if (!m2mFunctionalityStep.getAccesses().isEmpty()) {
                coordinationSteps.add(createCoordinationStep(m2mFunctionality, m2mFunctionalityStep));
                updateAccessMetrics(m2mFunctionality.getOrchestrator(), m2mFunctionalityStep);
            }
        }
        return coordinationSteps;
    }

    protected CoordinationStep createCoordinationStep(Functionality m2mFunctionality, FunctionalityStep m2mFunctionalityStep) {
        BoundedContext boundedContext = boundedContextsByName.get(m2mFunctionalityStep.getCluster());
        Service service = discoverApplicationService(boundedContext);
        Method operation = discoverOperation(service, m2mFunctionality, m2mFunctionalityStep.getAccesses());
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

    protected Method discoverOperation(Service service, Functionality m2mFunctionality, List<EntityAccess> m2mEntityAccesses) {
        String operationName = createOperationName(m2mFunctionality, m2mEntityAccesses);
        return service.getOperations().stream()
                .filter(operation -> operation.getName().equals(operationName))
                .findFirst()
                .orElse(createOperation(service, operationName));
    }

    protected Method createOperation(Service service, String operationName) {
        Method operation = new Method(operationName);
        operation.setReturnType(new Type("void"));
        service.addOperation(operation);
        return operation;
    }

    protected String createOperationName(Functionality m2mFunctionality, List<EntityAccess> m2mEntityAccesses) {
        switch (operationNamingMode) {
            case 1: return new FullAccessSequence(m2mEntityAccesses).createOperationName();
            case 2: return new IgnoreAccessTypes(m2mEntityAccesses).createOperationName();
            case 3: return new IgnoreAccessOrder(m2mEntityAccesses).createOperationName();
            default: return new AnonymousStepsOperationNamingStrategy(m2mFunctionality).createOperationName();
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
            domainObject.addAttribute(createAttribute(m2mDataType, m2mField.getName(), domainObject));
        }
    }

    protected Attribute createAttribute(DataType m2mDataType, String attributeName, DomainObject containerDomainObject) {
        return new Attribute(discoverAttributeType(m2mDataType, containerDomainObject), attributeName);
    }

    protected Type discoverAttributeType(DataType m2mDataType, DomainObject containerDomainObject) {
        Type type;

        if (domainObjectsByName.containsKey(m2mDataType.getName())) {
            type = createAttributeType(domainObjectsByName.get(m2mDataType.getName()), containerDomainObject.getParent());
        } else if (m2mDataType.isParameterizedType()) {
            // Only considering 1 parameter (List, Set, Collection)
            type = discoverAttributeType(m2mDataType.getParameters().get(0), containerDomainObject);
        } else {
            type = new Type(m2mDataType.getName());
        }

        if (m2mDataType.isCollectionType()) {
            type.setCollectionType(m2mDataType.getName());
        }

        return type;
    }

    protected Type createAttributeType(DomainObject referencedDomainObject, Aggregate containingAggregate) {
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
                "\n\t\t * This entity was created to reference the '" + name +
                        "' entity of the '" + referencedAggregate.getName() + "' aggregate.");
        containingAggregate.addDomainObject(domainObject);
        domainObjectsByName.put(referenceName, domainObject);

        return domainObject;
    }

    protected void updateAccessMetrics(String orchestratorName, FunctionalityStep m2mFunctionalityStep) {
        for (EntityAccess m2mEntityAccess : m2mFunctionalityStep.getAccesses()) {
            Map<String, Integer> accessesPerDomainObject;
            if (!orchestratorName.equals(m2mFunctionalityStep.getCluster())) {
                accessesPerDomainObject = externalAccesses.get(m2mFunctionalityStep.getCluster());
            } else {
                accessesPerDomainObject = localAccesses.get(m2mFunctionalityStep.getCluster());
            }
            accessesPerDomainObject.replace(
                    m2mEntityAccess.getEntity(),
                    accessesPerDomainObject.get(m2mEntityAccess.getEntity()) + 1);
        }
    }

    protected void finalizeAccessMetrics() {
        for (BoundedContext boundedContext : boundedContextsByName.values()) {
            Map<String, Integer> boundedContextExternalAccesses = externalAccesses.get(boundedContext.getName());
            int totalExternalAccesses = boundedContextExternalAccesses.values().stream()
                            .mapToInt(Integer::intValue).sum();
            Map<String, Integer> boundedContextLocalAccesses = localAccesses.get(boundedContext.getName());
            int totalLocalAccesses = boundedContextLocalAccesses.values().stream()
                    .mapToInt(Integer::intValue).sum();

            boundedContextExternalAccesses.keySet()
                    .forEach(domainObjectName -> {
                        DomainObject domainObject = domainObjectsByName.get(domainObjectName);
                        int entityExternalAccesses = boundedContextExternalAccesses.get(domainObject.getName());
                        int entityLocalAccesses = boundedContextLocalAccesses.get(domainObject.getName());

                        double percentageOfExternalAccesses =
                                Math.round(10000 * (entityExternalAccesses / (double) totalExternalAccesses)) / 100.0;
                        double percentageOfLocalAccesses =
                                Math.round(10000 * (entityLocalAccesses / (double) totalLocalAccesses)) / 100.0;

                        domainObject.setDiscoveryComment("\n\t\t * " + "Metrics:" +
                                "\n\t\t * - Percentage of external accesses: " + percentageOfExternalAccesses +
                                "% (" + entityExternalAccesses + "/" + totalExternalAccesses + ")" +
                                "\n\t\t * - Percentage of local accesses: " + percentageOfLocalAccesses +
                                "% (" + entityLocalAccesses + "/" + totalLocalAccesses + ")");
                    });

        }
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
