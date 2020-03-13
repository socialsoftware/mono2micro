package collectors;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.google.gson.JsonArray;
import parser.HqlAccess;
import parser.MyHqlParser;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import util.Classes;
import util.Query;
import util.Repository;

import java.lang.annotation.Annotation;
import java.util.*;

/*
* Assumptions:
* A SpringDataJPA project never uses EntityManager
*
* @Query annotations do not reference namedQueries
*
* All repositories are explicitly implemented and do not
* implement interfaces other than SpringFramework interfaces
*
* Inheritance (SINGLE_TABLE) no more than one level of inheritance
* */
public class SpringDataJPACollector extends SpoonCollector {

    private Map<String, CtType> entityClassNameMap; // map between EntityName -> ClassName
    private Map<String, Classes> tableClassesAccessedMap; // Classes related to a given table
    private ArrayList<Query> namedQueries; // list of tables accessed in a given query
    private List<Repository> repositories;

    public SpringDataJPACollector(String projectPath) {
        super(projectPath);
        launcher.getEnvironment().setSourceClasspath(new String[]{
                "./lib/spring-context-5.2.3.RELEASE.jar",
                "./lib/spring-data-commons-core-1.4.1.RELEASE.jar",
                "./lib/spring-data-jpa-2.2.5.RELEASE.jar"
        });
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
                clazz.getSimpleName().endsWith("Controller") ||
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("DispatchAction"))
            ) {
                controllers.add((CtClass) clazz);
                continue;
            }

            Repository repository = checkIfClassIsARepository(clazz, clazzAnnotations);
            if (repository != null) {
                repositories.add(repository);
            }
            else {
                CtAnnotation entityAnnotation = getAnnotation(clazzAnnotations, "Entity");
                if (entityAnnotation != null) { // Domain Class @Entity
                    parseEntity(clazz, entityAnnotation, clazzAnnotations);
                }
            }
            allEntities.add(clazz.getSimpleName());
        }

        allEntities.sort((string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first
    }

    private Repository checkIfClassIsARepository(CtType<?> clazz, List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        // Spring Data JPA Repository
        for (Object o : clazz.getSuperInterfaces().toArray()) {
            CtTypeReference ctInterface = (CtTypeReference) o;
            String packageName = ctInterface.getPackage().getSimpleName();
            if (packageName.equals("org.springframework.data.jpa.repository") ||
                    packageName.equals("org.springframework.data.repository"))
                return new Repository(clazz.getSimpleName(), ctInterface.getActualTypeArguments().get(0).getSimpleName());
        }

        return null;
    }

    private void parseEntity(CtType<?> clazz, CtAnnotation entityAnnotation, List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        String entityName = clazz.getSimpleName();

        // ------------------- @Entity -----------------------
        CtLiteral redefinedEntityName = (CtLiteral) ((CtAnnotationImpl) entityAnnotation).getElementValues().get("name");
        if (redefinedEntityName != null)
            entityName = (String) redefinedEntityName.getValue();

        entityClassNameMap.put(entityName, clazz);

        // ------------------- @Table -----------------------
        // ------------------- @Inheritance -----------------
        boolean isSingleTable = false;
        CtType<?> superClass = null;
        if (clazz.getSuperclass() != null) {
            superClass = clazz.getSuperclass().getTypeDeclaration();
            CtAnnotation<? extends Annotation> inheritanceAnnotation =
                    getAnnotation(superClass.getAnnotations(), "Inheritance");
            if (inheritanceAnnotation != null) {
                isSingleTable = inheritanceAnnotation.getValue("strategy").toString().contains("SINGLE_TABLE");
            }
        }
        Classes classes = new Classes();
        String tableName;
        if (isSingleTable) {
            tableName = getEntityTableName(superClass);
            // table is not created (= superclass table name)
        }
        else {
            tableName = getEntityTableName(clazz);
            classes.addClass(clazz);
            tableClassesAccessedMap.put(tableName, classes);
        }

        // ------------------- @NamedQueries -----------------------
        CtAnnotationImpl namedQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "NamedQueries");
        if (namedQueriesAnnotation != null) {
            List namedQueryList = ((CtNewArray) namedQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedQueryObject : namedQueryList) {
                CtAnnotation namedQueryElement = ((CtAnnotation) namedQueryObject);
                parseNamedQuery(namedQueryElement, false);
            }
        }

        // ------------------- @NamedQuery -----------------------
        CtAnnotation namedQueryAnnotation = (CtAnnotation) getAnnotation(clazzAnnotations, "NamedQuery");
        if (namedQueryAnnotation != null) {
            parseNamedQuery(namedQueryAnnotation, false);
        }

        // ------------------- @NamedNativeQueries -----------------------
        CtAnnotationImpl namedNativeQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "NamedNativeQueries");
        if (namedNativeQueriesAnnotation != null) {
            List namedNativeQueryList = ((CtNewArray) namedNativeQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedNativeQueryObject : namedNativeQueryList) {
                CtAnnotation namedNativeQueryElement = ((CtAnnotation) namedNativeQueryObject);
                parseNamedQuery(namedNativeQueryElement, true);
            }
        }

        // ------------------- @NamedNativeQuery -----------------------
        CtAnnotation namedNativeQueryAnnotation = (CtAnnotation) getAnnotation(clazzAnnotations, "NamedNativeQuery");
        if (namedNativeQueryAnnotation != null) {
            parseNamedQuery(namedNativeQueryAnnotation, true);
        }

        // ------------------- Check Fields -----------------------
        for (CtField field : clazz.getFields()) {

            List<CtAnnotation<? extends Annotation>> fieldAnnotations = field.getAnnotations();

            // ------------------- @OneToMany -----------------------
            CtAnnotation oneToManyAnnotation = (CtAnnotation) getAnnotation(fieldAnnotations, "OneToMany");
            if (oneToManyAnnotation != null) {
                Map annotationValues = ((CtAnnotationImpl) oneToManyAnnotation).getElementValues();
                CtAnnotation joinColumnAnnotation = getAnnotation(fieldAnnotations, "JoinColumn");
                if (annotationValues.get("mappedBy") == null && joinColumnAnnotation == null) { // new table
                    classes = new Classes();
                    classes.addClass(clazz);
                    CtType fieldType = field.getType().getActualTypeArguments().get(0).getTypeDeclaration();
                    classes.addClass(fieldType);
                    String joinTableName = tableName + "_" + getEntityTableName(fieldType);
                    tableClassesAccessedMap.put(joinTableName, classes);
                }
            }

            // ------------------- @ManyToMany -----------------------
            CtAnnotation manyToManyAnnotation = getAnnotation(fieldAnnotations, "ManyToMany");
            if (manyToManyAnnotation != null) {
                Map manyToManyValues = ((CtAnnotationImpl) manyToManyAnnotation).getElementValues();
                if (manyToManyValues.get("mappedBy") == null) { // new table

                    String joinTableName = "";
                    CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
                    if (joinTableAnnotation != null) {
                        CtExpression CtJoinTableName = joinTableAnnotation.getValue("name");
                        joinTableName = (String) ((CtLiteral) CtJoinTableName).getValue();
                    }
                    classes = new Classes();
                    classes.addClass(clazz);
                    CtType fieldType = field.getType().getActualTypeArguments().get(0).getTypeDeclaration();
                    classes.addClass(fieldType);
                    if (joinTableName.equals("")) {
                        joinTableName = tableName + "_" + getEntityTableName(fieldType);
                    }
                    tableClassesAccessedMap.put(joinTableName, classes);
                }
            }
        }
    }

    private String getEntityTableName(CtType clazz) {
        String tableName = clazz.getSimpleName();
        List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();
        // ------------------- @Table -----------------------
        CtAnnotation tableAnnotation = getAnnotation(annotations, "Table");
        if (tableAnnotation != null) {
            CtLiteral redefinedTableName = (CtLiteral) ((CtAnnotationImpl) tableAnnotation).getElementValues().get("name");
            tableName = (String) redefinedTableName.getValue();
        }
        return tableName;
    }



    @Override
    public void methodCallDFS(CtExecutable callerMethod, Stack<String> methodStack) {
        methodStack.push(callerMethod.toString());

        if (!methodCallees.containsKey(callerMethod.toString())) {
            methodCallees.put(callerMethod.toString(), getCalleesOf(callerMethod));
        }

        for (CtAbstractInvocation calleeLocation : methodCallees.get(callerMethod.toString())) {

            try {
                //System.out.println(calleeLocation.getExecutable().getSimpleName());
                if (calleeLocation == null)
                    continue;
                else if (calleeLocation.getExecutable().getDeclaringType() == null) {
                    // Non declared method. SpringFramework Repository default query
                    // extends JpaRepository
                    String targetClassName = ((CtInvocationImpl) calleeLocation).getTarget().getType().getSimpleName();
                    if (isRepository(targetClassName)) {
                        registerSpringDataRepositoryAccess(targetClassName, calleeLocation.getExecutable().getSimpleName());
                    }
                }
                else if (isRepository(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    registerRepositoryAccess(calleeLocation.getExecutable());
                }

                else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().toString())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), methodStack);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        methodStack.pop();
    }

    private void registerRepositoryAccess(CtExecutableReference methodDeclaration) {
        List<CtAnnotation<? extends Annotation>> methodAnnotations = methodDeclaration.getExecutableDeclaration().getAnnotations();
        CtAnnotation<? extends Annotation> queryAnnotation = getAnnotation(methodAnnotations, "Query");
        if (queryAnnotation != null) {
            CtAnnotation<? extends Annotation> query =
                    getAnnotation(methodDeclaration.getExecutableDeclaration().getAnnotations(), "Query");
            String value = (String) ((CtLiteral) query.getValue("value")).getValue();
            Boolean nativeQuery = (Boolean) ((CtLiteral) query.getValue("nativeQuery")).getValue();
            String namedQueryName = (String) ((CtLiteral) query.getValue("name")).getValue();

            if (!namedQueryName.equals("")) {
                // TODO
                System.err.println("Error: @Query with name attribute!");
                System.exit(1);

            }

            if (nativeQuery) {

            }
            else { // HQL query
                parseHqlQuery(value);
            }
        }
        else {
            Repository r = getRepositoryFromClassName(methodDeclaration.getDeclaringType().getSimpleName());
            String typeClassName = r.getTypeClassName(); // never null
            // Spring Data first tries to resolve calls to repository methods to a named query, starting with the simple name of the configured domain class, followed by the method name separated by a dot.
            Query q = getNamedQuery(typeClassName + "." + methodDeclaration.getSimpleName());
            if (q == null)
                registerSpringDataRepositoryAccess(methodDeclaration.getDeclaringType().getSimpleName(), methodDeclaration.getSimpleName());
            else {
                parseHqlQuery(q.getValue());
            }
        }
    }

    private void parseHqlQuery(String hql) {
        try {
            Set<HqlAccess> accesses = new MyHqlParser(hql).parse();
            for (HqlAccess a : accesses) {
                String entityName = a.getEntityName();
                String[] split = entityName.split("\\.");

                CtType clazz = entityClassNameMap.get(split[0]);
                if (clazz == null) {
                    // TODO (select *owner* From Owner) case
                }
                else {
                    addEntitiesSequenceAccess(clazz.getSimpleName(), a.getMode());
                    for (int i = 1; i < split.length; i++) {
                        String fieldName = split[i];
                        CtField field = clazz.getField(fieldName);
                        if (field != null) {
                            List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
                            if (actualTypeArguments.size() > 0) { // Set<Class> List<Class> etc
                                CtTypeReference<?> ctTypeReference = actualTypeArguments.get(0);
                                if (allEntities.contains(ctTypeReference.getSimpleName())) {
                                    addEntitiesSequenceAccess(ctTypeReference.getSimpleName(), a.getMode());
                                }
                            }
                            else {
                                if (allEntities.contains(field.getType().getSimpleName())) {
                                    addEntitiesSequenceAccess(field.getType().getSimpleName(), a.getMode());
                                }
                            }
                        }
                        clazz = field.getType().getTypeDeclaration(); // in order to retrieve next fields
                    }
                }
            }
        } catch (TokenStreamException | RecognitionException e) {
            System.err.println("QueryException on query: \"" + hql + "\"");
            e.printStackTrace();
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
                    JsonArray entityAccess = new JsonArray();
                    entityAccess.add(typeClassName);
                    entityAccess.add(mode);
                    entitiesSequence.add(entityAccess);
                    return;
                }
            }
        }
    }

    private boolean isRepository(String className) {
        for (Repository r : repositories) {
            if (r.getRepositoryClassName().equals(className))
                return true;
        }
        return false;
    }

    private void parseNamedQuery(CtAnnotation namedQueryAnnotation, boolean isNative) {
        Map queryValues = ((CtAnnotationImpl) namedQueryAnnotation).getElementValues();
        String name = (String) ((CtLiteral) queryValues.get("name")).getValue();
        String query = (String) ((CtLiteral) queryValues.get("query")).getValue();
        this.namedQueries.add(new Query(name, query, isNative));
    }

    private CtAnnotation<? extends Annotation> getAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return a;
        }
        return null;
    }
}
