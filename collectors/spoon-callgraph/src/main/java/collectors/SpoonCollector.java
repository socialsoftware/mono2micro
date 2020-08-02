package collectors;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import spoon.DecompiledResource;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import util.Constants;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class SpoonCollector {
    private int controllerCount;
    private String projectName;

    private JsonObject callSequence;
    private List<Trace> controllerTraces; // list of traces
    private int traceId;
    private JsonArray entitiesSequence;
    private boolean foundAccess;

    private HashMap<Container<MySourcePosition>, Node> entitiesSequenceHashMap;
    protected Container<MySourcePosition> currentParentNodeId;
    private Set<Container<MySourcePosition>> optimizationProcessVisited;
    private HashMap<Container<MySourcePosition>, List<Access>> traverseProcessVisitedMap;
    private JsonGenerator jGenerator;

    // all project classes
    ArrayList<String> allEntities;

    // in FenixFramework: allDomainEntities = all classes that extend from .*_Base
    // in JPA: allDomainEntities = all @Entities @Embeddable @MappedSuperClass
    ArrayList<String> allDomainEntities;

    HashSet<CtClass> controllers;
    Map<String, List<CtType>> abstractClassesAndInterfacesImplementorsMap;

    Factory factory;
    Launcher launcher;

    SpoonCollector(int launcherChoice, String repoName, String projectPath) throws IOException {
        File decompiledDir = new File(Constants.DECOMPILED_SOURCES_PATH);
        if (decompiledDir.exists()) {
            FileUtils.deleteDirectory(decompiledDir);
        }

        callSequence = new JsonObject();
        controllers = new HashSet<>();
        allEntities = new ArrayList<>();
        allDomainEntities = new ArrayList<>();
        abstractClassesAndInterfacesImplementorsMap = new HashMap<>();
        foundAccess = false;

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

        String fileName = projectName + ".json";
        initJsonFile(Constants.COLLECTION_SAVE_PATH, fileName);

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

        // close jsonFile
        jGenerator.writeEndObject();
        jGenerator.close();

        File file = new File(Constants.DECOMPILED_SOURCES_PATH);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
    }

    private void initJsonFile(String filepath, String fileName) throws IOException {
        File filePath = new File(filepath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        JsonFactory jsonfactory = new ObjectMapper().getFactory();
        jGenerator = jsonfactory.createGenerator(
                new FileOutputStream(new File(Constants.COLLECTION_SAVE_PATH, fileName)),
                JsonEncoding.UTF8
        );
        jGenerator.setPrettyPrinter(new MinimalPrettyPrinter(""));
        jGenerator.writeStartObject();
    }

    private void processController(CtClass controller) throws IOException {
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

            if (!controllerFullName.contains("ToRemoveController"))
                continue;

            entitiesSequence = new JsonArray();
            entitiesSequenceHashMap = new HashMap<>();
            Stack<SourcePosition> methodStack = new Stack<>();
            Stack<Container<MySourcePosition>> nextNodeIdStack = new Stack<>();
            foundAccess = false;

            // create graph node begin
            ArrayList<MySourcePosition> id = new ArrayList<>();
            id.add(new MySourcePosition(controllerMethod.getPosition(), false, false, controllerMethod.toString()));
            Container<MySourcePosition> idC = new Container<>(id);
            Node beginNode = createGraphNode(idC);
            currentParentNodeId = beginNode.getId();
            methodCallDFS(controllerMethod, null, methodStack, nextNodeIdStack, idC);

            if (foundAccess) {
                System.out.println("Optimizing controller graph...");
                // child (controller method block begin) of the root node (controller node)
                optimizationProcessVisited = new HashSet<>();
                optimizeGraph(getGraphNode(idC).getEdges().get(0).getId());
                optimizationProcessVisited = null;

                System.out.println("Traversing controller graph...");
                traverseProcessVisitedMap = new HashMap<>();
                controllerTraces = new ArrayList<>();
                traceId = 0;
                traverseGraphPathCoverage(getGraphNode(idC), new ArrayList<>());
                traverseProcessVisitedMap = null;
                updateJsonWithNewController(controllerFullName);
                controllerTraces = null;

//                callSequence.add(controller.getSimpleName() + "." + controllerMethod.getSimpleName(), entitiesSequence);
            }
        }
    }

    private void updateJsonWithNewController(String controllerFullName) throws IOException {
        jGenerator.writeFieldName(controllerFullName);
        jGenerator.writeStartObject();
        jGenerator.writeFieldName("traces");
        jGenerator.writeStartArray();
        jGenerator.writeRaw('\n');
        for (Trace trace : controllerTraces) {
//            jGenerator.writeObject(trace);
            jGenerator.writeRaw('[');
            for (Access a : trace.getAccesses()) {
                jGenerator.writeRaw('[');
                jGenerator.writeString(a.getEntity());
                jGenerator.writeRaw(',');
                jGenerator.writeString(a.getType().toString());
                jGenerator.writeRaw(']');
                jGenerator.writeRaw(',');
            }
            jGenerator.writeRaw(']');
            jGenerator.writeRaw('\n');
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw('\n');
//        jGenerator.flush();
    }

    private void optimizeGraph(Container<MySourcePosition> nodeId) {
        if (optimizationProcessVisited.contains(nodeId))
            return; // reached by another branch, everything in front of this node is already optimized

        Node node = getGraphNode(nodeId);
        if (node == null)
            return;

        List<Node> children = new ArrayList<>(node.getEdges());
        if (node.getEntitiesSequence().size() == 0) {
            // delete node from graph
            List<Node> parents = new ArrayList<>(node.getParents());
            for (Node parent : parents) {
                unlinkEdge(parent, node);
                for (Node child : children) {
                    unlinkEdge(node, child);
                    linkNodes(parent, child);
                }
            }
            removeGraphNode(node.getId());
        }

        optimizationProcessVisited.add(nodeId);

        for (Node child : children)
            optimizeGraph(child.getId());
    }

    private List<Access> traverseGraphPathCoverage(Node node, List<Access> accessList) {
        List<Node> children = new ArrayList<>(node.getEdges());
        List<Access> nodeStoredValue = traverseProcessVisitedMap.get(node.getId());

        if (children.size() == 0 && nodeStoredValue == null) { // leaf not marked as visited yet
            traverseProcessVisitedMap.put(node.getId(), node.getEntitiesSequence());
            accessList.addAll(node.getEntitiesSequence());
//            System.out.println(accessList.toString()); // path clear trace
            controllerTraces.add(new Trace(traceId++, accessList));
            return node.getEntitiesSequence();
        }

        if (nodeStoredValue != null) { // already visited
            accessList.addAll(nodeStoredValue);
//            System.out.println(accessList.toString()); // path clear trace
            controllerTraces.add(new Trace(traceId++, accessList));
            return nodeStoredValue;
        }


        List<Access> returnedPathFromChild = null;
        accessList.addAll(node.getEntitiesSequence());
        for (Node child : children) {
            returnedPathFromChild = traverseGraphPathCoverage(child, new ArrayList<>(accessList));
        }

        List<Access> myPartialPath = new ArrayList<>();
        myPartialPath.addAll(node.getEntitiesSequence());
        myPartialPath.addAll(returnedPathFromChild);
        traverseProcessVisitedMap.put(node.getId(), myPartialPath);
        return myPartialPath;
    }

    abstract void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack, Stack<Container<MySourcePosition>> nextNodeIdStack, Container<MySourcePosition> id);

    abstract void collectControllersAndEntities();

    boolean existsAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return true;
        }
        return false;
    }

    private void storeJsonFile(String filepath, String fileName, JsonObject callSequence) {
        try {
            File filePath = new File(filepath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            FileWriter file = new FileWriter(new File(filePath, fileName));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(callSequence));
            file.close();

            System.out.println("File '" + fileName + "' created at: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Node createGraphNode(Container<MySourcePosition> id) {
        Node newNode = new Node(new Container<MySourcePosition>(id));
        entitiesSequenceHashMap.put(new Container<MySourcePosition>(id), newNode);
        return newNode;
    }

    private Node getGraphNode(Container<MySourcePosition> id) {
        return entitiesSequenceHashMap.get(id);
    }

    private void removeGraphNode(Container<MySourcePosition> id) {
        entitiesSequenceHashMap.remove(id);
    }

    void linkNodes(Container<MySourcePosition> originNodeId, Container<MySourcePosition> destNodeId) {
        entitiesSequenceHashMap.get(originNodeId).addEdge(entitiesSequenceHashMap.get(destNodeId));
        entitiesSequenceHashMap.get(destNodeId).addParent(entitiesSequenceHashMap.get(originNodeId));
    }

    void linkNodes(Node origin, Node dest) {
        origin.addEdge(dest);
        dest.addParent(origin);
    }

    private void unlinkEdge(Node parent, Node node) {
        parent.removeEdge(node);
        node.removeParent(parent);
    }

    void unlinkLast(Container<MySourcePosition> id) {
        Node node = entitiesSequenceHashMap.get(id);
        Node oldChild = node.removeLastEdge();
        oldChild.removeParent(node);
    }

    void addEntitiesSequenceAccess(String simpleName, String mode) {
        JsonArray entityAccess = new JsonArray();
        entityAccess.add(simpleName);
        entityAccess.add(mode);
        entitiesSequence.add(entityAccess);
        Access access = new Access(simpleName, Access.Type.valueOf(mode));
        entitiesSequenceHashMap.get(currentParentNodeId).addEntityAccess(access);
        foundAccess = true;
    }
}
