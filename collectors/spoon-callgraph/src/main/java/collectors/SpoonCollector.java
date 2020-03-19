package collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import spoon.Launcher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class SpoonCollector {

    private String projectPath;
    private int controllerCount;
    private JsonObject callSequence;

    ArrayList<String> allEntities;
    HashSet<CtClass> controllers;
    Map<String, List<CtAbstractInvocation>> methodCallees;
    JsonArray entitiesSequence;

    Factory factory;
    Launcher launcher;
//    private String controllerGlobal;
//    private int lastPositionSeen;

    SpoonCollector(String projectPath) {
        this.projectPath = projectPath;
        callSequence = new JsonObject();
        controllers = new HashSet<>();
        allEntities = new ArrayList<>();
        methodCallees = new HashMap<>();
        launcher = new Launcher();
        launcher.addInputResource(projectPath);
    }

    public void run() {
        factory = launcher.getFactory();

        String projectName = null;
        String[] split = projectPath.split("/");
        for (String s : split) {
            if (s.equals("src"))
                break;
            projectName = s;
        }
        System.out.println("Processing Project: " + projectName);
        long startTime = System.currentTimeMillis();
        collectControllersAndEntities();

        controllerCount = 0;
        for (CtClass controller : controllers) {
            controllerCount++;
            processController(controller);
        }

        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");

        String filepath = System.getProperty("user.dir") + "/" + "spoon_callSequence_" + projectName + ".json";
        storeJsonFile(filepath, callSequence);
    }

    private void processController(CtClass controller) {
        for (Object cM : controller.getMethods()) {
            CtMethod controllerMethod = (CtMethod) cM;

            //Check if the controller method return type == ActionForward
            if (controller.getSuperclass() != null && controller.getSuperclass().getSimpleName().contains("DispatchAction")) {
                if (!controllerMethod.getType().toString().contains("ActionForward"))
                    continue;
            } else {
                List<CtAnnotation<? extends Annotation>> annotations = controllerMethod.getAnnotations();
                if (!existsAnnotation(annotations, "RequestMapping") &&
                    !existsAnnotation(annotations, "GetMapping") &&
                    !existsAnnotation(annotations, "PostMapping") &&
                    !existsAnnotation(annotations, "PatchMapping") &&
                    !existsAnnotation(annotations, "PutMapping") &&
                    !existsAnnotation(annotations, "DeleteMapping")
                ) {
                    continue;
                }
            }

            String controllerFullName = controller.getSimpleName() + "." + controllerMethod.getSimpleName();
//            controllerGlobal = controllerFullName;
//            lastPositionSeen = 0;
            System.out.println("Processing Controller: " + controllerFullName + "   " + controllerCount + "/" + controllers.size());
            entitiesSequence = new JsonArray();
            Stack<String> methodStack = new Stack<>();

//            if (controller.getSimpleName().equals("SearchController")) {
//                System.out.println("breakppoint");
//            }
            methodCallDFS(controllerMethod, methodStack);

            if (entitiesSequence.size() > 0) {
                callSequence.add(controller.getSimpleName() + "." + controllerMethod.getSimpleName(), entitiesSequence);
            }
        }
    }

    abstract void methodCallDFS(CtExecutable controllerMethod, Stack<String> methodStack);
    /*
    // TO REMOVE (a medida que insere verifica se está a dar match com a "expert collection")
    if (lastPositionSeen >= entitiesSequence.size())
        continue;

    LinkedTreeMap jdt = (LinkedTreeMap) new Gson().fromJson(new FileReader(util.JsonComparator.JDTCALLGRAPHPATH), Object.class);
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

    abstract void collectControllersAndEntities();

    List<CtAbstractInvocation> getCalleesOf(CtExecutable method) {
        Map<Integer,CtAbstractInvocation> orderedCallees = new TreeMap<>();
        List<CtAbstractInvocation> methodCalls = method.getElements(new TypeFilter<>(CtAbstractInvocation.class));
        for (CtAbstractInvocation i : methodCalls)
            try {
                orderedCallees.put(i.getPosition().getSourceEnd(), i);
            } catch (Exception ignored) {} // "super" fantasmas em construtores
        return new ArrayList<>(orderedCallees.values()); // why are we ordering the calls?
    }

    boolean existsAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return true;
        }
        return false;
    }

    private void storeJsonFile(String filepath, JsonObject callSequence) {
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

    void addEntitiesSequenceAccess(String simpleName, String mode) {
        JsonArray entityAccess = new JsonArray();
        entityAccess.add(simpleName);
        entityAccess.add(mode);
        entitiesSequence.add(entityAccess);
    }
}
