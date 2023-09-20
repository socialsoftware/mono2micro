package collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.appender.routing.IdlePurgePolicy;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;

import spoon.DecompiledResource;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.Map.Entry;

import javax.naming.Context;

public abstract class SpoonCollector {
    private int controllerCount;
    HashMap<String, Controller> controllerSequences;
    private String projectName;

    // all project classes
    protected Set<String> allEntities;

    // in FenixFramework: allDomainEntities = all classes that extend .*_Base
    // in collectors.JPA: allDomainEntities = all @Entities @Embeddable @MappedSuperClass
    protected Set<String> allDomainEntities;
    protected LinkedHashMap<String, Integer> entitiesMap;

    protected HashSet<CtClass> controllers;
    private Map<Integer, List<Access>> controllerAccesses;
    private Integer controllerContextCounter;
    private List<Integer> controllerContextStack;
    private Map<Integer, String> controllerContextType;
    private List<ContextStackListener> controllerContextStackObservers;
    protected Map<String, List<CtType>> abstractClassesAndInterfacesImplementorsMap;

    protected Factory factory;
    protected Launcher launcher;

    /* ------- TO REMOVE ----------- */
    protected int repositoryCount = 0;
    private int controllerMethodsCount = 0;

    public SpoonCollector(int launcherChoice, String repoName, String projectPath) throws IOException {
        File decompiledDir = new File(Constants.DECOMPILED_SOURCES_PATH);
        if (decompiledDir.exists()) {
            FileUtils.deleteDirectory(decompiledDir);
        }

        controllerSequences = new HashMap<>();
        controllers = new HashSet<>();
        allEntities = new HashSet<>();
        allDomainEntities = new TreeSet<>();
        entitiesMap = new LinkedHashMap<>();
        abstractClassesAndInterfacesImplementorsMap = new HashMap<>();

        this.projectName = repoName;

        switch (launcherChoice) {
            case Constants.LAUNCHER:
                launcher = new Launcher();
                launcher.addInputResource(projectPath);
                break;

            case Constants.MAVEN_LAUNCHER:
                launcher = new MavenLauncher(projectPath, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
                break;

            case Constants.JAR_LAUNCHER:
                launcher = new Launcher();
                File projectDir = new File(projectPath);
                String[] extensions = new String[]{"jar"};
                Collection<File> packageFiles = FileUtils.listFiles(
                        projectDir,
                        extensions,
                        true
                );

                for (File packageFile : packageFiles) {
                    String packagePath = packageFile.getPath();
                    String decompiledPackageSourcesDir = Constants.DECOMPILED_SOURCES_PATH
                            + packagePath.substring(packagePath.lastIndexOf("/")+1)
                            + File.separator;
                    new File(decompiledPackageSourcesDir).mkdirs();

                    new DecompiledResource(
                            packagePath,
                            null,
                            null,
                            decompiledPackageSourcesDir
                    );
                    launcher.addInputResource(decompiledPackageSourcesDir);
                }
                break;
            default:
                System.exit(1);
                break;
        }

        launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
        launcher.getEnvironment().setCommentEnabled(false);
    }

    public void run() throws IOException {
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

        storeJsonFile(Constants.COLLECTION_SAVE_PATH, projectName, controllerSequences);

        File file = new File(Constants.DECOMPILED_SOURCES_PATH);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }

        /* ------ TO REMOVE ------- */
        File testData = new File(Constants.TEST_DATA_PATH);
        if (!testData.exists()) testData.createNewFile();
        String s0 = "Project: " + projectName;
        String s1 = "#Controllers: " + controllers.size();
        String s15 = "#ControllerMethods: " + controllerMethodsCount;
        String s2 = "#Entities: " + allEntities.size();
        String s3 = "#DomainEntities: " + allDomainEntities.size();
        String s4 = "#Repositories: " + repositoryCount;
        StringBuilder sb = new StringBuilder()
                .append("---------------------------\n")
                .append(s0)
                .append("\n")
                .append(s1)
                .append("\n")
                .append(s15)
                .append("\n")
                .append(s2)
                .append("\n")
                .append(s3)
                .append("\n")
                .append(s4)
                .append("\n");
        Files.write(Paths.get(testData.getPath()), sb.toString().getBytes(), StandardOpenOption.APPEND);
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
            initializeContextArray();
            controllerAccesses = new HashMap<Integer, List<Access>>();
            Stack<SourcePosition> methodStack = new Stack<>();

            controllerMethodsCount++;
            methodCallDFS(controllerMethod, null, methodStack);

            if (controllerAccesses.size() > 0) {
                List<Trace> traces = new ArrayList<>();
                for(Map.Entry<Integer, List<Access>> contextAcesses: controllerAccesses.entrySet()) {
                    traces.add(new Trace(contextAcesses.getKey(), contextAcesses.getValue()));
                }
                controllerSequences.put(controllerFullName, new Controller(traces));
            }
        }
    }

    protected abstract void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack);

    protected abstract void collectControllersAndEntities();

    protected boolean existsAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return true;
        }
        return false;
    }

    private void storeJsonFile(String filepath, String fileName, HashMap<String, Controller> controllerSequences) {
        try {
            File filePath = new File(filepath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath+fileName + ".json"), controllerSequences);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath+fileName + "_linear.json"), getFlattenedControllerSequences(controllerSequences));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath+fileName + "_entityToID.json"), entitiesMap);

            LinkedHashMap<Integer, String> idToEntityMap = new LinkedHashMap<>();
            entitiesMap.forEach((s, integer) -> idToEntityMap.put(integer, s));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath+fileName+"_IDToEntity.json"), idToEntityMap);
            System.out.println("File '" + fileName + "' created at: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Controller> getFlattenedControllerSequences(HashMap<String, Controller> controllerSequences) {
        
        for (Controller controller : controllerSequences.values()) {
            
            Map<Integer, Trace> flatTraces = new HashMap<Integer, Trace>();

            // create map of traces, ordered by id (via a list)
            for(Trace trace: controller.getT()) {
                flatTraces.put(trace.getId(), trace);
            }

            List<Integer> idList = new ArrayList<Integer>(flatTraces.keySet());
            Collections.sort(idList);
            
            for (Integer i = idList.size()-1; i >= 0; i--) {
                
                Integer traceId = idList.get(i);
                
                List<Access> flatSequence = new ArrayList<Access>();

                for (Access access : flatTraces.get(traceId).getA()) {
                    if (access instanceof Label) {
                        // skip - not supported by flat traces
                    } else if(access instanceof ContextReference) {
                        // add flattened context sequence
                        flatSequence.addAll(flatTraces.get(access.getEntityID()).getA());
                    } else {
                        // add access
                        flatSequence.add(access);
                    }
                }

                flatTraces.put(traceId, new Trace(traceId, flatSequence)); // substitute the trace by its flat equivalent
            }

            List<Trace> newTraces = new ArrayList<Trace>();
            newTraces.add(flatTraces.get(0)); // full flat trace of the controller can be found in indice 0
            controller.setT(newTraces);
        }

        return controllerSequences;
    }

    protected List<CtMethod> getExplicitImplementationsOfAbstractMethod(CtMethod abstractMethod) throws UnkownMethodException {
        List<CtMethod> explicitMethods = new ArrayList<>();
        CtType<?> declaringType = abstractMethod.getDeclaringType();
        List<CtType> implementors = abstractClassesAndInterfacesImplementorsMap.get(declaringType.getSimpleName());
        if (implementors != null) {
            for (CtType ctImplementorType : implementors) {
                List<CtMethod> methodsByName = ctImplementorType.getMethodsByName(abstractMethod.getSimpleName());
                if (methodsByName.size() > 0) {
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

    protected boolean isEqualMethodSignature(CtExecutableReference method1, CtExecutableReference method2) throws UnkownMethodException {
        // compare return type
        try {
            if (method1.getType().equals(method2.getType())) {
                // compare parameters type
                List ctMParams1 = method1.getParameters();
                List ctMParams2 = method2.getParameters();
                if (ctMParams1.size() != ctMParams2.size())
                    return false;
                for (int i = 0; i < ctMParams1.size(); i++) {
                    CtTypeReference ctMParam1 = (CtTypeReference) ctMParams1.get(i);
                    CtTypeReference ctMParam2 = (CtTypeReference) ctMParams2.get(i);
                    if (!ctMParam1.equals(ctMParam2)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("isEqualMethodSignature Unknown method: " + method1.toString() + " " + method2.toString());
            throw new UnkownMethodException();
        }
    }

    public void addEntitiesSequenceAccess(String simpleName, String mode) {
        addEntitiesSequenceAccess(new Access(mode, entityToID(simpleName)));
    }
    
    public void addContextReference(int contextIndex, String type) {
        addEntitiesSequenceAccess(new ContextReference(contextIndex, type));
    }

    public void addLabel(String text) {
        addEntitiesSequenceAccess(new Label(text));
    }

    protected void addEntitiesSequenceAccess(Access access) {
        int currentContext = getCurrentContextIndex();
        if(controllerAccesses.get(currentContext) == null)
            controllerAccesses.put(currentContext, new ArrayList<Access>());
        controllerAccesses.get(currentContext).add(access);
    }


    private Integer entityToID(String simpleName) {
        return entitiesMap.get(simpleName);
    }

    public Integer getCurrentContextIndex() {
        if (controllerContextStack.size() == 0) return -1;
        return controllerContextStack.get(controllerContextStack.size() - 1);
    }

    public List<Integer> getContextStack() {
        return new ArrayList<Integer>(controllerContextStack);
    }

    public String getContextTypeFromIndex(int index) {
        return controllerContextType.get(index);
    }

    public String getCurrentContextType() {
        return controllerContextType.get(getCurrentContextIndex());
    }

    protected void openNewContext(String type) {
        controllerContextCounter++;
        controllerContextStack.add(controllerContextCounter);
        controllerContextType.put(controllerContextCounter, type);

        // update listeners
        if (controllerContextStackObservers != null)
            for (ContextStackListener listener : controllerContextStackObservers) {
                listener.contextClosed(controllerContextCounter);
            }
    }

    protected void openNewContext() {
        openNewContext("");
    }

    protected void closeCurrentContext() {
        if(controllerContextCounter > 0) {
            Integer previousContext = getCurrentContextIndex();
            String previousContextType = getCurrentContextType();
            controllerContextStack.remove(previousContext);
            controllerContextType.remove(previousContextType, previousContext);

            // insert context reference in the new current context
            if(previousContext != -1 && controllerAccesses.get(previousContext) != null)
                addContextReference(previousContext, previousContextType);
            else
                controllerContextCounter--;
            
            // update listeners
            if (controllerContextStackObservers != null)
                for (ContextStackListener listener : controllerContextStackObservers) {
                    listener.contextClosed(previousContext);
                }
        }
    }

    private void initializeContextArray() {
        //System.out.println("initialize context array");
        controllerContextCounter = -1;
        controllerContextStack = new ArrayList<Integer>();
        controllerContextType = new HashMap<Integer, String>();
        openNewContext(); // will bring controllerContextCounter to 0
    }

    public void addControllerContextListener(ContextStackListener listener) {
        if (controllerContextStackObservers == null)
            controllerContextStackObservers = new ArrayList<>();

        if (!controllerContextStackObservers.contains(listener))
            controllerContextStackObservers.add(listener);
    }

    public void removeControllerContextListener(ContextStackListener listener) {
        if (controllerContextStackObservers == null) return;

        if (controllerContextStackObservers.contains(listener))
            controllerContextStackObservers.remove(listener);
    }
}
