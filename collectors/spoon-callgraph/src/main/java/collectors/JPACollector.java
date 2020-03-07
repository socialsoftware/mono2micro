package collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import spoon.Launcher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class JPACollector {
    private final String projectPath;
    private HashSet<CtClass> controllers;
    private ArrayList<String> allEntities;
    private Set<String> abstractEntities;
    private int controllerCount;
    private Factory factory;
    private JsonArray entitiesSequence;
    private Map<String, List<CtAbstractInvocation>> methodCallees;
    private ArrayList<Entity> domainEntitiesList;
    private ArrayList<Query> namedQueries;
    private JsonObject callSequence;

    public JPACollector(String projectPath) {
        this.projectPath = projectPath;
    }

    public void run() throws Exception {
        Launcher launcher = new Launcher();
        launcher.addInputResource("/home/samuel/ProjetoTese/spring-petclinic/src/main"); // put project path
        launcher.getEnvironment().setSourceClasspath(new String[]{"./lib/spring-context-5.2.3.RELEASE.jar"});
        launcher.buildModel();

        System.out.println("Processing JPA Project: " /* + projectName*/);
        long startTime = System.currentTimeMillis();

        factory = launcher.getFactory();
        callSequence = new JsonObject();
        controllers = new HashSet<>();
        allEntities = new ArrayList<>();
        abstractEntities = new HashSet<>();
        methodCallees = new HashMap<>();
        domainEntitiesList = new ArrayList<>();
        namedQueries = new ArrayList<>();

        collectControllersAndEntities();

        controllerCount = 0;
        for (CtClass controller : controllers) {
            controllerCount++;
            processController(controller);
        }

        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");

        // extract to SpoonCallgraph
        //file store
        String filepath = System.getProperty("user.dir") + "/" + "spoon_callSequence_JPA.json";
        CallgraphUtil.storeJsonFile(filepath, callSequence);
    }

    private void collectControllersAndEntities() {
        for(CtType<?> clazz : factory.Class().getAll()) {
            List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();

            if (CallgraphUtil.existsAnnotation(annotations, "Controller") ||
                CallgraphUtil.existsAnnotation(annotations, "RestController") ||
                CallgraphUtil.existsAnnotation(annotations, "RequestMapping") ||
                CallgraphUtil.existsAnnotation(annotations, "GetMapping") ||
                CallgraphUtil.existsAnnotation(annotations, "PostMapping") ||
                CallgraphUtil.existsAnnotation(annotations, "PatchMapping") ||
                CallgraphUtil.existsAnnotation(annotations, "PutMapping") ||
                CallgraphUtil.existsAnnotation(annotations, "DeleteMapping") ||
                clazz.getSimpleName().endsWith("Controller") ||
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("DispatchAction"))
            ) {
                controllers.add((CtClass) clazz);
                allEntities.add(clazz.getSimpleName());
            } else {
                CtAnnotation entityAnnotation = getAnnotation(annotations, "Entity");
                if (entityAnnotation != null) { // Domain Class @Entity
                    String entityName = clazz.getSimpleName();
                    String tableName = clazz.getSimpleName();

                    CtLiteral redefinedEntityName = (CtLiteral) ((CtAnnotationImpl) entityAnnotation).getElementValues().get("name");
                    if (redefinedEntityName != null)
                        entityName = (String) redefinedEntityName.getValue();

                    // ------------------- @Table -----------------------
                    CtAnnotation tableAnnotation = getAnnotation(annotations, "Table");
                    if (tableAnnotation != null) {
                        CtLiteral redefinedTableName = (CtLiteral) ((CtAnnotationImpl) tableAnnotation).getElementValues().get("name");
                        tableName = (String) redefinedTableName.getValue();
                    }

                    // ------------------- @NamedQueries -----------------------
                    CtAnnotationImpl namedQueriesAnnotation = (CtAnnotationImpl) getAnnotation(annotations, "NamedQueries");
                    if (namedQueriesAnnotation != null) {
                        List namedQueryList = ((CtNewArray) namedQueriesAnnotation.getElementValues().get("value")).getElements();

                        for (Object namedQueryObject : namedQueryList) {
                            CtAnnotation namedQueryElement = ((CtAnnotation) namedQueryObject);
                            parseNamedQuery(namedQueryElement, false);
                        }
                    }

                    // ------------------- @NamedQuery -----------------------
                    CtAnnotation namedQueryAnnotation = (CtAnnotation) getAnnotation(annotations, "NamedQuery");
                    if (namedQueryAnnotation != null) {
                        parseNamedQuery(namedQueryAnnotation, false);
                    }

                    // ------------------- @NamedNativeQueries -----------------------
                    CtAnnotationImpl namedNativeQueriesAnnotation = (CtAnnotationImpl) getAnnotation(annotations, "NamedNativeQueries");
                    if (namedNativeQueriesAnnotation != null) {
                        List namedNativeQueryList = ((CtNewArray) namedNativeQueriesAnnotation.getElementValues().get("value")).getElements();

                        for (Object namedNativeQueryObject : namedNativeQueryList) {
                            CtAnnotation namedNativeQueryElement = ((CtAnnotation) namedNativeQueryObject);
                            parseNamedQuery(namedNativeQueryElement, true);
                        }
                    }

                    // ------------------- @NamedNativeQuery -----------------------
                    CtAnnotation namedNativeQueryAnnotation = (CtAnnotation) getAnnotation(annotations, "NamedNativeQuery");
                    if (namedNativeQueryAnnotation != null) {
                        parseNamedQuery(namedNativeQueryAnnotation, true);
                    }

                    if (clazz.isAbstract()) {
                        abstractEntities.add(clazz.getSimpleName());
                    }
                    domainEntitiesList.add(new Entity(clazz.getSimpleName(), entityName, tableName));
                    allEntities.add(clazz.getSimpleName());
                }
            }
        }

        allEntities.sort((string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first
    }

    private void processController(CtClass controller) throws Exception {
        for (Object cM : controller.getMethods()) {
            CtMethod controllerMethod = (CtMethod) cM;

            //check if the controller method return type == ActionForward
            if (controller.getSuperclass() != null && controller.getSuperclass().getSimpleName().equals("DispatchAction")) {
                if (!controllerMethod.getType().toString().contains("ActionForward"))
                    continue;
            } else {
                List<CtAnnotation<? extends Annotation>> annotations = controllerMethod.getAnnotations();
                if (!CallgraphUtil.existsAnnotation(annotations, "RequestMapping") &&
                    !CallgraphUtil.existsAnnotation(annotations, "GetMapping") &&
                    !CallgraphUtil.existsAnnotation(annotations, "PostMapping") &&
                    !CallgraphUtil.existsAnnotation(annotations, "PatchMapping") &&
                    !CallgraphUtil.existsAnnotation(annotations, "PutMapping") &&
                    !CallgraphUtil.existsAnnotation(annotations, "DeleteMapping")
                ) {
                    continue;
                }
            }

            String controllerFullName = controller.getSimpleName() + "." + controllerMethod.getSimpleName();
            System.out.println("Processing Controller: " + controllerFullName + "   " + controllerCount + "/" + controllers.size());
            entitiesSequence = new JsonArray();
            Stack<String> methodStack = new Stack<>();

            /*if (controller.getSimpleName().equals("SearchController")) {
                System.out.println("breakppoint");
            }*/
            methodCallDFS(controllerMethod, methodStack);

            if (entitiesSequence.size() > 0) {
                callSequence.add(controller.getSimpleName() + "." + controllerMethod.getSimpleName(), entitiesSequence);
            }
        }
    }

    private void methodCallDFS(CtExecutable callerMethod, Stack<String> methodStack) throws Exception {
        methodStack.push(callerMethod.toString());

        if (!methodCallees.containsKey(callerMethod.toString())) {
            methodCallees.put(callerMethod.toString(), CallgraphUtil.getCalleesOf(callerMethod));
        }

        for (CtAbstractInvocation calleeLocation : methodCallees.get(callerMethod.toString())) {

            try {
                //System.out.println(calleeLocation.getExecutable().getSimpleName());
                if (calleeLocation == null)
                    continue;
                /*else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                    registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                }
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                        calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                    registerDomainObject(callerMethod, calleeLocation);
                } else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().toString())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), methodStack);
                    }
                }*/
                if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().toString())) {
                    methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), methodStack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        methodStack.pop();
    }

    private void parseNamedQuery(CtAnnotation namedQueryAnnotation, boolean isNative) {
        Map queryValues = ((CtAnnotationImpl) namedQueryAnnotation).getElementValues();
        String name = (String) ((CtLiteral) queryValues.get("name")).getValue();
        String query = (String) ((CtLiteral) queryValues.get("query")).getValue();
        this.namedQueries.add(new Query(null, name, query, isNative));
    }

    private CtAnnotation<? extends Annotation> getAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return a;
        }
        return null;
    }
}
