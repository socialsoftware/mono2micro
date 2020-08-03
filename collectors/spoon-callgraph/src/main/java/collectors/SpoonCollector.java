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
    private JsonArray entitiesSequence;
    private boolean foundAccess;

    private HashMap<Container, Node> entitiesSequenceHashMap;
    protected Container currentParentNodeId;
    protected HashMap<CtExecutable, MethodNodeReference> methodNodeReferenceMap;
    private int methodIdIncrementWhenCopying = 0;
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
            long partialStartTime = System.currentTimeMillis();
            processController(controller);
            System.out.println("Controller elapsed time: " + (System.currentTimeMillis() - partialStartTime)/1000F + " seconds");
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

//            if (!controllerFullName.contains("getPublicVirtualEditions4User"))
//                continue;

            entitiesSequence = new JsonArray();
            entitiesSequenceHashMap = new HashMap<>();
            methodNodeReferenceMap = new HashMap<>();
            Stack<SourcePosition> methodStack = new Stack<>();
            Stack<Container> nextNodeIdStack = new Stack<>();
            foundAccess = false;

            // create graph node begin
            ArrayList<MySourcePosition> id = new ArrayList<>();
            id.add(new MySourcePosition(controllerMethod.getPosition(), false, false, controllerMethod.toString()));
            Container idC = new Container(id);
            Node beginNode = createGraphNode(idC);
            currentParentNodeId = beginNode.getId();
            methodCallDFS(controllerMethod, null, methodStack, nextNodeIdStack, idC);

            if (foundAccess) {
                System.out.println("Optimizing controller graph...");
                // child (controller method block begin) of the root node (controller node)
                optimizeGraph(getGraphNode(idC).getEdges().get(0).getId());

                System.out.println("Traversing controller graph...");
                jsonUpdateNewController(controllerFullName);
                /*List<Trace> traces = */traverseGraphPathCoverage(getGraphNode(idC), new ArrayList<>());
                jsonCloseNewController();
//                updateJsonWithNewController(controllerFullName, traces);

//                callSequence.add(controller.getSimpleName() + "." + controllerMethod.getSimpleName(), entitiesSequence);
            }
        }
    }

    private void jsonUpdateNewController(String controllerName) throws IOException {
        jGenerator.writeFieldName(controllerName);
        jGenerator.writeStartObject();
        jGenerator.writeFieldName("traces");
        jGenerator.writeStartArray();
        jGenerator.writeRaw('\n');
    }

    private void jsonWriteTrace(Trace trace) throws IOException {
        for (Access a : trace.getAccesses()) {
            jGenerator.writeRaw('[');
            jGenerator.writeString(a.getEntity());
            jGenerator.writeRaw(',');
            jGenerator.writeString(a.getType().toString());
            jGenerator.writeRaw(']');
            jGenerator.writeRaw(',');
        }
    }

    private void jsonCloseNewController() throws IOException {
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw('\n');
        jGenerator.flush();
    }

    private void updateJsonWithNewController(String controllerFullName, List<Trace> traces) throws IOException {
        jGenerator.writeFieldName(controllerFullName);
        jGenerator.writeStartObject();
        jGenerator.writeFieldName("traces");
        jGenerator.writeStartArray();
        jGenerator.writeRaw('\n');
        for (Trace trace : traces) {
////            jGenerator.writeObject(trace);
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
            jGenerator.writeRaw(',');
            jGenerator.writeRaw('\n');
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.writeRaw('\n');
        jGenerator.flush();
    }

    private void optimizeGraph(Container nodeId) {
        System.out.println("Before optimization - " + entitiesSequenceHashMap.size());
        Set<Container> visited = new HashSet<>();
        optimizeGraphAux(nodeId, visited);
        System.out.println("After optimization - " + entitiesSequenceHashMap.size());
    }

    private void optimizeGraphAux(Container nodeId, Set<Container> visited) {
        if (visited.contains(nodeId))
            return; // reached by another branch, everything in front of this node is already optimized

        Node node = getGraphNode(nodeId);
        if (node == null)
            return;

        List<Node> children = new ArrayList<>(node.getEdges());
        if (node.getParents().size() == 1)
            deleteNodeFromGraph(node);

        visited.add(nodeId);

        for (Node child : children)
            optimizeGraphAux(child.getId(), visited);
    }


    private void traverseGraphPathCoverage(Node node, List<Access> accessList) throws IOException {
//        HashMap<Container, List<Access>> visitedMap = new HashMap<>();
//        List<Trace> controllerTraces = new ArrayList<>();
//        traverseGraphPathCoverageAux(node, accessList, visitedMap, controllerTraces);
        traverseGraphPathCoverageAux(node, accessList);
//        return controllerTraces;
    }

    private void traverseGraphPathCoverageAux(Node node, List<Access> accessList/*, HashMap<Container, List<Access>> visitedMap, List<Trace> controllerTraces*/) throws IOException {
//        System.out.println(node.getId().toString());
        List<Node> children = node.getEdges();
//        List<Access> nodeStoredValue = visitedMap.get(node.getId());

        if (children.size() == 0 /*&& nodeStoredValue == null*/) { // leaf not marked as visited yet
//            visitedMap.put(node.getId(), node.getAccesses());

            accessList.addAll(node.getAccesses());
            jsonWriteTrace(new Trace(-1, accessList));
//            System.out.println(accessList.toString()); // path clear trace
//            controllerTraces.add(new Trace(-1, accessList));
//            return node.getAccesses();
            return;
        }

//        if (nodeStoredValue != null) { // already visited
//            accessList.addAll(nodeStoredValue);
//            jsonWriteTrace(new Trace(-1, accessList));
////            System.out.println(accessList.toString()); // path clear trace
////            controllerTraces.add(new Trace(-1, accessList));
//            return nodeStoredValue;
//        }

//        List<Access> returnedPathFromChild = null;
        accessList.addAll(node.getAccesses());
        for (Node child : children) {
            /*returnedPathFromChild = */
            traverseGraphPathCoverageAux(child, new ArrayList<>(accessList)/*, visitedMap, controllerTraces*/);
        }

//        List<Access> myPartialPath = new ArrayList<>();
//        myPartialPath.addAll(node.getAccesses());
//        myPartialPath.addAll(returnedPathFromChild);
//        visitedMap.put(node.getId(), myPartialPath);
//        return myPartialPath;
    }

    void storeMethodSubGraph(MethodNodeReference mnr) {
        mnr.setBeginNode(new Node(mnr.getBeginNode()));
        mnr.getBeginNode().setParents(new ArrayList<>());
        HashMap<Node, Node> map = new HashMap<>();
        copySubGraph(mnr.getBeginNode(), map);
        mnr.setEndNode(map.get(mnr.getEndNode()));
        mnr.setBeginNode(map.get(mnr.getBeginNode()));
        linkNodesOnCopy(map);
    }

    private void linkNodesOnCopy(HashMap<Node, Node> map) {
        for (Node key : map.keySet()) {
            for (Node keyEdge : key.getEdges()) {
                linkNodes(map.get(key), map.get(keyEdge));
            }
        }
    }

    private void copySubGraph(Node node, HashMap<Node, Node> map) {
        if (map.get(node) == null) {
            Node nodeCopy = new Node(node);
            nodeCopy.setParents(new ArrayList<>());
            nodeCopy.setEdges(new ArrayList<>());
            map.put(node, nodeCopy);
        }
        else {
            return;
        }

        for (Node n : node.getEdges()) {
            copySubGraph(n, map);
        }
    }

    void extendGraphWithMethodSubGraph(Container id, MethodNodeReference methodNodeReference) {
        Node beginNode = methodNodeReference.getBeginNode();
        HashMap<Node, Node> map = new HashMap<>();
        methodIdIncrementWhenCopying = 1;
        extendGraphWithMethodSubGraphAux(id, beginNode, map);
        linkNodesOnCopy(map);

        Node nodeCopyBegin = map.get(beginNode);

        linkNodes(entitiesSequenceHashMap.get(this.currentParentNodeId), nodeCopyBegin);

        currentParentNodeId = map.get(methodNodeReference.getEndNode()).getId();
    }

    private void extendGraphWithMethodSubGraphAux(Container id, Node node, HashMap<Node, Node> map) {
        if (map.get(node) == null) {
            Container cCopy = new Container(id);
            cCopy.list.get(id.list.size() - 1).setNumber(methodIdIncrementWhenCopying++);
            Node nodeCopy = createGraphNode(cCopy);
            nodeCopy.setAccesses(node.getAccesses());
            map.put(node, nodeCopy);
        }
        else
            return;

        for (Node child : node.getEdges())
            extendGraphWithMethodSubGraphAux(id, child, map);
    }

    abstract void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack, Stack<Container> nextNodeIdStack, Container id);

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

    Node createGraphNode(Container id) {
        Node newNode = new Node(new Container(id));
        entitiesSequenceHashMap.put(new Container(id), newNode);
        return newNode;
    }

    private Node getGraphNode(Container id) {
        return entitiesSequenceHashMap.get(id);
    }

    private void deleteNodeFromGraph(Node node) {
        List<Node> parents = new ArrayList<>(node.getParents());
        List<Node> children = new ArrayList<>(node.getEdges());
        for (Node parent : parents) {
            unlinkEdge(parent, node);
            for (Node child : children) {
                unlinkEdge(node, child);
                linkNodes(parent, child);
            }
            node.getAccesses().forEach(a -> parent.addEntityAccess(a));
        }
        entitiesSequenceHashMap.remove(node.getId());
    }

    void linkNodes(Container originNodeId, Container destNodeId) {
        if (!originNodeId.equals(destNodeId)) {
            entitiesSequenceHashMap.get(originNodeId).addEdge(entitiesSequenceHashMap.get(destNodeId));
            entitiesSequenceHashMap.get(destNodeId).addParent(entitiesSequenceHashMap.get(originNodeId));
        }
        else {
            System.out.println("");
        }
    }

    void linkNodes(Node origin, Node dest) {
        if (!origin.equals(dest)) {
            origin.addEdge(dest);
            dest.addParent(origin);
        }
        else {
            System.out.println("");
        }
    }

    private void unlinkEdge(Node parent, Node node) {
        parent.removeEdge(node);
        node.removeParent(parent);
    }

    void unlinkLast(Container id) {
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
