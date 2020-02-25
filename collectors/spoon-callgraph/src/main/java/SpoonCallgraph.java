import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtReturnImpl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.List;

public class SpoonCallgraph {

    private static HashSet<CtClass> controllers;
    private static ArrayList<String> allEntities;
    private static Set<String> abstractEntities;
    private static int controllerCount;
    private static Factory factory;
    private static JsonArray entitiesSequence;
    private static Map<String, List<CtAbstractInvocation>> methodCallees;
    private static JsonObject callSequence;
    private static String controllerGlobal;
    private static int lastPositionSeen;


    public static void main(String[] args) throws Exception {

        Launcher launcher = new Launcher();

        // To prompt
        launcher.addInputResource("/home/samuel/ProjetoTese/edition-master/edition-ldod");

        launcher.getEnvironment().setSourceClasspath(new String[]{"./lib/fenix-framework-core-2.0.jar", "./lib/spring-context-5.2.3.RELEASE.jar"});
        launcher.buildModel();
        CtModel model = launcher.getModel();

        System.out.println("Processing Project: "); // add prompt
        long startTime = System.currentTimeMillis();

        factory = launcher.getFactory();
        callSequence = new JsonObject();
        controllers = new HashSet<>();
        allEntities = new ArrayList<>();
        abstractEntities = new HashSet<>();
        methodCallees = new HashMap<>();

        collectControllersAndEntities();

        controllerCount = 0;
        for (CtClass controller : controllers) {
            controllerCount++;
            processController(controller);
        }

        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");

        //file store
        String filepath = System.getProperty("user.dir") + "/" + "spoon_callSequence.json";
        if (filepath != null) {
            try {
                FileWriter file = new FileWriter(filepath);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(callSequence));
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void collectControllersAndEntities() {
        for(CtType<?> clazz : factory.Class().getAll()) {
            List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();

            if (existsAnnotation(annotations, "Controller") ||
                existsAnnotation(annotations, "RestController") ||
                existsAnnotation(annotations, "RequestMapping") ||
                clazz.getSimpleName().endsWith("Controller") ||
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("FenixDispatchAction"))  // test
            ) {
                controllers.add((CtClass) clazz);
                allEntities.add(clazz.getSimpleName());
            } else if (!clazz.getSimpleName().endsWith("_Base")) {  //Domain class
                if (clazz.isAbstract())
                    abstractEntities.add(clazz.getSimpleName());
                allEntities.add(clazz.getSimpleName());
            }
        }

        allEntities.sort((string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first
    }

    private static void processController(CtClass controller) throws Exception {

        for (Object cM : controller.getMethods()) {
            CtMethod controllerMethod = (CtMethod) cM;

            //For Fenix: check if the controller method return type == ActionForward
            if (controller.getSuperclass() != null && controller.getSuperclass().getTypeDeclaration().getSimpleName().equals("FenixDispatchAction")) {
                if (!controllerMethod.getType().toString().contains("ActionForward")) // simpleName ou toString?
                    continue;
            } else {
                CtTypeReference<? extends Annotation> requestMappingAnnotation = factory.Type().createReference("org.springframework.web.bind.annotation.RequestMapping");
                CtAnnotation<? extends Annotation> annotation = controllerMethod.getAnnotation(requestMappingAnnotation);
                if (annotation == null) { // if not annotated with @RequestMapping continue
                    continue;
                }
            }

            String controllerFullName = controller.getSimpleName() + "." + controllerMethod.getSimpleName();
            controllerGlobal = controllerFullName;
            lastPositionSeen = 0;
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

    private static void methodCallDFS(CtExecutable callerMethod, Stack<String> methodStack) throws Exception {
        methodStack.push(callerMethod.toString());

        if (!methodCallees.containsKey(callerMethod.toString())) {
            methodCallees.put(callerMethod.toString(), getCalleesOf(callerMethod));
        }

        for (CtAbstractInvocation calleeLocation : methodCallees.get(callerMethod.toString())) {

            try {
                //System.out.println(calleeLocation.getExecutable().getSimpleName());
                if (calleeLocation == null)
                    continue;
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                    registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                }
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                        calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                    registerDomainObject(callerMethod, calleeLocation);
                } else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().toString())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), methodStack);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            // TO REMOVE
            if (lastPositionSeen >= entitiesSequence.size())
                continue;

            LinkedTreeMap jdt = (LinkedTreeMap) new Gson().fromJson(new FileReader(JsonComparator.JDTCALLGRAPHPATH), Object.class);
            for (Object jdtElement : jdt.entrySet()) {
                Map.Entry<String, ArrayList> jdtElement1 = (Map.Entry<String, ArrayList>) jdtElement;
                if (jdtElement1.getKey().equals(controllerGlobal)) {
                    ArrayList value = jdtElement1.getValue();
                    int sizeSoFar = entitiesSequence.size();
                    for (int i = lastPositionSeen; i<sizeSoFar; i++) {
                        String access = entitiesSequence.get(i).toString(); // access registered
                        String expertAccess = (String) ((ArrayList) value.get(i)).get(0);
                        if (!expertAccess.equals(access.substring(2, access.indexOf(",")-1))) {
                            int i1 = 1 + 1; // breakpoint
                        }
                    }
                }
            }
            lastPositionSeen = entitiesSequence.size();
            */
        }
        methodStack.pop();
    }

    private static void registerBaseClass(CtExecutable callee, CtAbstractInvocation calleeLocation) {
        String methodName = callee.getSimpleName();
        String mode = "";
        String returnType = "";
        String argType = "";
        if (methodName.startsWith("get")) {
            mode = "R";
            returnType = callee.getType().toString();
        } else if (methodName.startsWith("set") || methodName.startsWith("add") || methodName.startsWith("remove")) {
            mode = "W";
            CtParameter parameter = (CtParameter) callee.getParameters().get(0);
            argType = parameter.getType().toString();
        }

        String baseClassName = callee.getParent(CtClass.class).getSimpleName();
        baseClassName = baseClassName.substring(0,baseClassName.length()-5); //remove _Base

        String[] resolvedType = new String[] {" "};
        //resolvedType[0] = String.valueOf(calleeLocation.getExecutable().getDeclaringType());

        resolvedType[0] = resolveTypeBase(calleeLocation);
        // verificar! O que é o resolveTypeBinding() ?? Está a dar retorno diferente

        // String a = virtualEdition.getAcronym()
        // Neste caso getAcronym está definido na superclasse Edition, portanto eu iria obter uma leitura de Edition
        // em vez de VirtualEdition se não fizesse o resolveBinding

        if (mode.equals("R")) {
            if (allEntities.contains(resolvedType[0])) {
                JsonArray entityAccess = new JsonArray();
                entityAccess.add(resolvedType[0]);
                entityAccess.add(mode);
                entitiesSequence.add(entityAccess);
            } else if (allEntities.contains(baseClassName)) {
                JsonArray entityAccess = new JsonArray();
                entityAccess.add(baseClassName);
                entityAccess.add(mode);
                entitiesSequence.add(entityAccess);
            }

            for (String entityName : allEntities) {
                if (returnType.contains(entityName)) {
                    JsonArray entityAccess = new JsonArray();
                    entityAccess.add(entityName);
                    entityAccess.add(mode);
                    entitiesSequence.add(entityAccess);
                    return;
                }
            }

        } else if (mode.equals("W")) {
            if (allEntities.contains(resolvedType[0])) {
                JsonArray entityAccess = new JsonArray();
                entityAccess.add(resolvedType[0]);
                entityAccess.add(mode);
                entitiesSequence.add(entityAccess);
            } else if (allEntities.contains(baseClassName)) {
                JsonArray entityAccess = new JsonArray();
                entityAccess.add(baseClassName);
                entityAccess.add(mode);
                entitiesSequence.add(entityAccess);
            }

            for (String entityName : allEntities) {
                if (argType.contains(entityName)) {
                    JsonArray entityAccess = new JsonArray();
                    entityAccess.add(entityName);
                    entityAccess.add(mode);
                    entitiesSequence.add(entityAccess);
                    return;
                }
            }
        }
    }

    private static String resolveTypeBase(CtAbstractInvocation calleeLocation) {
        CtExpression target = ((CtInvocationImpl) calleeLocation).getTarget();
        if (target.getTypeCasts().size() > 0)
            return ((CtTypeReference) target.getTypeCasts().get(0)).getSimpleName();
        else
            return target.getType().getSimpleName();
    }

    private static void registerDomainObject(CtExecutable method, CtAbstractInvocation abstractCalleeLocation) throws Exception {
        // final CurricularCourse course = FenixFramework.getDomainObject(values[0]);
        // return (DepartmentUnit) FenixFramework.getDomainObject(this.getSelectedDepartmentUnitID());
        CtInvocation calleeLocation = (CtInvocation) abstractCalleeLocation;
        String resolvedType;
        resolvedType = resolveTypeDomainObject(calleeLocation);


        if (allEntities.contains(resolvedType)) {
            JsonArray entityAccess = new JsonArray();
            entityAccess.add(resolvedType);
            entityAccess.add("R");
            entitiesSequence.add(entityAccess);
        }
    }

    private static String resolveTypeDomainObject(CtInvocation calleeLocation) throws Exception {
        CtElement parent = calleeLocation.getParent();
        if (calleeLocation.getTypeCasts().size() > 0)
            return ((CtTypeReference) calleeLocation.getTypeCasts().get(0)).getSimpleName();
        else if (parent instanceof CtReturnImpl)
            return parent.getParent(CtMethod.class).getType().getSimpleName();
        else if (parent instanceof CtLocalVariable)
            return ((CtLocalVariable) parent).getType().getSimpleName();
        else if (parent instanceof CtAssignment)
            return ((CtAssignment) parent).getAssigned().getType().getSimpleName();
        else
            throw new Exception("Couldn't Resolve Type.");
    }

    private static CtExecutable getMethodFromCtInvocation(CtAbstractInvocation calleeLocation) {
        // se executableDeclaration = null entao o metodo invocado nao esta no classpath dado ao spoon, ou seja,
        // nao e um metodo do repositorio
        return calleeLocation.getExecutable().getExecutableDeclaration();
    }

    private static List<CtAbstractInvocation> getCalleesOf(CtExecutable method) {
        Map<Integer,CtAbstractInvocation> orderedCallees = new TreeMap<>();
        List<CtAbstractInvocation> methodCalls = method.getElements(new TypeFilter<>(CtAbstractInvocation.class));
        for (CtAbstractInvocation i : methodCalls)
            try {
                orderedCallees.put(i.getPosition().getSourceEnd(), i);
            } catch (Exception ignored) {} // "super" fantasmas em construtores
        return new ArrayList<>(orderedCallees.values()); // why are we ordering the calls?
    }

    private static boolean existsAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return true;
        }
        return false;
    }
}
