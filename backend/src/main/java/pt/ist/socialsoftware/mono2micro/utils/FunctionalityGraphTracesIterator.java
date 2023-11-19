package pt.ist.socialsoftware.mono2micro.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.AbstractCall;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Call;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.HeuristicFlags;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.If;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Label;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Loop;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Switch;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraph;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraphNode;

/**
 * FunctionalityGraphTracesIterator
 */
public class FunctionalityGraphTracesIterator extends TracesIterator {
    private final String _representationName;
    private final JSONObject _codebaseAsJSON;
    private static ConcurrentMap<String, FunctionalityInfo> _graphInfo = new ConcurrentHashMap<>();
    private String requestedFunctionality;

    public FunctionalityGraphTracesIterator(String representationName, InputStream file) throws IOException, JSONException {
        _representationName = representationName;
        _codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();
        
        // load graphs from cache or launch building thread
        System.out.println("FunctionalityGraphTracesIterator - " + _representationName);
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Callable<Pair<String, FunctionalityInfo>>> tasks = new ArrayList<>();

        Iterator<String> functionalities = getFunctionalitiesNames();
        for(Iterator<String> it = functionalities; it.hasNext(); ) {
            String functionality = it.next();

            if (!_graphInfo.containsKey(innerFunctionalityName(functionality))) {
                tasks.add(new TraceGraphCreationThread(this, functionality));
            } else {
            }
        }

        // await threads finish
        try {
            List<Future<Pair<String, FunctionalityInfo>>> futures = executor.invokeAll(tasks);
            for (Future<Pair<String, FunctionalityInfo>> future : futures) {
                addFunctionalityInfo(future.get().getFirst(), future.get().getSecond());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private String innerFunctionalityName(String functionalityName) {
        return _representationName + "_" + functionalityName;
    }

    public Iterator<String> getFunctionalitiesNames() {
        return _codebaseAsJSON.keys();
    }

    public void getFunctionalityWithName(String functionalityName) throws JSONException {
        requestedFunctionality = functionalityName;
    }

    /* Get Trace Types */

    public TraceDto getLongestTrace() throws JSONException {
        return _graphInfo.get(innerFunctionalityName(requestedFunctionality)).getLongestPath();
    }

    public static TraceDto getLongestTrace(Graph<AccessDto, DefaultWeightedEdge> graph, String functionalityName) throws JSONException {
        
        Map<AccessDto, Integer> vertexToDepthMap = new HashMap<>();

        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(graph);

        // calculate all possible path lengths
        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            AccessDto vertex = it.next();

            List<AccessDto> predecessors = Graphs.predecessorListOf(graph, vertex);
            Integer maxPredecessorDepth = 0;

            for (AccessDto predecessor : predecessors) {
                maxPredecessorDepth = Math.max(maxPredecessorDepth, vertexToDepthMap.get(predecessor));
            }

            vertexToDepthMap.put(vertex, predecessors.size() > 0? maxPredecessorDepth + 1: maxPredecessorDepth);
        }
        
        // get ending of longest path
        AccessDto maxPathEnd = null;

        for (AccessDto vertex : vertexToDepthMap.keySet()) {
            if (maxPathEnd == null || vertexToDepthMap.get(vertex) > vertexToDepthMap.get(maxPathEnd)) {
                maxPathEnd = vertex;
            }
        }

        if (maxPathEnd == null) {
            throw new JSONException(functionalityName + ": no max path");
        }

        // get longest path
        List<ReducedTraceElementDto> path = new ArrayList<>();
        path.add(maxPathEnd);

        List<AccessDto> predecessors = Graphs.predecessorListOf(graph, maxPathEnd);
        
        while (!predecessors.isEmpty()) {
            AccessDto maxPredecessor = null;
            for (AccessDto predecessor : predecessors) {
                if (maxPredecessor == null || vertexToDepthMap.get(predecessor) > vertexToDepthMap.get(maxPredecessor)) {
                    maxPredecessor = predecessor;
                }
            }

            path.add(maxPredecessor);
            predecessors = Graphs.predecessorListOf(graph, maxPredecessor);
        }

        Collections.reverse(path);

        // register probability of each node
        float carriedProbability = 1.0f; // used to carry probability from null nodes
        List<AccessDto> emptyNodes = new ArrayList<>();
        AccessDto previous = null;
        for (ReducedTraceElementDto el : path) {
            AccessDto access = (AccessDto)el;

            if (previous != null) {
                carriedProbability *= graph.getEdgeWeight(graph.getEdge(previous, access));
            }

            if (access.getEntityID() == -1) {
                emptyNodes.add(access);
            } else {
                access.setProbability(carriedProbability);
                carriedProbability = 1.0f;
            }

            previous = access;
        }

        path.removeAll(emptyNodes);

        return new TraceDto(0, 0, path);
    }

    public TraceDto getTraceWithMoreDifferentAccesses() throws JSONException {
        return _graphInfo.get(innerFunctionalityName(requestedFunctionality)).getMostDifferentAccessesPath();
    }

    public static TraceDto getTraceWithMoreDifferentAccesses(Graph<AccessDto, DefaultWeightedEdge> graph, String functionalityName) throws JSONException {
        
        Map<AccessDto, Integer> vertexToDepthMap = new HashMap<>();
        Map<AccessDto, List<Short>> vertexToDiffAccessesMap = new HashMap<>();

        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(graph);

        // calculate all possible path lengths
        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            AccessDto vertex = it.next();

            List<AccessDto> predecessors = Graphs.predecessorListOf(graph, vertex);
            Integer maxPredecessorDepth = 0;
            AccessDto maxPredecessor = null;

            for (AccessDto predecessor : predecessors) {
                List<Short> diffAccessesMap = vertexToDiffAccessesMap.get(predecessor);
                if (!diffAccessesMap.contains(vertex.getEntityID()))
                    diffAccessesMap.add(vertex.getEntityID());

                if (maxPredecessor == null || vertexToDiffAccessesMap.get(maxPredecessor).size() != Math.max(vertexToDiffAccessesMap.get(maxPredecessor).size(), diffAccessesMap.size())) {
                    maxPredecessorDepth = Math.max(maxPredecessorDepth, vertexToDepthMap.get(predecessor));
                    maxPredecessor = predecessor;
                }
            }

            List<Short> vertexDiffAccesses = new ArrayList<>();

            if (predecessors.size() > 0) {
                vertexDiffAccesses.addAll(vertexToDiffAccessesMap.get(maxPredecessor));
            }

            if (!vertexDiffAccesses.contains(vertex.getEntityID()) && vertex.getEntityID() > -1)
                vertexDiffAccesses.add(vertex.getEntityID());

            vertexToDiffAccessesMap.put(vertex, vertexDiffAccesses);
            vertexToDepthMap.put(vertex, predecessors.size() > 0? maxPredecessorDepth + 1: maxPredecessorDepth);
        }
        
        // get ending of path
        AccessDto pathEnd = null;

        List<AccessDto> endingVertexes = vertexToDiffAccessesMap.keySet().stream().filter(v -> Graphs.successorListOf(graph, v).size() == 0).collect(Collectors.toList());

        // end of path is the most probable final node
        for (AccessDto vertex : endingVertexes) {
            if (pathEnd == null || vertexToDiffAccessesMap.get(vertex).size() > vertexToDiffAccessesMap.get(pathEnd).size()
                ||
                vertexToDiffAccessesMap.get(vertex).size() == vertexToDiffAccessesMap.get(pathEnd).size() && vertexToDepthMap.get(vertex) > vertexToDepthMap.get(pathEnd)
                ){
                pathEnd = vertex;
            }
        }

        if (pathEnd == null) {
            throw new JSONException(functionalityName + ": no max path");
        }

        // get longest path
        List<ReducedTraceElementDto> path = new ArrayList<>();
        path.add(pathEnd);

        List<Short> pathEntities = new ArrayList<>();

        List<AccessDto> predecessors = Graphs.predecessorListOf(graph, pathEnd);

        while (!predecessors.isEmpty()) {
            AccessDto maxPredecessor = null;
            for (AccessDto predecessor : predecessors) {
                List<Short> maxPredecessorAccList = vertexToDiffAccessesMap.get(maxPredecessor);
                List<Short> predecessorAccList = vertexToDiffAccessesMap.get(predecessor);

                if (maxPredecessor != null) maxPredecessorAccList.removeAll(pathEntities);
                predecessorAccList.removeAll(pathEntities);

                if (maxPredecessor == null || predecessorAccList.size() > maxPredecessorAccList.size()) {
                    maxPredecessor = predecessor;
                }
            }

            path.add(maxPredecessor);
            predecessors = Graphs.predecessorListOf(graph, maxPredecessor);

            pathEntities.add(maxPredecessor.getEntityID());
        }

        Collections.reverse(path);

        // register probability of each node
        float carriedProbability = 1.0f; // used to carry probability from null nodes
        List<AccessDto> emptyNodes = new ArrayList<>();
        AccessDto previous = null;
        for (ReducedTraceElementDto el : path) {
            AccessDto access = (AccessDto)el;

            if (previous != null) {
                carriedProbability *= graph.getEdgeWeight(graph.getEdge(previous, access));
            }

            if (access.getEntityID() == -1) {
                emptyNodes.add(access);
            } else {
                access.setProbability(carriedProbability);
                carriedProbability = 1.0f;
            }

            previous = access;
        }

        path.removeAll(emptyNodes);

        return new TraceDto(0, 0, path);
    }

    public TraceDto getMostProbableTrace() throws JSONException {
        return _graphInfo.get(innerFunctionalityName(requestedFunctionality)).getMostProbablePath();
    }

    public static TraceDto getMostProbableTrace(Graph<AccessDto, DefaultWeightedEdge> graph, String functionalityName) throws JSONException {
        
        Map<AccessDto, Integer> vertexToDepthMap = new HashMap<>();
        Map<AccessDto, Double> vertexToProbMap = new HashMap<>();

        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(graph);

        // calculate all possible path lengths
        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            AccessDto vertex = it.next();

            List<AccessDto> predecessors = Graphs.predecessorListOf(graph, vertex);
            Integer maxPredecessorDepth = 0;
            Double maxPredecessorProb = 0d;
            AccessDto maxPredecessor = null;

            for (AccessDto predecessor : predecessors) {
                if (maxPredecessorProb != Math.max(maxPredecessorProb, vertexToProbMap.get(predecessor))) {
                    maxPredecessorProb = Math.max(maxPredecessorProb, vertexToProbMap.get(predecessor));
                    maxPredecessorDepth = Math.max(maxPredecessorDepth, vertexToDepthMap.get(predecessor));
                    maxPredecessor = predecessor;
                }
            }

            vertexToProbMap.put(vertex, predecessors.size() > 0? maxPredecessorProb * graph.getEdgeWeight(graph.getEdge(maxPredecessor, vertex)): 1f);
            vertexToDepthMap.put(vertex, predecessors.size() > 0? maxPredecessorDepth + 1: maxPredecessorDepth);
        }
        
        // get ending of path
        AccessDto pathEnd = null;

        List<AccessDto> endingVertexes = vertexToProbMap.keySet().stream().filter(v -> Graphs.successorListOf(graph, v).size() == 0).collect(Collectors.toList());

        // end of path is the most probable final node
        for (AccessDto vertex : endingVertexes) {
            if (pathEnd == null || vertexToProbMap.get(vertex) > vertexToProbMap.get(pathEnd)
                ||
                vertexToProbMap.get(vertex) == vertexToProbMap.get(pathEnd) && vertexToDepthMap.get(vertex) > vertexToDepthMap.get(pathEnd)
                ){
                pathEnd = vertex;
            }
        }

        if (pathEnd == null) {
            throw new JSONException(functionalityName + ": no max path");
        }

        // get longest path
        List<ReducedTraceElementDto> path = new ArrayList<>();
        path.add(pathEnd);

        List<AccessDto> predecessors = Graphs.predecessorListOf(graph, pathEnd);

        while (!predecessors.isEmpty()) {
            AccessDto maxPredecessor = null;
            for (AccessDto predecessor : predecessors) {
                if (maxPredecessor == null || vertexToProbMap.get(predecessor) > vertexToProbMap.get(maxPredecessor)) {
                    maxPredecessor = predecessor;
                }
            }

            path.add(maxPredecessor);
            predecessors = Graphs.predecessorListOf(graph, maxPredecessor);
        }

        Collections.reverse(path);

        // register probability of each node
        float carriedProbability = 1.0f; // used to carry probability from null nodes
        List<AccessDto> emptyNodes = new ArrayList<>();
        AccessDto previous = null;
        for (ReducedTraceElementDto el : path) {
            AccessDto access = (AccessDto)el;

            if (previous != null) {
                carriedProbability *= graph.getEdgeWeight(graph.getEdge(previous, access));
            }

            if (access.getEntityID() == -1) {
                emptyNodes.add(access);
            } else {
                access.setProbability(carriedProbability);
                carriedProbability = 1.0f;
            }

            previous = access;
        }

        path.removeAll(emptyNodes);

        return new TraceDto(0, 0, path);
    }

    public List<TraceDto> getAllTraces() throws JSONException {
        return new ArrayList<>();
    }

    public List<TraceDto> getTracesByType(Constants.TraceType traceType) throws JSONException {
        List<TraceDto> traceDtos = new ArrayList<>();

        System.out.println("Get Trace by Type: " + traceType);
        // Get traces according to trace type
        switch(traceType) {
            case LONGEST:
                traceDtos.add(this.getLongestTrace());
                break;
            case MOST_PROBABLE:
                traceDtos.add(this.getMostProbableTrace());
                break;
            case WITH_MORE_DIFFERENT_ACCESSES:
                traceDtos.add(this.getTraceWithMoreDifferentAccesses());
                break;
            default:
                //traceDtos.addAll(this.getAllTraces());
                //traceDtos.add(this.getLongestTrace());
                traceDtos.add(new TraceDto());
        }
        if (traceDtos.size() == 0)
            throw new RuntimeException("Functionality does not contain any trace.");

        return traceDtos;
    }

    /* Process Access Graph */

    public void addFunctionalityInfo(String functionalityName, FunctionalityInfo info) {
        _graphInfo.put(innerFunctionalityName(functionalityName), info);
    }

    public TraceGraph getFunctionalityTraceGraph(String functionalityName) throws JSONException {
        return getFunctionalityTraceGraph(_codebaseAsJSON.getJSONObject(functionalityName));
    }

    public static TraceGraph getFunctionalityTraceGraph(JSONObject object) throws JSONException {
        JSONObject mainTrace = object.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = translateSubTrace(object, mainTrace);

        TraceGraph processedSubTrace = processSubTrace(preProcessedTraces);

        return processedSubTrace;
    }

    public static List<TraceGraphNode> translateSubTrace(JSONObject totalTrace, JSONObject subTrace) throws JSONException {
        JSONArray totalTraceArray = totalTrace.getJSONArray("t");
        JSONArray subTraceArray = subTrace.getJSONArray("a");
        Integer subTraceIndex = subTrace.getInt("id");

        List<TraceGraphNode> translatedTrace = new ArrayList<>(); 

        for (int i = 0; i < subTraceArray.length(); i++) {
            JSONArray traceElementJSON = subTraceArray.getJSONArray(i);
            Object elementType = traceElementJSON.get(0);

            String type = (String)elementType;
            if (elementType instanceof String) {
                switch (type.substring(0, 1)) {
                    case "&": // Is a TraceReferenceDto
                        String description = traceElementJSON.getString(0);
                        
                        if (description.contains("if")) {
                            translatedTrace.add(new If(totalTrace, totalTraceArray, traceElementJSON));
                        } else if (description.contains("loop")) {
                            translatedTrace.add(new Loop(totalTrace, totalTraceArray, traceElementJSON));
                        } else if (description.contains("call")) {
                            translatedTrace.add(new Call(totalTrace, totalTraceArray, traceElementJSON));
                        } else if (description.contains("ac")) {
                            translatedTrace.add(new AbstractCall(totalTrace, totalTraceArray, traceElementJSON));
                        } else if (description.contains("sw")) {
                            translatedTrace.add(new Switch(totalTrace, totalTraceArray, traceElementJSON));
                        }
                        
                        break;
                    case "#": // Is a TraceLabelDto
                        translatedTrace.add(new Label(traceElementJSON, subTraceIndex));

                        break;
                    default: // Is an AccessDto
                        translatedTrace.add(new Access(traceElementJSON, subTraceIndex));

                        break;
                }
            }
        }
        
        return translatedTrace;
    }

    public static JSONArray getRoleInSubTrace(String role, JSONArray traceArray) {
        for (int i = 0; i < traceArray.length(); i++) {
            try {
                if (traceArray.getJSONArray(i).getString(0).contains(role)) {
                    return traceArray.getJSONArray(i);
                }                
            } catch (JSONException e) {
                // continue
            }
        }
        return null;
    }

    public static List<JSONArray> getAllOfRoleInSubTrace(String role, JSONArray traceArray) {
        List<JSONArray> matches = new ArrayList<>();
        for (int i = 0; i < traceArray.length(); i++) {
            try {
                if (traceArray.getJSONArray(i).getString(0).contains(role)) {
                    matches.add(traceArray.getJSONArray(i));
                }                
            } catch (JSONException e) {
                // continue
            }
        }
        return matches;
    }

    public static TraceGraph processSubTrace(List<TraceGraphNode> subTrace) {
        return processSubTrace(subTrace, null, null, null, new HeuristicFlags());
    }

    public static TraceGraph processSubTrace(List<TraceGraphNode> subTrace, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        if (subTrace == null || subTrace.isEmpty()) return null;
        
        TraceGraph resultingGraph = new TraceGraph();

        for (int i = 0; i < subTrace.size(); i++) {
            subTrace.get(i).nodeToAccessGraph(resultingGraph, lastCallEnd, lastLoopStart, lastLoopEnd, heuristicFlags);
        }

        return resultingGraph;
    }

    /* Fill Similarity Measures Structures */

    public void fillEntityDataStructures(
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities
    ) {
        System.out.println("\n\ngraph fill data structures");
        System.out.println(requestedFunctionality);

        Map<String, Float> cachedE1e2PairCount = _graphInfo.get(innerFunctionalityName(requestedFunctionality)).getE1e2PairCount();
        for (String pairDesc : cachedE1e2PairCount.keySet()) {
            Float value = cachedE1e2PairCount.get(pairDesc);
            if (e1e2PairCount.containsKey(pairDesc)) {
                value += e1e2PairCount.get(pairDesc);
            }
            e1e2PairCount.put(pairDesc, value);
        }
        
        entityFunctionalities.putAll(_graphInfo.get(innerFunctionalityName(requestedFunctionality)).getEntityFunctionalities());
        
    }

    public static void fillEntityDataStructures(
            Graph<AccessDto, DefaultWeightedEdge> graph,
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities,
            String functionalityName
    ) {

        Map<AccessDto, Double> vertexProbabilitiesMap = new HashMap<>();
        Map<AccessDto, List<AccessDto>> vertexLastValidVertexesMap = new HashMap<>();

        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(graph);

        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            AccessDto vertex = it.next();
            Short entityID = vertex.getEntityID();
            byte mode = vertex.getMode();

            List<AccessDto> predecessors = Graphs.predecessorListOf(graph, vertex);
            Double addedPredecessorProbability = 0d;

            List<AccessDto> allPredecessorValidVertexes = new ArrayList<>();

            for (AccessDto predecessor : predecessors) {
                Double edgeWeight = graph.getEdgeWeight(graph.getEdge(predecessor, vertex));
                Double predecessorPreviousProbability = vertexProbabilitiesMap.get(predecessor);

                addedPredecessorProbability += predecessorPreviousProbability * edgeWeight;

                if (addedPredecessorProbability-1 >= 0.1) {
                    DOTExporter<AccessDto, DefaultWeightedEdge> exporter = new DOTExporter<>();
                    exporter.setVertexAttributeProvider((v) -> {
                        Map<String, Attribute> map = new LinkedHashMap<>();
                        map.put("label", DefaultAttribute.createAttribute(v.toString()));
                        return map;
                    });
                    //Writer writer = new StringWriter();
                    //exporter.exportGraph(graph, writer);
                    exporter.exportGraph(graph, new File("/" + functionalityName + ".dot"));
                    //System.out.println("Print Graph");
                    //System.out.println(writer.toString());

                    throw new RuntimeException("Invalid vertex probability (" + functionalityName + ", id=" + vertex.getId() + ", prob=" + addedPredecessorProbability + ").");
                }

                addedPredecessorProbability = Math.min(addedPredecessorProbability, 1);

                List<AccessDto> predecessorValidVertexes = vertexLastValidVertexesMap.get(predecessor);
                if (entityID != -1) {
                    // connect to pair
                    for (AccessDto validVertex : predecessorValidVertexes) {
                        Short validVertexEntityID = validVertex.getEntityID();
                        if (entityID != validVertexEntityID) {
                            String e1e2 = validVertexEntityID + "->" + entityID;
                            String e2e1 = entityID + "->" + validVertexEntityID;
        
                            float count = e1e2PairCount.getOrDefault(e1e2, 0f);
                            e1e2PairCount.put(e1e2, count + predecessorPreviousProbability.floatValue() * edgeWeight.floatValue());
        
                            count = e1e2PairCount.getOrDefault(e2e1, 0f);
                            e1e2PairCount.put(e2e1, count + predecessorPreviousProbability.floatValue() * edgeWeight.floatValue());
                        }
                        
                    }

                }

                allPredecessorValidVertexes.addAll(predecessorValidVertexes);
            }

            // collect entity functionalities
            if (entityID != -1 && entityFunctionalities.containsKey(entityID)) {
                Pair<String, Byte> thisFunctionalityPair = null;
                float currentAccessingEntityProb = -1;

                for (Pair<String, Byte> functionalityPair : entityFunctionalities.get(entityID).keySet()) {
                    if (functionalityPair.getFirst().equals(functionalityName)) {
                        thisFunctionalityPair = functionalityPair;
                        currentAccessingEntityProb = entityFunctionalities.get(entityID).get(functionalityPair);

                        if (functionalityPair.getSecond() != 3 && functionalityPair.getSecond() != mode)
                            functionalityPair.setSecond((byte) 3); // "RW" -> 3

                        break;
                    }
                }

                if (thisFunctionalityPair == null || currentAccessingEntityProb == -1 || addedPredecessorProbability > currentAccessingEntityProb) {
                    if (thisFunctionalityPair == null) {
                        thisFunctionalityPair = new Pair<>(functionalityName, mode);
                    }
    
                    entityFunctionalities.get(entityID).put(thisFunctionalityPair, addedPredecessorProbability.floatValue());
                }

            } else if (entityID != -1) {
                Map<Pair<String, Byte>, Float> functionalitiesPairMap = new HashMap<>();
                functionalitiesPairMap.put(new Pair<>(functionalityName, mode), addedPredecessorProbability.floatValue());

                entityFunctionalities.put(entityID, functionalitiesPairMap);
            }

            vertexProbabilitiesMap.put(vertex, predecessors.size() > 0? addedPredecessorProbability: 1f);
            vertexLastValidVertexesMap.put(vertex, entityID == -1? allPredecessorValidVertexes: Arrays.asList(vertex));
        }

    }

    /* Utils */

    public static byte accessModeStringToByte(String mode) {
        return (byte) (mode.equals("R") ? 1 : 2);
    }
    
}