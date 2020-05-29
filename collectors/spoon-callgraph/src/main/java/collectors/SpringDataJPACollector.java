package collectors;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import parser.MyHqlParser;
import parser.QueryAccess;
import parser.TableNamesFinderExt;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import util.Classes;
import util.Constants;
import util.Query;
import util.Repository;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class SpringDataJPACollector extends SpoonCollector {

    private Map<String, CtType> entityClassNameMap; // map between EntityName -> ClassName
    private Map<String, Classes> tableClassesAccessedMap; // Classes related to a given table
    private ArrayList<Query> namedQueries; // list of tables accessed in a given query
    private List<Repository> repositories;
    private Repository eventualNewRepository;

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
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("DispatchAction"))
            ) {
                controllers.add((CtClass) clazz);
                continue;
            }

            if (isRepository(clazz.getReference())) {
                repositories.add(eventualNewRepository);
                eventualNewRepository = null;
                continue;
            }

            if (clazz.isInterface()) {
                interfaces.put(clazz.getSimpleName(), null);
                continue;
            }

            if (existsAnnotation(clazzAnnotations, "Entity") ||
                    existsAnnotation(clazzAnnotations, "Embeddable") ||
                    existsAnnotation(clazzAnnotations, "MappedSuperclass")) {
                allEntities.add(clazz.getSimpleName());
            }
        }

        allEntities.sort((string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first

        // 2nd iteration, to retrieve JPA related information and interface explicit implementations
        List<String> toDeleteInterfaces = new ArrayList<>();

        for(CtType<?> clazz : factory.Class().getAll()) {
            Set<CtTypeReference<?>> superInterfaces = clazz.getSuperInterfaces();
            for (CtTypeReference ctTypeReference : superInterfaces) {
                if (interfaces.containsKey(ctTypeReference.getSimpleName())) {
                    if (interfaces.get(ctTypeReference.getSimpleName()) == null) {
                        interfaces.replace(ctTypeReference.getSimpleName(), clazz);
                    }
                    else {
                        // more than one implementation for this interface
                        // don't replace interface calls by any explicit implementation
                        toDeleteInterfaces.add(ctTypeReference.getSimpleName());
                    }
                }
            }

            List<CtAnnotation<? extends Annotation>> clazzAnnotations = clazz.getAnnotations();
            CtAnnotation entityAnnotation = getAnnotation(clazzAnnotations, "Entity");
            if (entityAnnotation != null) { // Domain Class @Entity
                parseEntity(clazz, entityAnnotation, clazzAnnotations);
            }
        }

        for (String interfaceClassName : toDeleteInterfaces) {
            interfaces.remove(interfaceClassName);
        }

        super.repositoryCount = repositories.size();
    }

    /*
    *  isRepository: true if class Package = org.springframework.data.repository or if it extends (in)directly from it */
    private boolean isRepository(CtTypeReference<?> clazzReference) {
        try {
            if (isRepositoryPackageClass(clazzReference.getPackage().toString(), clazzReference.getSimpleName()))
                return true;

            for (Object o : clazzReference.getSuperInterfaces().toArray()) {
                CtTypeReference ctInterface = (CtTypeReference) o;

                if (eventualNewRepository == null) {
                    if (ctInterface.getActualTypeArguments().size() > 0) {
                        eventualNewRepository = new Repository(
                                clazzReference.getSimpleName(),
                                ctInterface.getActualTypeArguments().get(0).getSimpleName()
                        );
                    }
                    else
                        return false;
                }

                if (isRepositoryPackageClass(ctInterface.getPackage().getSimpleName(), ctInterface.getSimpleName())) {
                    return true;
                }
                else {
                    if (isRepository(ctInterface))
                        return true;
                }
            }

            eventualNewRepository = null;
            return false;
        } catch (Exception e) {
            eventualNewRepository = null;
            return false;
        }
    }

    private boolean isRepositoryPackageClass(String packageName, String className) {
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

            // JPA allows to either put the annotations on a field or its getter
            Optional<CtMethod> first = gettersList.stream()
                    .filter(m -> m.getSimpleName().equalsIgnoreCase("get" + field.getSimpleName()))
                    .findFirst();
            if (first.isPresent()) {
                fieldAnnotations.addAll(first.get().getAnnotations());
            }
            ;
            // ------------------- @ElementCollection -----------------------
            parseAtElementCollection(clazz, field, fieldAnnotations, entityName);

            // ------------------- @OneToOne -----------------------
            parseAtOneToOne(clazz, field, fieldAnnotations, tableName);

            // ------------------- @OneToMany -----------------------
            parseAtOneToMany(clazz, field, fieldAnnotations, tableName);

            // ------------------- @ManyToMany -----------------------
            parseAtManyToMany(clazz, field, fieldAnnotations, tableName);

            // ------------------- @ManyToOne -----------------------
            parseAtManyToOne(clazz, field, fieldAnnotations, tableName);
        }
    }

    private void parseAtOneToOne(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation oneToOneAnnotation = (CtAnnotation) getAnnotation(fieldAnnotations, "OneToOne");
        if (oneToOneAnnotation != null) {
            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (joinTableAnnotation != null) {
                if (joinTableAnnotation.getValues().get("name") != null) {
                    CtExpression name = joinTableAnnotation.getValue("name");
                    String joinTableName = getValueAsString(name);

                    Classes classes = new Classes();
                    classes.addClass(clazz.getSimpleName());
                    CtType fieldType = field.getType().getTypeDeclaration();
                    classes.addClass(fieldType.getSimpleName());
                    tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
                }
            }
        }
    }

    private void parseAtManyToOne(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation manyToOneAnnotation = getAnnotation(fieldAnnotations, "ManyToOne");
        if (manyToOneAnnotation != null) {
            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (joinTableAnnotation != null) {
                if (joinTableAnnotation.getValues().get("name") != null) {
                    CtExpression name = joinTableAnnotation.getValue("name");
                    String joinTableName = getValueAsString(name);

                    Classes classes = new Classes();
                    classes.addClass(clazz.getSimpleName());
                    CtType fieldType = field.getType().getTypeDeclaration();
                    classes.addClass(fieldType.getSimpleName());
                    tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
                }
            }
        }
    }

    private void parseAtManyToMany(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation manyToManyAnnotation = getAnnotation(fieldAnnotations, "ManyToMany");
        if (manyToManyAnnotation != null) {
            Map manyToManyValues = ((CtAnnotationImpl) manyToManyAnnotation).getElementValues();
            if (manyToManyValues.get("mappedBy") == null) { // new table

                String joinTableName = "";
                CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
                if (joinTableAnnotation != null) {
                    if (joinTableAnnotation.getValues().get("name") != null) {
                        CtExpression name = joinTableAnnotation.getValue("name");
                        joinTableName = getValueAsString(name);
                    }
                }
                Classes classes = new Classes();
                classes.addClass(clazz.getSimpleName());

                // @ManyToMany fields won't be HashMaps, thus, get(0), and the Type will always be an Entity
                CtType fieldType = field.getType().getActualTypeArguments().get(0).getTypeDeclaration();
                classes.addClass(fieldType.getSimpleName());

                if (joinTableName.equals("")) {
                    joinTableName = tableName + "_" + getEntityTableName(fieldType);
                }
                tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
            }
        }
    }

    private void parseAtOneToMany(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation oneToManyAnnotation = (CtAnnotation) getAnnotation(fieldAnnotations, "OneToMany");
        if (oneToManyAnnotation != null) {
            Map annotationValues = ((CtAnnotationImpl) oneToManyAnnotation).getElementValues();
            CtAnnotation joinColumnAnnotation = getAnnotation(fieldAnnotations, "JoinColumn");
            if (annotationValues.get("mappedBy") == null && joinColumnAnnotation == null) { // new table
                Classes classes = new Classes();
                classes.addClass(clazz.getSimpleName());

                CtType fieldType = null;
                List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
                for (CtTypeReference type : actualTypeArguments) {

                    fieldType = type.getTypeDeclaration();

                    if (allEntities.contains(fieldType.getSimpleName()))
                        classes.addClass(fieldType.getSimpleName());

                }

                String joinTableName = tableName + "_" + getEntityTableName(fieldType);
                tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
            }

            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (joinTableAnnotation != null) {
                String joinTableName = "";

                if (joinTableAnnotation.getValues().get("name") != null) {
                    CtExpression name = joinTableAnnotation.getValue("name");
                    joinTableName = getValueAsString(name);
                }

                Classes classes = new Classes();
                classes.addClass(clazz.getSimpleName());

                // @ManyToMany fields won't be HashMaps, thus, get(0), and the Type will always be an Entity
                CtType fieldType = field.getType().getActualTypeArguments().get(0).getTypeDeclaration();
                classes.addClass(fieldType.getSimpleName());

                if (joinTableName.equals("")) {
                    joinTableName = tableName + "_" + getEntityTableName(fieldType);
                }
                tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
            }
        }
    }

    private void parseAtElementCollection(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String entityName) {
        CtAnnotation elementCollectionAnnotation = getAnnotation(fieldAnnotations, "ElementCollection");
        if (elementCollectionAnnotation != null) {
            String joinTableName = "";
            CtAnnotation collectionTableAnnotation = getAnnotation(fieldAnnotations, "CollectionTable");
            if (collectionTableAnnotation != null) {
                CtExpression name = collectionTableAnnotation.getValue("name");
                joinTableName = getValueAsString(name);
            }
            Classes classes = new Classes();
            classes.addClass(clazz.getSimpleName());

            List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
            for (CtTypeReference type : actualTypeArguments) {

                CtType fieldType = type.getTypeDeclaration();

                if (allEntities.contains(fieldType.getSimpleName()))
                    classes.addClass(fieldType.getSimpleName());

            }

            if (joinTableName.equals("")) {
                joinTableName = entityName + "_" + field.getSimpleName();
            }
            tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
        }
    }

    private void parseAtNamedNativeQuery(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotation namedNativeQueryAnnotation = (CtAnnotation) getAnnotation(clazzAnnotations, "NamedNativeQuery");
        if (namedNativeQueryAnnotation != null) {
            parseNamedQuery(namedNativeQueryAnnotation, true);
        }
    }

    private void parseAtNamedNativeQueries(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotationImpl namedNativeQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "NamedNativeQueries");
        if (namedNativeQueriesAnnotation != null) {
            List namedNativeQueryList = ((CtNewArray) namedNativeQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedNativeQueryObject : namedNativeQueryList) {
                CtAnnotation namedNativeQueryElement = ((CtAnnotation) namedNativeQueryObject);
                parseNamedQuery(namedNativeQueryElement, true);
            }
        }
    }

    private void parseAtNamedQuery(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotation namedQueryAnnotation = (CtAnnotation) getAnnotation(clazzAnnotations, "NamedQuery");
        if (namedQueryAnnotation != null) {
            parseNamedQuery(namedQueryAnnotation, false);
        }
    }

    private void parseAtNamedQueries(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotationImpl namedQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "NamedQueries");
        if (namedQueriesAnnotation != null) {
            List namedQueryList = ((CtNewArray) namedQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedQueryObject : namedQueryList) {
                CtAnnotation namedQueryElement = ((CtAnnotation) namedQueryObject);
                parseNamedQuery(namedQueryElement, false);
            }
        }
    }

    private String parseAtTableAndAtInheritance(CtType clazz) {
        boolean isSingleTable = false;
        CtType<?> superClassType = null;
        if (clazz.getSuperclass() != null) {
            superClassType = clazz.getSuperclass().getTypeDeclaration();
            if (superClassType != null) {
                CtAnnotation<? extends Annotation> inheritanceAnnotation =
                        getAnnotation(superClassType.getAnnotations(), "Inheritance");
                if (inheritanceAnnotation != null) {
                    isSingleTable = inheritanceAnnotation.getValue("strategy").toString().contains("SINGLE_TABLE");
                }
            }
        }
        Classes classes = new Classes();
        String tableName;
        if (isSingleTable) {
            tableName = getEntityTableName(superClassType);
            // table is not created (= superclass table name)
        }
        else {
            tableName = getEntityTableName(clazz);
            classes.addClass(clazz.getSimpleName());
            tableClassesAccessedMap.put(tableName.toUpperCase(), classes);
        }
        return tableName;
    }

    private String parseAtEntity(CtType clazz, CtAnnotation atEntityAnnotation) {
        String entityName = clazz.getSimpleName();

        Object name = ((CtAnnotationImpl) atEntityAnnotation).getElementValues().get("name");
        if (name != null)
            entityName = getValueAsString(name);

        entityClassNameMap.put(entityName, clazz);

        return entityName;
    }

    private String getEntityTableName(CtType clazz) {
        if (clazz == null) return "";

        String tableName = clazz.getSimpleName();
        List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();
        // ------------------- @Table -----------------------
        CtAnnotation tableAnnotation = getAnnotation(annotations, "Table");
        if (tableAnnotation != null) {
            Object name = ((CtAnnotationImpl) tableAnnotation).getElementValues().get("name");
            if (name != null) {
                String redefinedTableName = getValueAsString(name);
                tableName = redefinedTableName.trim();
            }

        }
        return tableName;
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
                        if (((CtInvocation) calleeLocation).getTarget().getType().getSimpleName().contains("EntityManager"))
                            System.err.println(calleeLocation.toString() + "\n" +calleeLocation.getPosition());
                    } catch (Exception e) {}

                    try {
                        boolean entityManager = ((CtInvocation) calleeLocation).getTarget().getType().getSimpleName().contains("EntityManager");
                        if (entityManager) {
                            SpringDataJPACollector.this.entityManager++;
                        }
                    } catch (Exception e) {}

                    try {
                        CtMethod calleeMethod = (CtMethod) calleeLocation.getExecutable().getExecutableDeclaration();
                        if (calleeMethod.isAbstract()) {
                            boolean alreadyProcessed = updateCalleeLocationWithExplicitImplementation(calleeLocation, calleeMethod);
                            if (alreadyProcessed)
                                return;
                        }
                    } catch(Exception castError) {
                        // ConstructorCall, ignore
                    }

                    // In some cases the SpringDataJPA may be in the classpath (for instance when MavenLauncher is
                    // used. In other cases, it won't, and getDeclaringType to a SpringDataJPA class will return null
                    if (calleeLocation.getExecutable().getDeclaringType() == null ||
                            isRepository(calleeLocation.getExecutable().getDeclaringType())) {
                        // possible call to a repository
                        CtTypeReference targetTypeReference = ((CtInvocationImpl) calleeLocation).getTarget().getType();
                        String targetClassName = targetTypeReference.getSimpleName();

                        if (isProjectDeclaredRepository(targetClassName)) {
                            if (calleeLocation.getExecutable().getDeclaringType() == null) {
                                // out of our classpath -> SpringData default repository method
                                // however it may be in the class path (checked below)
                                registerSpringDataRepositoryAccess(targetClassName, calleeLocation.getExecutable().getSimpleName());
                            }
                            else {
                                CtType targetType = targetTypeReference.getTypeDeclaration();
                                List<CtMethod> methodsByName = targetType.getMethodsByName(calleeLocation.getExecutable().getSimpleName());

                                boolean declared = false;
                                for (CtMethod ctM : methodsByName) {
                                    if (compareWithMethod(calleeLocation.getExecutable(), ctM)) {
                                        // method declared in project's class
                                        declared = true;
                                        registerRepositoryAccess(calleeLocation.getExecutable());
                                        break;
                                    }
                                }
                                if (!declared) {
                                    registerSpringDataRepositoryAccess(targetClassName, calleeLocation.getExecutable().getSimpleName());
                                }
                            }
                        }
                    }
                    else if (isCallToCollections(calleeLocation)) {
                        inspectTargetFromCall((CtInvocation) calleeLocation);
                        if (collectionEntityAccess && !collectionTransientField) {
                            registerDomainAccess(collectionDeclaringTypeName, collectionFieldAccessedType, (CtInvocation) calleeLocation);
                        }
                    }
                    else if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().getPosition())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation, methodStack);
                    }

                } catch (Exception e) {
                    if (!(e instanceof ClassCastException || e instanceof NullPointerException)) {
                        System.err.println(e.getCause().getMessage());
                    }
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

                    if (allEntities.contains(declaringTypeName)) {

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
                                    if (allEntities.contains(ctTypeReference.getSimpleName())) {
                                        addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), mode);
                                    }
                                }
                            }
                            else {
                                if (allEntities.contains(fieldType.getSimpleName())) {
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
            }
        });

        methodStack.pop();
    }

    private boolean compareWithMethod(CtExecutableReference executable, CtMethod ctM) {
        // compare return type
        if (ctM.getType().getSimpleName().equals(executable.getType().getSimpleName())) {
            // compare parameters type
            List ctMParameters = ctM.getParameters();
            List executableParameters = executable.getParameters();
            if (ctMParameters.size() != executableParameters.size())
                return false;
            for (int i = 0; i < ctMParameters.size(); i++) {
                CtParameter ctMP = (CtParameter) ctMParameters.get(i);
                CtTypeReference eP = ((CtTypeReference) executableParameters.get(i));
                if (!ctMP.getType().getSimpleName().equals(eP.getSimpleName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
    * Return true = we reached a call to a SpringData Repository and we processed it. End of this call chain
    * was reached so the methodCallDFS function does not have to proceed (also if it proceeded it would
    * count the repository access twice).
    * Return false = either explicit implementation was found and replaced the calleeLocation of the
    * methodCallDFS function or implementation was not found.
    * */
    private boolean updateCalleeLocationWithExplicitImplementation(CtAbstractInvocation calleeLocation, CtMethod calleeLocationMethod) {
        // methodCallDFS calleeLocation will be changed by reference
        CtType interfaceImplType = interfaces.get(calleeLocationMethod.getDeclaringType().getSimpleName());
        if (interfaceImplType != null) {
            // calling an abstract method declared in an interface
            List methodsByName = interfaceImplType.getMethodsByName(calleeLocation.getExecutable().getSimpleName());
            if (methodsByName.size() > 0) {
                // redirect pointer to the explicit method
                Object explicitMethodObject = methodsByName.get(0);
                CtMethod calleeLocationExplicit = (CtMethod) explicitMethodObject;
                CtExecutableReference reference = calleeLocationExplicit.getReference();
                calleeLocation.setExecutable(reference);
            }

            // replace the target of the call by the explicit target
            CtConstructorCall call = factory.Core().createConstructorCall();
            call.setType(factory.Core().createTypeReference().setSimpleName(interfaceImplType.getSimpleName()));
            ((CtInvocation) calleeLocation).setTarget(call);

            if (methodsByName.size() == 0) {
                // May be a SpringDataJPA implicit method access
                // unfortunately we cant get a reference to those methods so we have to duplicate code
                String targetClassName = ((CtInvocation) calleeLocation).getTarget().getType().getSimpleName();
                if (isProjectDeclaredRepository(targetClassName)) {
                    registerSpringDataRepositoryAccess(targetClassName, calleeLocation.getExecutable().getSimpleName());
                }
                // end of call chain
                return true;
            }
        }
        return false;
    }

    private <T> boolean isTransient(CtField<T> fieldDeclaration) {
        List<CtAnnotation<? extends Annotation>> annotations = fieldDeclaration.getAnnotations();
        if (existsAnnotation(annotations, "Transient") ||
                hasModifier(fieldDeclaration, "TRANSIENT") ||
                hasModifier(fieldDeclaration, "STATIC") ||
                hasModifier(fieldDeclaration, "FINAL")) {
            return true;
        }
        return false;
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
        targetExpression.accept(new CtScanner() {
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                visitTarget(invocation.getExecutable().getExecutableDeclaration());
            }

            @Override
            public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                super.visitCtFieldRead(fieldRead);
                CtField<T> fieldDeclaration = fieldRead.getVariable().getFieldDeclaration();
                String declaringTypeName = fieldDeclaration.getDeclaringType().getSimpleName();

                if (isTransient(fieldDeclaration)) {
                    collectionTransientField = true;
                }
                else {
                    collectionTransientField = false;
                }

                if (allEntities.contains(declaringTypeName)) {
                    collectionEntityAccess = true;
                    collectionFieldAccessedType = fieldDeclaration.getType();
                    collectionDeclaringTypeName = declaringTypeName;
                    return;
                }
                collectionEntityAccess = false;
                collectionFieldAccessedType = null;
                collectionDeclaringTypeName = null;
            }

            @Override
            public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                super.visitCtVariableRead(variableRead);
                String declaringClassName = variableRead.getVariable().getDeclaration().getParent(CtClass.class).getSimpleName();
                if (allEntities.contains(declaringClassName)) {
                    collectionEntityAccess = true;
                    collectionFieldAccessedType = variableRead.getVariable().getType();
                    collectionDeclaringTypeName = declaringClassName;
                    collectionTransientField = false;
                    return;
                }
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
                if (allEntities.contains(ctTypeReference.getSimpleName())) {
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
                    if (allEntities.contains(ctTypeReference.getSimpleName())) {
                        addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), mode);
                    }
                }
            }
            else {
                if (allEntities.contains(returnType.getSimpleName())) {
                    addEntitiesSequenceAccess(returnType.getSimpleName(), mode);
                }
            }
        }
        else if (mode.equals("W")) {
            for (String argTypeName : argTypeNames) {
                if (allEntities.contains(argTypeName)) {
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

    private void registerRepositoryAccess(CtExecutableReference methodDeclaration) {
        List<CtAnnotation<? extends Annotation>> methodAnnotations = methodDeclaration.getExecutableDeclaration().getAnnotations();
        CtAnnotation<? extends Annotation> queryAnnotation = getAnnotation(methodAnnotations, "Query");
        if (queryAnnotation != null) {
            CtAnnotation<? extends Annotation> query =
                    getAnnotation(methodDeclaration.getExecutableDeclaration().getAnnotations(), "Query");
            String value = getValueAsString(query.getValue("value"));
            Boolean nativeQuery = (Boolean) ((CtLiteral) query.getValue("nativeQuery")).getValue();
            String namedQueryName = getValueAsString(query.getValue("name"));

            if (!namedQueryName.equals("")) {
                // TODO not used i think
                System.err.println("Error: @Query with name attribute!");
                System.exit(1);

            }

            if (nativeQuery) {
                parseNativeQuery(value);
            }
            else { // HQL query
                parseHqlQuery(value);
            }
        }
        else {
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
                    parseHqlQuery(q.getValue());
            }
        }
    }

    // TODO (select *) case
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

    private void parseHqlQuery(String hql) {
        try {
            Set<QueryAccess> accesses = new MyHqlParser(hql).parse();
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
                            List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
                            if (actualTypeArguments.size() > 0) { // Set<Class> List<Class> etc
                                for (CtTypeReference ctTypeReference : actualTypeArguments) {
                                    if (allEntities.contains(ctTypeReference.getSimpleName())) {
                                        addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), a.getMode());
                                        clazz = ctTypeReference.getTypeDeclaration(); // in order to retrieve next fields
                                    }
                                }
                            }
                            else {
                                if (allEntities.contains(field.getType().getSimpleName())) {
                                    addEntitiesSequenceAccess(field.getType().getSimpleName(), a.getMode());
                                    clazz = field.getType().getTypeDeclaration(); // in order to retrieve next fields
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
        String mode = null;
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
                methodName.startsWith("delete")) {
            mode = "W";
        }

        if (mode != null) {
            for (Repository r : repositories) {
                if (r.getRepositoryClassName().equals(repositoryClassName)) {
                    String typeClassName = r.getTypeClassName();
                    addEntitiesSequenceAccess(typeClassName, mode);
                    return;
                }
            }
        }
    }

    private boolean isProjectDeclaredRepository(String className) {
        for (Repository r : repositories) {
            if (r.getRepositoryClassName().equals(className))
                return true;
        }
        return false;
    }

    private void parseNamedQuery(CtAnnotation namedQueryAnnotation, boolean isNative) {
        Map queryValues = ((CtAnnotationImpl) namedQueryAnnotation).getElementValues();
        String name = getValueAsString(queryValues.get("name"));
        String query = getValueAsString(queryValues.get("query"));
        this.namedQueries.add(new Query(name, query, isNative));
    }

    private String getValueAsString(Object name) {
        if (name instanceof CtLiteral)
            return (String) ((CtLiteral) name).getValue();
        else if (name instanceof CtFieldRead)
            return (String) ((CtLiteral) ((CtFieldRead) name).getVariable().getDeclaration().getAssignment()).getValue();
        else if (name instanceof CtBinaryOperatorImpl)
            return getValueAsString(((CtBinaryOperatorImpl) name).partiallyEvaluate());
        else {
            System.err.println("Couldn't parse value! " + name.toString());
            System.exit(1);
            return null;
        }
    }

    private CtAnnotation<? extends Annotation> getAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return a;
        }
        return null;
    }
}
