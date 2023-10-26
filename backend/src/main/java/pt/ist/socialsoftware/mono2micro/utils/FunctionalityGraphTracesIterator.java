package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Call;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.HeuristicFlags;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.If;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Label;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Loop;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraph;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraphNode;

/**
 * FunctionalityGraphTracesIterator
 */
public class FunctionalityGraphTracesIterator extends TracesIterator {
    private final String _representationName;
    private final JSONObject _codebaseAsJSON;
    private static ConcurrentMap<String, FunctionalityInfo> _graphInfo = new ConcurrentHashMap<>(); //TODO: transform into map of processed graph info
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
                System.out.println("Launching creation thread: " + innerFunctionalityName(functionality));
                tasks.add(new TraceGraphCreationThread(this, functionality));
            } else {
                System.out.println("Loading from cache: " + innerFunctionalityName(functionality));
            }
        }

        // await threads finish
        try {
            List<Future<Pair<String, FunctionalityInfo>>> futures = executor.invokeAll(tasks);
            for (Future<Pair<String, FunctionalityInfo>> future : futures) {
                addFunctionalityInfo(future.get().getFirst(), future.get().getSecond());
                System.out.println("Thread finished " + future.get().getSecond());
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

        System.out.println("ran all nodes");
        
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
        
        System.out.println("getting longest path");
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

        System.out.println("finished max path, inverting");

        Collections.reverse(path);

        System.out.println("applying probability");

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

        System.out.println("returning");

        return new TraceDto(0, 0, path);
    }

    public TraceDto getTraceWithMoreDifferentAccesses() throws JSONException {
        return null;
    }

    public TraceDto getMostProbableTrace() throws JSONException {
        return null;
    }

    public List<TraceDto> getAllTraces() throws JSONException {
        return null;
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
                traceDtos.addAll(this.getAllTraces());
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


        
    }

    public static void fillEntityDataStructures(
            Access access,
            List<Access> currentPath,
            Float runningProbability,
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities,
            String functionalityName
    ) {

        short entityID = (short)access.getEntityAccessedId();
        
        
        // System.out.println(functionalityName + " (" + countFill + ") -> " + entityID + ", " + access.getMode());
        //System.out.println("[" + currentPath.size() + "]" + functionalityName + " (" + access.getContextIndex() + ") -> " + entityID + ", " + access.getMode());
        /* int currentCountForAccess = 0;
        if (accessCounter.containsKey(access.getContextIndex())) {
            currentCountForAccess = accessCounter.get(access.getContextIndex());
        }
        currentCountForAccess++;
        accessCounter.put(access.getContextIndex(), currentCountForAccess); */
        /* List<Integer> countedThisTime = new ArrayList<>();
        for (Access a : currentPath) {
            if (countedThisTime.contains(a.getContextIndex())) continue;

            int currentCountForAccess = 0;
            if (accessCounter.containsKey(a.getContextIndex())) {
                currentCountForAccess = accessCounter.get(a.getContextIndex());
            }
            currentCountForAccess++;
            accessCounter.put(a.getContextIndex(), currentCountForAccess); 
            countedThisTime.add(a.getContextIndex());
        }*/
        /* totalPaths++;
        totalPathSizes += currentPath.size();
        
        accessCounter.put(access.getContextIndex(), totalPaths);
        
        for (Integer context : accessCounter.keySet()) {
            System.out.println(context + ": " + accessCounter.get(context));
        }

        System.out.println("average path size: " + totalPathSizes/totalPaths);
        if (currentPath.size() > 170) {
            String section = "section: ";
            
            for (int i = 150; i < 170; i++) {
                section += currentPath.get(i).getContextIndex() + "->";
            }
            System.out.println(section);
        }
        System.out.println();
        System.out.println("---------------");*/

        // fill entity functionalities
        if (access.getMode() != null) {
            byte mode = accessModeStringToByte(access.getMode());

            if (entityFunctionalities.containsKey(entityID)) {
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

                if (thisFunctionalityPair == null || currentAccessingEntityProb == -1 || runningProbability > currentAccessingEntityProb) {
                    if (thisFunctionalityPair == null) {
                        thisFunctionalityPair = new Pair<>(functionalityName, mode);
                    }
    
                    entityFunctionalities.get(entityID).put(thisFunctionalityPair, runningProbability);
                }

            } else {
                Map<Pair<String, Byte>, Float> functionalitiesPairMap = new HashMap<>();
                functionalitiesPairMap.put(new Pair<>(functionalityName, mode), runningProbability);

                entityFunctionalities.put(entityID, functionalitiesPairMap);
            }
        }

        boolean hasBeenTraversed = currentPath.contains(access);

        // fill e1e2 pair count
        for (TraceGraphNode nextNode : access.getNextAccessProbabilities().keySet()) {
            // if the current access has been traversed, only continue to non-traversed successors
            if (hasBeenTraversed && currentPath.contains(nextNode)) continue;

            List<Access> newCurrentPath = new ArrayList<>(currentPath);
            newCurrentPath.add(access);

            Access nextAccess = (Access)nextNode;

            short nextEntityID = (short)nextAccess.getEntityAccessedId();

            if (access.getMode() != null && nextAccess.getMode() != null && entityID != nextEntityID) {
                connectAccesses(e1e2PairCount, entityID, nextEntityID, runningProbability * access.getNextAccessProbabilities().get(nextNode));
                
            } else if (access.getMode() == null && nextAccess.getMode() != null) {
                List<Access> invertedCurrentPath = new ArrayList<>(currentPath);
                Collections.reverse(invertedCurrentPath);
                Optional<Access> lastValidAccess = invertedCurrentPath.stream().filter(a -> a.getMode() != null).findFirst();
                
                if (lastValidAccess.isPresent()) {
                    connectAccesses(e1e2PairCount, (short)lastValidAccess.get().getEntityAccessedId(), nextEntityID, runningProbability * access.getNextAccessProbabilities().get(nextNode));
                }
            }
            fillEntityDataStructures(nextAccess, newCurrentPath, runningProbability*access.getNextAccessProbabilities().get(nextNode), e1e2PairCount, entityFunctionalities, functionalityName);
        }
        // System.out.println("closed node (" + access.getContextIndex() + ")" + ", parent: " + currentPath.get(currentPath.size()-1).getContextIndex());

    }

    private static void connectAccesses(Map<String, Float> e1e2PairCount, short entityID, short nextEntityID, float probability) {
        String e1e2 = entityID + "->" + nextEntityID;
        String e2e1 = nextEntityID + "->" + entityID;

        float count = e1e2PairCount.getOrDefault(e1e2, 0f);
        e1e2PairCount.put(e1e2, count + probability);

        count = e1e2PairCount.getOrDefault(e2e1, 0f);
        e1e2PairCount.put(e2e1, count + probability);
    }

    /* Utils */

    public static TraceDto pathDataAccessListToTraceDto(List<PathDataAccess> accessList) {
        List<ReducedTraceElementDto> traceElementList = new ArrayList<>();

        AccessDto accessDto;
        Access access;
        float carriedProbability = 1.0f; // used to carry probability from null nodes
        for (PathDataAccess pathDataAccess : accessList) {
            access = pathDataAccess.getAccess();
            carriedProbability *= pathDataAccess.getProbability();
            if (access.getMode() == null) continue; // skip the first node

            accessDto = accessToAccessDto(access, carriedProbability);

            traceElementList.add(accessDto);
            carriedProbability = 1.0f; // reset on valid nodes
        }

        return new TraceDto(0, 0, traceElementList);
    }

    public static AccessDto accessToAccessDto(Access access, float probability) {
        AccessDto accessDto = new AccessDto();
        accessDto.setEntityID((short)access.getEntityAccessedId());
        accessDto.setMode(accessModeStringToByte(access.getMode()));
        accessDto.setOccurrences(1);
        accessDto.setProbability(probability);
        return accessDto;
    }

    public static byte accessModeStringToByte(String mode) {
        return (byte) (mode.equals("R") ? 1 : 2);
    }
    
}