package collectors.JPA;

import collectors.SpoonCollector;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import collectors.JPA.parser.MyHqlParser;
import collectors.JPA.parser.QueryAccess;
import collectors.JPA.parser.TableNamesFinderExt;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.SpoonClassNotFoundException;
import spoon.support.reflect.code.CtDoImpl;
import spoon.support.reflect.code.CtForEachImpl;
import spoon.support.reflect.code.CtForImpl;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLoopImpl;
import spoon.support.reflect.code.CtWhileImpl;
import util.property_scanner.PropertyScanner;
import util.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static collectors.JPA.JPAUtils.*;

public class SpringDataJPACollector extends SpoonCollector {

    static Map<String, CtType> entityClassNameMap; // map between EntityName -> ClassName
    static Map<String, Classes> tableClassesAccessedMap; // Classes related to a given table
    static ArrayList<Query> namedQueries; // list of tables accessed in a given query
    private List<Repository> repositories;
    private Repository eventualNewRepository;
    private List<PropertyScanner> propertyScanners;

    public SpringDataJPACollector(int launcherChoice, String repoName, String projectPath) throws IOException {
        super(launcherChoice, repoName, projectPath);

        switch (launcherChoice) {
            case Constants.LAUNCHER:
            case Constants.JAR_LAUNCHER:
            case Constants.MAVEN_LAUNCHER:
                launcher.getEnvironment().setSourceClasspath(new String[]{
                        "./lib/spring-context-5.2.3.RELEASE.jar",
                        "./lib/spring-data-commons-core-1.4.1.RELEASE.jar",
                        "./lib/spring-data-jpa-2.2.5.RELEASE.jar"
                });
                break;
            default:
                System.exit(1);
                break;
        }

        // instantiate property scanners
        String[] pScanners = new String[]{
            "util.property_scanner.ReturnPropertyScanner",
            "util.property_scanner.ContinuePropertyScanner",
            "util.property_scanner.BreakPropertyScanner",
            "util.property_scanner.ZeroComparisonPropertyScanner"
        };
        
        propertyScanners = new ArrayList<PropertyScanner>();
        
        PropertyScanner scannerBuffer;
        for (String scanner : pScanners) {
            try {
                scannerBuffer = (PropertyScanner)(Class.forName(scanner).getDeclaredConstructor().newInstance());
                propertyScanners.add(scannerBuffer);
                
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Invalid Scanner: " + scanner);
                System.exit(-1);
            }

        }

        System.out.println("Generating AST...");
        launcher.buildModel();

        entityClassNameMap = new HashMap<>();
        tableClassesAccessedMap = new HashMap<>();
        namedQueries = new ArrayList<>();
        repositories = new ArrayList<>();
    }

    @Override
    public void collectControllersAndEntities() {
        for(CtType<?> clazz : factory.Class().getAll()) {
            List<CtAnnotation<? extends Annotation>> clazzAnnotations = clazz.getAnnotations();

            if (existsAnnotation(clazzAnnotations, "Controller") ||
                existsAnnotation(clazzAnnotations, "RestController") ||
                existsAnnotation(clazzAnnotations, "RequestMapping") ||
                existsAnnotation(clazzAnnotations, "GetMapping") ||
                existsAnnotation(clazzAnnotations, "PostMapping") ||
                existsAnnotation(clazzAnnotations, "PatchMapping") ||
                existsAnnotation(clazzAnnotations, "PutMapping") ||
                existsAnnotation(clazzAnnotations, "DeleteMapping") ||
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("DispatchAction"))) {

                if (clazz instanceof CtClass) {
                    controllers.add((CtClass) clazz);
                }
            }
            else {
                if (isRepository(clazz.getReference())) {
                    repositories.add(eventualNewRepository);
                    eventualNewRepository = null;
                }
                if (clazz.isInterface() || clazz.isAbstract()) {
                    abstractClassesAndInterfacesImplementorsMap.put(clazz.getSimpleName(), new ArrayList<>());
                }
                if (existsAnnotation(clazzAnnotations, "Entity") ||
                        existsAnnotation(clazzAnnotations, "MappedSuperclass")) {
                    allDomainEntities.add(clazz.getSimpleName());
                }
            }

            allEntities.add(clazz.getSimpleName());
        }

        for (String e : allDomainEntities)
            entitiesMap.put(e, entitiesMap.size() + 1);

        // 2nd iteration, to retrieve collectors.JPA related information and interface explicit implementations
        for(CtType<?> clazz : factory.Class().getAll()) {
            Set<CtTypeReference<?>> superInterfaces = clazz.getSuperInterfaces();
            for (CtTypeReference ctTypeReference : superInterfaces) {
                if (abstractClassesAndInterfacesImplementorsMap.containsKey(ctTypeReference.getSimpleName())) {
                    abstractClassesAndInterfacesImplementorsMap.get(ctTypeReference.getSimpleName()).add(clazz);
                }
            }

            CtTypeReference<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                List<CtType> ctTypes = abstractClassesAndInterfacesImplementorsMap.get(superclass.getSimpleName());
                if (ctTypes != null) // key exists
                    ctTypes.add(clazz);
            }

            List<CtAnnotation<? extends Annotation>> clazzAnnotations = clazz.getAnnotations();
            CtAnnotation entityAnnotation = getAnnotation(clazzAnnotations, "Entity");
            if (entityAnnotation != null) { // Domain Class @Entity
                parseEntity(clazz, entityAnnotation, clazzAnnotations);
            }
        }

        super.repositoryCount = repositories.size();
    }

    /*
    *  isRepository: true if class Package = org.springframework.data.repository or if it extends (in)directly from it */
    private boolean isRepository(CtTypeReference<?> clazzReference) {
        try {
            if (isRepositoryPackageClass(clazzReference.getPackage().toString()))
                return true;


            // first call -> lets keep info about this class which may be a repository
            // namely the eventual repository type
            if (eventualNewRepository == null) {
                for (Object o : clazzReference.getSuperInterfaces().toArray()) {
                    CtTypeReference ctInterface = (CtTypeReference) o;
                    if (ctInterface.getActualTypeArguments().size() > 0) {
                        CtTypeReference<?> ctTypeReference = ctInterface.getActualTypeArguments().get(0);
                        String typeArgumentName = ctTypeReference.getSimpleName();

                        if (!allEntities.contains(typeArgumentName)) {
                            // repository of an entity which is not declared
                            // should be a parameterized type
                            // Example:
                            // public interface ExperimentRepositoryTemplate<E extends ExperimentTemplate> extends JpaRepository<E, Long>
                            if (ctTypeReference instanceof CtTypeParameterReference) {
                                // bounding type returns the superClass of the TypeParameter or Object
                                CtTypeReference<?> boundingType = ((CtTypeParameterReference) ctTypeReference).getBoundingType();
                                if (allEntities.contains(boundingType.getSimpleName()))
                                    typeArgumentName = boundingType.getSimpleName();
                            }
                        }
                        eventualNewRepository = new Repository(
                                clazzReference.getSimpleName(),
                                typeArgumentName
                        );

                        if (isRepository(ctInterface))
                            return true;
                        else {
                            eventualNewRepository = null;
                        }
                    }
                }
            }

            if (eventualNewRepository == null) // root class is not a repository class definitely
                return false;
            else { // root class looks like a repository class; iterate over superinterfaces to confirm
                for (Object o : clazzReference.getSuperInterfaces().toArray()) {
                    CtTypeReference ctInterface = (CtTypeReference) o;

                    if (isRepository(ctInterface))
                        return true;
                }
            }

            return false;
        } catch (Exception e) {
            eventualNewRepository = null;
            return false;
        }
    }

    private boolean isRepositoryPackageClass(String packageName) {
        return packageName.contains("org.springframework.data") && packageName.endsWith(".repository");
    }

    private void parseEntity(CtType<?> clazz, CtAnnotation atEntityAnnotation, List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        // ------------------- @Entity -----------------------
        String entityName = parseAtEntity(clazz, atEntityAnnotation);

        // ------------------- @Table -----------------------
        String tableName = parseAtTableAndAtInheritance(clazz);

        // ------------------- @NamedQueries -----------------------
        parseAtNamedQueries(clazzAnnotations);

        // ------------------- @NamedQuery -----------------------
        parseAtNamedQuery(clazzAnnotations);

        // ------------------- @NamedNativeQueries -----------------------
        parseAtNamedNativeQueries(clazzAnnotations);

        // ------------------- @NamedNativeQuery -----------------------
        parseAtNamedNativeQuery(clazzAnnotations);

        Object[] clazzMethodsArray = clazz.getMethods().toArray();
        ArrayList<CtMethod> gettersList = new ArrayList<>();
        for (Object ctMObject : clazzMethodsArray) {
            CtMethod ctM = (CtMethod) ctMObject;
            if (ctM.getSimpleName().startsWith("get"))
                gettersList.add(ctM);
        }

        // ------------------- Check Fields -----------------------
        for (CtField field : clazz.getFields()) {

            List<CtAnnotation<? extends Annotation>> fieldAnnotationsUnmodifiable = field.getAnnotations();
            List<CtAnnotation<? extends Annotation>> fieldAnnotations = new ArrayList<>(fieldAnnotationsUnmodifiable);

            // collectors.JPA allows to either put the annotations on a field or its getter
            Optional<CtMethod> first = gettersList.stream()
                    .filter(m -> m.getSimpleName().equalsIgnoreCase("get" + field.getSimpleName()))
                    .findFirst();
            first.ifPresent(method -> fieldAnnotations.addAll(method.getAnnotations()));
            ;
            // ------------------- @ElementCollection -----------------------
            parseAtElementCollection(clazz, field, fieldAnnotations, entityName);

            // ------------------- @OneToOne -----------------------
            parseAtOneToOne(clazz, field, fieldAnnotations, tableName);

            // ------------------- @OneToMany -----------------------
            parseAtOneToMany(clazz, field, fieldAnnotations, tableName, allDomainEntities);

            // ------------------- @ManyToMany -----------------------
            parseAtManyToMany(clazz, field, fieldAnnotations, tableName);

            // ------------------- @ManyToOne -----------------------
            parseAtManyToOne(clazz, field, fieldAnnotations, tableName);
        }
    }

    @Override
    public void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack) {
        methodStack.push(callerMethod.getPosition());

        callerMethod.accept(new CtScanner() {
            private <T> void visitCtAbstractInvocation(CtAbstractInvocation calleeLocation) {
                try {
                    if (calleeLocation == null)
                        return;

                    try {
                        if (calleeLocation instanceof CtInvocation) {
                            CtExpression target = ((CtInvocation) calleeLocation).getTarget();
                            if (target != null) {
                                CtTypeReference targetTypeReference = target.getType();
                                if (targetTypeReference != null) {
                                    String targetTypeName = targetTypeReference.getSimpleName();
                                    if (isProjectDeclaredRepository(targetTypeName)) {
                                        analyzeCallAndRegisterAccess(
                                                calleeLocation.getExecutable(),
                                                targetTypeReference.getTypeDeclaration()
                                        );
                                        return;
                                    }
                                }

                                CtExecutable executableDeclaration = calleeLocation.getExecutable().getExecutableDeclaration();
                                if (executableDeclaration != null) {
                                    CtMethod eDM = (CtMethod) executableDeclaration;
                                    if (eDM.isAbstract()) {
                                        // call to an abstract method (or interface method)
                                        // 1. Call to a regular interface method
                                        // 2. Call to an interface implemented by repositories (declared methods without bodies)
                                        // 3. Call to an interface implemented by repositories but JPA method (not declared in the implementor class)
                                        // The getExplicitImplementationsOfAbstractMethodJPA will check and register calls
                                        // to other abstract methods within repository classes
                                        List<CtMethod> explicitImplementationsOfAbstractMethod = getExplicitImplementationsOfAbstractMethodJPA(eDM);
                                        for (CtMethod ctMethod : explicitImplementationsOfAbstractMethod) {
                                            if (!methodStack.contains(ctMethod.getPosition())) {
                                                CtInvocation calleeLocationClone = ((CtInvocation) calleeLocation).clone();
                                                updateCalleeLocationWithExplicitImplementation(calleeLocationClone, ctMethod);
                                                methodCallDFS(ctMethod, calleeLocationClone, methodStack);
                                            }
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (UnkownMethodException e) {
                        return;
                    } catch (Exception e) {
                        if (e.getCause() instanceof SpoonClassNotFoundException)
                            return;
                        e.printStackTrace();
                    }

                    if (calleeLocation.getExecutable().getDeclaringType() == null)
                        return;
                    else if (isCallToCollections(calleeLocation)) {
                        if (calleeLocation instanceof CtInvocation) {
                            inspectTargetFromCall((CtInvocation) calleeLocation);
                            if (collectionEntityAccess && !collectionTransientField) {
                                registerDomainAccess(collectionDeclaringTypeName, collectionFieldAccessedType, (CtInvocation) calleeLocation);
                            }
                        }
                    }
                    else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                        CtExecutable executableDeclaration = calleeLocation.getExecutable().getExecutableDeclaration();
                        if (executableDeclaration != null && !methodStack.contains(executableDeclaration.getPosition())) {
                            methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation, methodStack);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (StackOverflowError soe) {
                    System.err.println("StackOverflowError on call " + calleeLocation.toString());
                }
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> m) {
                super.visitCtMethod(m);
                /* When inspecting calls, if we see an abstract method we redirect to the implementor method
                * However, it is possible that the implementor is abstract as well. This block of code allows to redirect
                * until we find a non-abstract implementor */
                if (m.isAbstract()) {
                    try {
                        List<CtMethod> explicitImplementationsOfAbstractMethod = getExplicitImplementationsOfAbstractMethodJPA(m);
                        for (CtMethod ctMethod : explicitImplementationsOfAbstractMethod) {
                            if (!methodStack.contains(ctMethod.getPosition())) {
                                CtInvocation calleeLocationClone = ((CtInvocation) prevCalleeLocation).clone();
                                updateCalleeLocationWithExplicitImplementation(calleeLocationClone, ctMethod);
                                methodCallDFS(ctMethod, calleeLocationClone, methodStack);
                            }
                        }
                    } catch (UnkownMethodException ignored) {}
                }
            }

            private <T> void visitCtFieldAccess(CtFieldAccess<T> fieldAccess, String mode) {
                try {
                    // class that declares the field
                    CtTypeReference<?> declaringType = fieldAccess.getVariable().getDeclaringType();
                    String declaringTypeName = null;

                    /* MappedSuperclasses are similar to abstract classes. They are supposed to be extended
                    * and not instantiated, they won't even have its own table. When accessing a field from
                    * a class like this one, we will consider its target as the accessor and not the class
                    * itself.
                    * To reach the setter of an abstract class, it had to be called previously. That's why
                    * im saving the prevCalleeLocation */
                    if (existsAnnotation(declaringType.getTypeDeclaration().getAnnotations(),
                            "MappedSuperclass")) {
                        try {
                            CtTypeReference type = ((CtInvocation) prevCalleeLocation).getTarget().getType();
                            declaringTypeName = type.getSimpleName();
                        } catch (Exception e) {}
                    }

                    declaringTypeName = declaringTypeName == null ? declaringType.getSimpleName() : declaringTypeName;

                    if (allDomainEntities.contains(declaringTypeName)) {

                        addEntitiesSequenceAccess(declaringTypeName, mode);

                        // field read
                        CtField<T> fieldDeclaration = fieldAccess.getVariable().getFieldDeclaration();
                        if (isTransient(fieldDeclaration)) {
                            return;
                        }
                        else {
                            CtTypeReference<T> fieldType = fieldDeclaration.getType();
                            List<CtTypeReference<?>> actualTypeArguments = fieldType.getActualTypeArguments();
                            if (actualTypeArguments.size() > 0) {
                                for (CtTypeReference ctTypeReference : actualTypeArguments) {
                                    if (allDomainEntities.contains(ctTypeReference.getSimpleName())) {
                                        addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), mode);
                                    }
                                }
                            }
                            else {
                                if (allDomainEntities.contains(fieldType.getSimpleName())) {
                                    addEntitiesSequenceAccess(fieldType.getSimpleName(), mode);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                super.visitCtFieldRead(fieldRead);
                visitCtFieldAccess(fieldRead, "R");
            }

            @Override
            public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
                super.visitCtFieldWrite(fieldWrite);
                visitCtFieldAccess(fieldWrite, "W");
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                visitCtAbstractInvocation(invocation);
            }

            @Override
            public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
                super.visitCtConstructorCall(ctConstructorCall);
                visitCtAbstractInvocation(ctConstructorCall);
                registerConstructorCallAccess(ctConstructorCall);
            }

            @Override
            public void scan(CtRole role, CtElement element) {
                CtElement elParent = null;
                if(element != null) {
                    try {
                        elParent = element.getParent();
                    } catch (ParentNotInitializedException e) {
                        elParent = null;
                    }
                }

                List<CtRole> loopRoles = Arrays.asList(
                        CtRole.EXPRESSION,
                        CtRole.BODY,
                        CtRole.FOR_INIT,
                        CtRole.FOR_UPDATE
                    );
                List<CtRole> ifRoles = Arrays.asList(
                        CtRole.CONDITION,
                        CtRole.THEN,
                        CtRole.ELSE
                    );

                boolean roleOpened = false;
                if( (elParent instanceof CtIfImpl && ifRoles.contains(role)) ||
                    (elParent instanceof CtLoopImpl && loopRoles.contains(role))
                    ) {
                    openNewContext(role.toString());
                    roleOpened = true;

                }
                
                if(element instanceof CtIfImpl) { // IF
                    openNewContext("if");
                    super.scan(element);
                    closeCurrentContext();

                } else if(element instanceof CtLoopImpl) { // LOOP
                    String loopType = "loop";

                    if(element instanceof CtDoImpl)
                        loopType = "do_loop";
                    else if(element instanceof CtWhileImpl)
                        loopType = "while_loop";
                    else if(element instanceof CtForImpl)
                        loopType = "for_loop";
                    else if(element instanceof CtForEachImpl)
                        loopType = "foreach_loop";

                    openNewContext(loopType);
                    super.scan(element);
                    closeCurrentContext();
                
                } else if(element instanceof CtInvocationImpl) {
                    openNewContext("call");
                    super.scan(element);
                    closeCurrentContext();

                } else {
                    super.scan(element);
                }

                // read heuristic properties
                //new ReturnPropertyScanner().process(SpringDataJPACollector.this, element);
                for (PropertyScanner scanner : propertyScanners) {
                    scanner.process(SpringDataJPACollector.this, element);
                }

                if(roleOpened) closeCurrentContext();

            }
        });

        methodStack.pop();
    }

    /**
     * Assume that all created entities (call to 'new') will be saved later on.
     * Storing the access also at constructor call time catches cases of entities which are subclasses,
     * but whose subtype reference is lost or not considered later on,
     * leaving entities completely out of the traces in the worst case.
     *
     * @param ctConstructorCall A constructor call (new Object())
     */
    private void registerConstructorCallAccess(CtConstructorCall<?> ctConstructorCall) {
        String createdTypeName = ctConstructorCall.getExecutable().getType().getSimpleName();
        if (allDomainEntities.contains(createdTypeName)) {
            addEntitiesSequenceAccess(createdTypeName, "W");
        } else {
            // Might be a collection (List<Entity>, Map<String, Entity>, ...)
            for (CtTypeReference<?> typeArguments : ctConstructorCall.getType().getActualTypeArguments()) {
                if (allDomainEntities.contains(typeArguments.getSimpleName())) {
                    addEntitiesSequenceAccess(typeArguments.getSimpleName(), "W");
                    break;
                }
            }
        }
    }

    private void analyzeCallAndRegisterAccess(CtExecutableReference executable, CtType repositoryCtType) throws UnkownMethodException {
        // Entity Access Call
        List<CtMethod> methodsByName = repositoryCtType.getMethodsByName(executable.getSimpleName());
        boolean declared = false;
        for (CtMethod ctM : methodsByName) {
            if (isEqualMethodSignature(executable, ctM.getReference())) {
                // method declared in repository class
                declared = true;
                registerRepositoryAccess(repositoryCtType.getSimpleName(), ctM.getReference());
                break;
            }
        }
        if (!declared) {
            registerSpringDataRepositoryAccess(repositoryCtType.getSimpleName(), executable.getSimpleName());
        }
        return;
    }

    private void updateCalleeLocationTargetWithType(CtInvocation calleeLocation, CtType ctType) {
        CtConstructorCall call = factory.Core().createConstructorCall();
        call.setType(factory.Core().createTypeReference().setSimpleName(ctType.getSimpleName()));
        (calleeLocation).setTarget(call);
    }

    private List<CtMethod> getExplicitImplementationsOfAbstractMethodJPA(CtMethod abstractMethod) throws UnkownMethodException {
        List<CtMethod> explicitMethods = new ArrayList<>();
        CtType<?> declaringType = abstractMethod.getDeclaringType();
        List<CtType> implementors = abstractClassesAndInterfacesImplementorsMap.get(declaringType.getSimpleName());
        if (implementors != null) {
            for (CtType ctImplementorType : implementors) {
                if (isProjectDeclaredRepository(ctImplementorType.getSimpleName())) {
                    analyzeCallAndRegisterAccess(abstractMethod.getReference(), ctImplementorType);
                }
                else {
                    List<CtMethod> methodsByName = ctImplementorType.getMethodsByName(abstractMethod.getSimpleName());
                    for (CtMethod ctM : methodsByName) {
                        if (isEqualMethodSignature(abstractMethod.getReference(), ctM.getReference())) {
                            explicitMethods.add(ctM);
                        }
                    }
                }
            }
        }

        return explicitMethods;
    }

    private void updateCalleeLocationWithExplicitImplementation(CtInvocation calleeLocation, CtMethod explicitMethod) {
        // calleeLocation will be changed by reference
        // redirect calleeLocation to the explicit method found
        calleeLocation.setExecutable(explicitMethod.getReference());
        // replace the target of the call by the explicit target
        updateCalleeLocationTargetWithType(calleeLocation, explicitMethod.getDeclaringType());
    }

    private <T> boolean isTransient(CtField<T> fieldDeclaration) {
        List<CtAnnotation<? extends Annotation>> annotations = fieldDeclaration.getAnnotations();
        return existsAnnotation(annotations, "Transient") ||
                hasModifier(fieldDeclaration, "TRANSIENT") ||
                hasModifier(fieldDeclaration, "STATIC") ||
                hasModifier(fieldDeclaration, "FINAL");
    }

    private <T> boolean hasModifier(CtField<T> fieldDeclaration, String modifierName) {
        Set<ModifierKind> modifiers = fieldDeclaration.getModifiers();
        for (ModifierKind mk : modifiers) {
            if (mk.name().equals(modifierName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * When we have a call to a collection method (add/get/etc), the target of that call
     * must be either a Collection variable or a Collection field.
     * We are visiting the target until we reach the field/variable that
     * is being accessed and that may be "disguised" inside an invocation call.
     * e.g. Class.getListField().add(value)
     * */
    private void inspectTargetFromCall(CtInvocation calleeLocation) {
        collectionEntityAccess = false;
        collectionTransientField = false;
        collectionFieldAccessedType = null;
        collectionDeclaringTypeName = null;
        CtExpression targetExpression = calleeLocation.getTarget();
        visitTarget(targetExpression);
    }

    private boolean collectionEntityAccess = false;
    private boolean collectionTransientField = false;
    private CtTypeReference collectionFieldAccessedType = null;
    private String collectionDeclaringTypeName = null;

    private void visitTarget(CtTypedElement targetExpression) {
        if (targetExpression == null) {
            collectionEntityAccess = false;
            collectionFieldAccessedType = null;
            collectionDeclaringTypeName = null;
            collectionTransientField = false;
            return;
        }
        targetExpression.accept(new CtScanner() {
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                visitTarget(invocation.getExecutable().getExecutableDeclaration());
            }

            @Override
            public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                super.visitCtFieldRead(fieldRead);
                try {
                    CtField<T> fieldDeclaration = fieldRead.getVariable().getFieldDeclaration();
                    String declaringTypeName = fieldDeclaration.getDeclaringType().getSimpleName();

                    if (isTransient(fieldDeclaration)) {
                        collectionTransientField = true;
                    }
                    else {
                        collectionTransientField = false;
                    }

                    if (allDomainEntities.contains(declaringTypeName)) {
                        collectionEntityAccess = true;
                        collectionFieldAccessedType = fieldDeclaration.getType();
                        collectionDeclaringTypeName = declaringTypeName;
                        return;
                    }
                } catch (Exception e) {}

                collectionEntityAccess = false;
                collectionFieldAccessedType = null;
                collectionDeclaringTypeName = null;
            }

            @Override
            public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                super.visitCtVariableRead(variableRead);
                try {
                    String declaringClassName = variableRead.getVariable().getDeclaration().getParent(CtClass.class).getSimpleName();
                    if (allDomainEntities.contains(declaringClassName)) {
                        collectionEntityAccess = true;
                        collectionFieldAccessedType = variableRead.getVariable().getType();
                        collectionDeclaringTypeName = declaringClassName;
                        collectionTransientField = false;
                        return;
                    }
                } catch (Exception e) {}

                collectionEntityAccess = false;
                collectionFieldAccessedType = null;
                collectionDeclaringTypeName = null;
                collectionTransientField = false;
            }
        });
    }

    /*
    * Accesses made to the domain through collection functions
    * */
    private void registerDomainAccess(String declaringTypeName, CtTypeReference fieldAccessedType, CtInvocation calleeLocation) {
        CtExecutableReference callee = calleeLocation.getExecutable();
        String methodName = callee.getSimpleName();
        String mode = "";
        CtTypeReference returnType = null;
        List<String> argTypeNames = new ArrayList<>();
        if (methodName.startsWith("get") ||
                methodName.startsWith("contains") ||
                methodName.startsWith("equals") ||
                methodName.startsWith("isEmpty") ||
                methodName.startsWith("size") ||
                methodName.startsWith("toArray") ||
                methodName.contains("index") ||
                methodName.contains("iterator") ||
                methodName.contains("sort")) {
            mode = "R";
            returnType = fieldAccessedType;
        }
        else if (methodName.startsWith("set") ||
                methodName.startsWith("add") ||
                methodName.startsWith("remove") ||
                methodName.startsWith("clear") ||
                methodName.startsWith("replace") ||
                methodName.startsWith("put")) {
            mode = "W";


            // we visited the target before to find the field so lets use the types of field type
            for (CtTypeReference ctTypeReference : fieldAccessedType.getActualTypeArguments()) {
                if (allDomainEntities.contains(ctTypeReference.getSimpleName())) {
                    argTypeNames.add(ctTypeReference.getSimpleName());
                }
            }
        }

        if (mode.isEmpty()) {
            System.err.println("Couldn't find mode of callee: " + calleeLocation.getShortRepresentation());
            return;
        }

        // class access
        addEntitiesSequenceAccess(declaringTypeName, mode);
        // field access
        if (mode.equals("R")) {
            List<CtTypeReference<?>> actualTypeArguments = returnType.getActualTypeArguments();
            if (actualTypeArguments.size() > 0) {
                for (CtTypeReference ctTypeReference : actualTypeArguments) {
                    if (allDomainEntities.contains(ctTypeReference.getSimpleName())) {
                        addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), mode);
                    }
                }
            }
            else {
                if (allDomainEntities.contains(returnType.getSimpleName())) {
                    addEntitiesSequenceAccess(returnType.getSimpleName(), mode);
                }
            }
        }
        else if (mode.equals("W")) {
            for (String argTypeName : argTypeNames) {
                if (allDomainEntities.contains(argTypeName)) {
                    addEntitiesSequenceAccess(argTypeName, mode);
                }
            }
        }
    }

    private boolean isCallToCollections(CtAbstractInvocation calleeLocation) {
        // TODO ArrayList<T> arrayList = new ArrayList<>(); won't have superinterfaces
        CtTypeReference declaringType = calleeLocation.getExecutable().getDeclaringType();
        Set superInterfaces = declaringType.getSuperInterfaces();
        for (Object interfaceO : superInterfaces) {
            CtTypeReference interfaceTypeReference = (CtTypeReference) interfaceO;
            if (interfaceTypeReference.getSimpleName().contains("Collection") ||
                    interfaceTypeReference.getSimpleName().contains("Map"))
                return true;
        }
        return false;
    }

    private void registerRepositoryAccess(String repositorySimpleName, CtExecutableReference methodDeclaration) {
        List<CtAnnotation<? extends Annotation>> methodAnnotations = methodDeclaration.getExecutableDeclaration().getAnnotations();
        CtAnnotation<? extends Annotation> queryAnnotation = getAnnotation(methodAnnotations, "Query");
        if (queryAnnotation != null) {
            CtAnnotation<? extends Annotation> query =
                    getAnnotation(methodDeclaration.getExecutableDeclaration().getAnnotations(), "Query");
            String value = getValueAsString(query.getValue("value"));
            Boolean nativeQuery = (Boolean) ((CtLiteral) query.getValue("nativeQuery")).getValue();
            String namedQueryName = getValueAsString(query.getValue("name"));

            if (!namedQueryName.equals("")) {
                if (!value.equals("")) {
                    System.err.println("Error on method: " + methodDeclaration.toString());
                    System.err.println("@Query with 'name' and 'value' definitions. Is that even possible?");
                }
                else {
                    Query q = getNamedQuery(namedQueryName);
                    if (q == null) {
                        System.err.println("Error on method: " + methodDeclaration.toString());
                        System.err.println("Couldn't find NamedQuery " + namedQueryName);
                    }
                    else {
                        if (q.isNative())
                            parseNativeQuery(q.getValue());
                        else
                            parseHqlQuery(q.getValue(), repositorySimpleName);
                    }
                }
            }

            if (nativeQuery) {
                parseNativeQuery(value);
            }
            else { // HQL query
                parseHqlQuery(value, repositorySimpleName);
            }
        }
        else { // No @Query annotation present
            Repository r = getRepositoryFromClassName(methodDeclaration.getDeclaringType().getSimpleName());
            String typeClassName = r.getTypeClassName(); // will never be null

            // Spring Data first tries to resolve calls to repository methods to a named query, starting with
            // the simple name of the configured domain class, followed by the method name separated by a dot.
            Query q = getNamedQuery(typeClassName + "." + methodDeclaration.getSimpleName());

            if (q == null) {
                registerSpringDataRepositoryAccess(methodDeclaration.getDeclaringType().getSimpleName(), methodDeclaration.getSimpleName());
            }
            else {
                if (q.isNative())
                    parseNativeQuery(q.getValue());
                else
                    parseHqlQuery(q.getValue(), repositorySimpleName);
            }
        }
    }

    private void parseNativeQuery(String sql) {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            TableNamesFinderExt tablesNamesFinder = new TableNamesFinderExt();
            tablesNamesFinder.getTableList(stmt); // run visitor, populate QueryAccesses
            ArrayList<QueryAccess> accesses = tablesNamesFinder.getAccesses();
            for (QueryAccess qa : accesses) {
                String tableName = qa.getName();
                Classes classes = tableClassesAccessedMap.get(parseTableName(tableName));
                if (classes == null) {
                    System.err.println("Exception on query: " + sql);
                    System.err.println("Table not found: " + tableName);
                    continue;

                }
                for (String typeName : classes.getListOfClasses()) {
                    String mode = qa.getMode();
                    if (mode == null)
                        mode = "R";
                    addEntitiesSequenceAccess(typeName, mode
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Exception on query: \"" + sql + "\"");
            System.err.println(e.getCause().getMessage());
        }
    }

    private String parseTableName(String tableName) {
        return tableName.toUpperCase().replace("`", "");
    }

    private void parseHqlQuery(String hql, String repositorySimpleName) {
        try {
            Repository repositoryFromClassName = getRepositoryFromClassName(repositorySimpleName);
            Set<QueryAccess> accesses = new MyHqlParser(hql, repositoryFromClassName.getTypeClassName()).parse();
            for (QueryAccess a : accesses) {
                String entityName = a.getName();
                String[] split = entityName.split("\\.");

                CtType clazz = entityClassNameMap.get(split[0]);
                if (clazz == null) {
//                    System.err.println("HQL Parser: Couldn't map class " + split[0]);
                }
                else {
                    addEntitiesSequenceAccess(clazz.getSimpleName(), a.getMode());
                    for (int i = 1; i < split.length; i++) {
                        String fieldName = split[i];
                        CtField field = getFieldRecursively(clazz, fieldName);
                        if (field != null) {
                            CtTypeReference fieldTypeReference = field.getType();
                            List<CtTypeReference<?>> actualTypeArguments = fieldTypeReference.getActualTypeArguments();

                            // Set<Class> List<Class>
                            if (!allEntities.contains(field.getType().getSimpleName()) && actualTypeArguments.size() > 0) {
                                for (CtTypeReference ctTypeReference : actualTypeArguments) {
                                    CtTypeReference<?> processedType;

                                    // List<T extends Entity>
                                    if (ctTypeReference instanceof CtTypeParameterReference)
                                        processedType = ((CtTypeParameterReference) ctTypeReference).getBoundingType();
                                    else
                                        processedType = ctTypeReference;

                                    if (allDomainEntities.contains(processedType.getSimpleName())) {
                                        addEntitiesSequenceAccess(processedType.getSimpleName(), a.getMode());
                                        clazz = processedType.getTypeDeclaration(); // in order to retrieve next fields
                                    }
                                }
                            }

                            else {
                                if (fieldTypeReference instanceof CtTypeParameterReference) {
                                    CtTypeReference<?> boundingType = ((CtTypeParameterReference) fieldTypeReference).getBoundingType();
                                    if (allDomainEntities.contains(boundingType.getSimpleName())) {
                                        addEntitiesSequenceAccess(boundingType.getSimpleName(), a.getMode());
                                        clazz = boundingType.getTypeDeclaration();
                                    }
                                }

                                else if (allDomainEntities.contains(fieldTypeReference.getSimpleName())) {
                                    addEntitiesSequenceAccess(fieldTypeReference.getSimpleName(), a.getMode());
                                    clazz = fieldTypeReference.getTypeDeclaration(); // in order to retrieve next fields
                                }
                            }
                        }
                        else {
                            System.err.println("Couldn't get field '" + fieldName + "' in " + Arrays.toString(split));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("QueryException on query: \"" + hql + "\"");
            System.err.println(e.getCause().getMessage());
        }
    }

    private CtField getFieldRecursively(CtType clazz, String fieldName) {
        if (clazz == null)
            return null;

        CtField field = clazz.getField(fieldName);
        if (field != null)
            return field;
        else {
            CtTypeReference<?> superClassReference = clazz.getSuperclass();
            if (superClassReference != null)
                return getFieldRecursively(superClassReference.getTypeDeclaration(), fieldName);
            else
                return null;
        }
    }

    private Query getNamedQuery(String namedQueryName) {
        for (Query q : namedQueries) {
            if (q.getName().equals(namedQueryName)) {
                return q;
            }
        }
        return null;
    }

    private Repository getRepositoryFromClassName(String simpleName) {
        for (Repository r : repositories) {
            if (r.getRepositoryClassName().equals(simpleName))
                return r;
        }
        return null;
    }

    private void registerSpringDataRepositoryAccess(String repositoryClassName, String methodName) {
        String mode;
        // Read Access
        if (methodName.startsWith("find") ||
                methodName.startsWith("get") ||
                methodName.startsWith("exists") ||
                methodName.startsWith("read") ||
                methodName.startsWith("count")) {
            mode = "R";
        }

        // Write Access
        else if (methodName.startsWith("save") ||
                methodName.startsWith("delete")||
                methodName.startsWith("flush")) {
            mode = "W";
        }
        else {
            System.err.println("SpringDataRepositoryAccess Unknown Mode: " + methodName);
            return;
        }

        for (Repository r : repositories) {
            if (r.getRepositoryClassName().equals(repositoryClassName)) {
                String typeClassName = r.getTypeClassName();
                addEntitiesSequenceAccess(typeClassName, mode);
                return;
            }
        }
    }

    private boolean isProjectDeclaredRepository(String ctTypeName) {
        for (Repository r : repositories) {
            if (r.getRepositoryClassName().equals(ctTypeName))
                return true;
        }
        return false;
    }
}
