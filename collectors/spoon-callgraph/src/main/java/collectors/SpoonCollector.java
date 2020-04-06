package collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import spoon.Launcher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class SpoonCollector {

    private int controllerCount;
    private JsonObject callSequence;
    private String projectName;

    // in FenixFramework: allEntities = all classes != _Base
    // in JPA: allEntities = all @Entities @Embeddable @MappedSuperClass
    ArrayList<String> allEntities;
    HashSet<CtClass> controllers;
    Map<String, List<CtAbstractInvocation>> methodCallees;
    private JsonArray entitiesSequence;
    Map<String, CtType> interfaces;

    Factory factory;
    Launcher launcher;

    SpoonCollector(String projectPath, String repoName) {
        callSequence = new JsonObject();
        controllers = new HashSet<>();
        allEntities = new ArrayList<>();
        methodCallees = new HashMap<>();
        interfaces = new HashMap<>();
        launcher = new Launcher();
        launcher.addInputResource(projectPath);
        this.projectName = repoName;
    }

    public void run() {
        factory = launcher.getFactory();

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
        System.out.println("File created at: " + filepath);
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
            System.out.println("Processing Controller: " + controllerFullName + "   " + controllerCount + "/" + controllers.size());
            entitiesSequence = new JsonArray();
            Stack<String> methodStack = new Stack<>();

            methodCallDFS(controllerMethod, null, methodStack);

            if (entitiesSequence.size() > 0) {
                callSequence.add(controller.getSimpleName() + "." + controllerMethod.getSimpleName(), entitiesSequence);
            }
        }
    }

    abstract void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<String> methodStack);

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
