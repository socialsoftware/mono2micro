package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
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
    private final JSONObject _codebaseAsJSON;
    private Map<String, TraceGraph> _traceGraphs;
    private String requestedFunctionality;

    private Map<Access, PathData> _pathDataCache;
    private Map<String, PathData> _functionalityPathData;

    private Map<Access, EntityAccessData> _entityAccessDataCache;

    public FunctionalityGraphTracesIterator(InputStream file) throws IOException, JSONException {
        _codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();

        // create graph representation
        _traceGraphs = new HashMap<String, TraceGraph>();

        Iterator<String> functionalities = getFunctionalitiesNames();
        while (functionalities.hasNext()) {
            String functionality = functionalities.next();

            _traceGraphs.put(functionality, getFunctionalityTraceGraph(_codebaseAsJSON.getJSONObject(functionality)));
        }

        _pathDataCache = new HashMap<>();
        _functionalityPathData = new HashMap<>();
    }

    public Iterator<String> getFunctionalitiesNames() {
        return _codebaseAsJSON.keys();
    }

    public void getFunctionalityWithName(String functionalityName) throws JSONException {
        requestedFunctionality = functionalityName;
    }

    /* Get Trace Types */

    public PathData computeTraceTypes() {
        PathData pathData = computeTraceTypes(_traceGraphs.get(requestedFunctionality).getFirstAccess(), _pathDataCache, new ArrayList<>());
        _pathDataCache.clear();
        return pathData;
    }

    public static PathData computeTraceTypes(Access access, Map<Access, PathData> pathDataCache, List<Access> currentPath) {
        PathData resultingPathData;
       
        boolean hasBeenTraversed = currentPath.contains(access);
        
        boolean decisionNode = access.getNextAccessProbabilities().size() > 1 || (currentPath.size() > 0 && currentPath.get(currentPath.size()-1).getNextAccessProbabilities().containsKey(access));
        
        if (decisionNode) {
            currentPath.add(access);
        }

        List<PathData> successorsPathData = new ArrayList<>();
        PathData succPathData;
        for (TraceGraphNode successor: access.getNextAccessProbabilities().keySet()) {
            // if the current access has been traversed, only continue to non-traversed successors
            if (hasBeenTraversed && currentPath.contains(successor)) continue;
            
            if (pathDataCache.containsKey(successor)) {
                succPathData = pathDataCache.get(successor);
            } else {
                succPathData = computeTraceTypes((Access)successor, pathDataCache, new ArrayList<>(currentPath));
            }

            float succProbability = access.getNextAccessProbabilities().get(successor);
            // update prob
            succPathData.setMostProbablePath(new ArrayList<>(succPathData.getMostProbablePath()));
            //succPathData.getMostProbablePath().set(0, new PathDataAccess(succPathData.getMostProbablePath().get(0).getAccess(), succProbability));
            if (decisionNode)
                succPathData.getMostProbablePath().add(0, new PathDataAccess(access, 1f));
            succPathData.setMostProbablePathProbability(succPathData.getMostProbablePathProbability() * succProbability);

            // add current access to list of diff accesses
            succPathData.setMostDifferentAccesses(new ArrayList<>(succPathData.getMostDifferentAccesses()));
            succPathData.setMostDifferentAccessesPath(new ArrayList<>(succPathData.getMostDifferentAccessesPath()));
            boolean alreadyExists = false;
            for (Access a: succPathData.getMostDifferentAccesses().stream().map(a -> a.getAccess()).collect(Collectors.toList())) {
                if (a.getMode() == access.getMode() && a.getEntityAccessedId() == access.getEntityAccessedId()) {
                    alreadyExists = true;
                    break;
                }
            }
            
            //succPathData.getMostDifferentAccessesPath().set(0, new PathDataAccess(succPathData.getMostDifferentAccessesPath().get(0).getAccess(), succProbability));
            if (decisionNode)
                succPathData.getMostDifferentAccessesPath().add(0, new PathDataAccess(access, 1.0f));
            if (!alreadyExists) {
                succPathData.getMostDifferentAccesses().add(new PathDataAccess(access));
            }
            
            // add current access to trace
            succPathData.setLongestPath(new ArrayList<>(succPathData.getLongestPath()));
            //succPathData.getLongestPath().set(0, new PathDataAccess(succPathData.getLongestPath().get(0).getAccess(), succProbability));
            if (decisionNode)
                succPathData.getLongestPath().add(0, new PathDataAccess(access, 1.0f));
            succPathData.setLongestPathSize(succPathData.getLongestPathSize()+1);

            successorsPathData.add(succPathData);
        }

        if (successorsPathData.size() != 0) {
            // calculate different accesses in currentPath
            List<Access> currentPathDifferentAccesses = new ArrayList<>();
            for (Access a : currentPath) {
                if (!currentPathDifferentAccesses.contains(a)) {
                    currentPathDifferentAccesses.add(a);
                }
            }

            Float highestProb = 0f;
            int highestProbIndex = 0;
            int biggerDiffAccessListAggregateSize = 0;
            int biggerDiffAccessListSizeIndex = 0;
            int highestLength = 0;
            int highestLengthIndex = 0;
            
            boolean firstLoop = true;
            for (PathData pathData : successorsPathData) {
                if (firstLoop || pathData.getMostProbablePathProbability() > highestProb) {
                    highestProb = pathData.getMostProbablePathProbability();
                    highestProbIndex = successorsPathData.indexOf(pathData);
                }

                List<Access> mostDifferentAccessesAggregate = new ArrayList<>(currentPathDifferentAccesses);
                for (Access a : pathData.getMostDifferentAccesses().stream().map(a -> a.getAccess()).collect(Collectors.toList())) {
                    if (!mostDifferentAccessesAggregate.contains(a)) {
                        mostDifferentAccessesAggregate.add(a);
                    }
                }
                
                if (firstLoop || mostDifferentAccessesAggregate.size() > biggerDiffAccessListAggregateSize) {
                    biggerDiffAccessListAggregateSize = mostDifferentAccessesAggregate.size();
                    biggerDiffAccessListSizeIndex = successorsPathData.indexOf(pathData);
                }

                if (firstLoop || pathData.getLongestPathSize() > highestLength) {
                    highestLength = pathData.getLongestPathSize();
                    highestLengthIndex = successorsPathData.indexOf(pathData);
                }
                
                if (firstLoop) firstLoop = false;
            }
            

            resultingPathData = new PathData(   new ArrayList<>(successorsPathData.get(highestLengthIndex).getLongestPath()),
                                                successorsPathData.get(highestLengthIndex).getLongestPathSize(),
                                                new ArrayList<>(successorsPathData.get(highestProbIndex).getMostProbablePath()),
                                                successorsPathData.get(highestProbIndex).getMostProbablePathProbability(),
                                                new ArrayList<>(successorsPathData.get(biggerDiffAccessListSizeIndex).getMostDifferentAccessesPath()),
                                                new ArrayList<>(successorsPathData.get(biggerDiffAccessListSizeIndex).getMostDifferentAccesses())
                                            );

            if (access.getPrevAccessProbabilities().size() > 1 && !pathDataCache.containsKey(access)) {
                pathDataCache.put(access, new PathData(         resultingPathData.getLongestPath(),
                                                                resultingPathData.getLongestPathSize(),
                                                                resultingPathData.getMostProbablePath(),
                                                                resultingPathData.getMostProbablePathProbability(),
                                                                resultingPathData.getMostDifferentAccessesPath(),
                                                                resultingPathData.getMostDifferentAccesses()
                                                            ));
            }

        } else {
            List<Access> accessList = new ArrayList<>();
            accessList.add(access);
            resultingPathData = new PathData((new ArrayList<>(accessList)), 1f, new ArrayList<>(accessList), new ArrayList<>(accessList), new ArrayList<>(accessList), 1);
        }

        return resultingPathData;
    }

    public TraceDto getLongestTrace() throws JSONException {
        initializeFunctionalityPathData(requestedFunctionality);

        List<PathDataAccess> decisionPath = _functionalityPathData.get(requestedFunctionality).getLongestPath();
        

        return pathDataAccessListToTraceDto(getPathFromDecisionPath(decisionPath, _traceGraphs.get(requestedFunctionality).getFirstAccess()));
    }

    public static List<PathDataAccess> getPathFromDecisionPath(List<PathDataAccess> decisionPath, Access firstAccess) throws JSONException {
        List<PathDataAccess> resultingPath = new ArrayList<>();

        int decisionPathIndex = 0;

        Stack<Access> queue = new Stack<>();
        queue.push(firstAccess);

        Access currentAccess;
        float probability = 1f;
        while (!queue.empty()) {
            currentAccess = queue.pop();

            resultingPath.add(new PathDataAccess(currentAccess, probability));

            if (currentAccess == decisionPath.get(decisionPathIndex).getAccess()) {
                decisionPathIndex++;
                if (decisionPathIndex < decisionPath.size() && currentAccess.getNextAccessProbabilities().containsKey(decisionPath.get(decisionPathIndex).getAccess())) {
                    probability = currentAccess.getNextAccessProbabilities().get(decisionPath.get(decisionPathIndex).getAccess());
                    queue.push(decisionPath.get(decisionPathIndex).getAccess());
                    continue;
                }
            }

            if (currentAccess.getNextAccessProbabilities().size() > 1)
                throw new JSONException("something wrong with decision path");

            if (currentAccess.getNextAccessProbabilities().size() > 0) {
                probability = 1f;
                queue.push((Access)currentAccess.getNextAccessProbabilities().keySet().toArray()[0]);
            }
        }

        return resultingPath;
    }

    public TraceDto getTraceWithMoreDifferentAccesses() throws JSONException {
        initializeFunctionalityPathData(requestedFunctionality);

        //TODO: translate decision path to actual path

        return pathDataAccessListToTraceDto(_functionalityPathData.get(requestedFunctionality).getMostDifferentAccessesPath());
    }

    public TraceDto getMostProbableTrace() throws JSONException {
        initializeFunctionalityPathData(requestedFunctionality);

        //TODO: translate decision path to actual path

        return pathDataAccessListToTraceDto(_functionalityPathData.get(requestedFunctionality).getMostProbablePath());
    }

    public List<TraceDto> getAllTraces() throws JSONException {
        return null;
    }

    public void initializeFunctionalityPathData(String functionality) {
        if (!_functionalityPathData.containsKey(requestedFunctionality)) {
            _functionalityPathData.put(requestedFunctionality, computeTraceTypes());
        }
    }

    public List<TraceDto> getTracesByType(Constants.TraceType traceType) throws JSONException {
        List<TraceDto> traceDtos = new ArrayList<>();

        initializeFunctionalityPathData(requestedFunctionality);

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

    TraceGraph getFunctionalityTraceGraph(JSONObject object) throws JSONException {
        JSONObject mainTrace = object.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = translateSubTrace(object, mainTrace);

        TraceGraph processedSubTrace = processSubTrace(preProcessedTraces);

        //processedSubTrace.removeEmptyNodes();

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

    public static TraceGraph processSubTrace(List<TraceGraphNode> subTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags) {
        if (subTrace == null || subTrace.isEmpty()) return null;

        List<Access> processedSubTrace = new ArrayList<Access>();
        for (int i = 0; i < subTrace.size(); i++) {
            subTrace.get(i).nodeToAccessGraph(processedSubTrace, lastCallEnd, lastLoopStart, lastLoopEnd, heuristicFlags);
        }

        TraceGraph resultingGraph = new TraceGraph();
        resultingGraph.setAllAccesses(processedSubTrace);
        if (processedSubTrace.size() > 0) {
            resultingGraph.setFirstAccess(processedSubTrace.get(0));
        }

        return resultingGraph;
    }

    /* Fill Similarity Measures Structures */
    static float countFill;
    static int totalPaths;
    static Map<Integer, Integer> accessCounter;
    static int totalPathSizes;

    public void fillEntityDataStructures(
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities
    ) {
        System.out.println("\n\ngraph fill data structures");
        System.out.println(requestedFunctionality);
        countFill = 0;
        totalPaths = 0;
        totalPathSizes = 0;
        accessCounter = new HashMap<>();

        if (_entityAccessDataCache == null) {
            _entityAccessDataCache = new HashMap<>();
        } else {
            _entityAccessDataCache.clear();
        }

        EntityAccessData result = getEntityAccessData(requestedFunctionality, _traceGraphs.get(requestedFunctionality).getFirstAccess(), _entityAccessDataCache, new ArrayList<>());

        e1e2PairCount = result.getE1e2PairCount();
        
        for (Short entityID : result.getEntityFunctionalitiesAccesses().keySet()) {
            Map<Pair<String, Byte>, Float> entityAccesses = new HashMap<>();
            for (Pair<String, Byte> accessPair : result.getEntityFunctionalitiesAccesses().get(entityID)) {
                String pairName = entityID + "_" + accessPair.getFirst();
                entityAccesses.put(accessPair, Collections.max(result.getEntityFunctionalitiesProbabilities().get(pairName)));
            }
            entityFunctionalities.put(entityID, entityAccesses);
        }

        //fillEntityDataStructures(_traceGraphs.get(requestedFunctionality).getFirstAccess(), new ArrayList<>(), 1f, e1e2PairCount, entityFunctionalities, requestedFunctionality);
    }

    public static EntityAccessData getEntityAccessData(
            String functionalityName,
            Access access,
            Map<Access, EntityAccessData> entityAccessDataCache,
            List<Access> currentPath
    ) {
        // foreach next
        //  if next in buffer, load
        //  else, visit next
        //  multiply all data by next prob
        //  add data to current structure
        EntityAccessData currentNodeData = new EntityAccessData(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        Map<Access, Float> nextValidAccesses = new HashMap<>();

        EntityAccessData succData;
        for (TraceGraphNode successor : access.getNextAccessProbabilities().keySet()) {
            if (currentPath.contains(access) && currentPath.contains(successor)) continue;

            Float succProbability = access.getNextAccessProbabilities().get(successor);
            if (entityAccessDataCache.containsKey(successor)) {
                System.out.println("load from cache");
                succData = entityAccessDataCache.get(successor);
            } else {
                List<Access> newCurrentPath = new ArrayList<>(currentPath);
                newCurrentPath.add(0, access);
                succData = getEntityAccessData(functionalityName, (Access)successor, entityAccessDataCache, newCurrentPath);
            }

            // merge e1e2 pair data
            for (String pairKey : succData.getE1e2PairCount().keySet()) {
                float currentProb = 0;
                if (currentNodeData.getE1e2PairCount().containsKey(pairKey)) {
                    currentProb = currentNodeData.getE1e2PairCount().get(pairKey);
                }

                currentNodeData.getE1e2PairCount().put(pairKey, currentProb + succData.getE1e2PairCount().get(pairKey) * succProbability);
            }

            // merge next valid accesses
            for (Access validAccess : succData.getNextValidAccesses().keySet()) {
                nextValidAccesses.put(validAccess, succData.getNextValidAccesses().get(validAccess)*succProbability);
            }

            // merge entity access types
            for (Short entityID : succData.getEntityFunctionalitiesAccesses().keySet()) {
                if (currentNodeData.getEntityFunctionalitiesAccesses().containsKey(entityID)) {

                    for (Pair<String, Byte> functionalityPairSucc : succData.getEntityFunctionalitiesAccesses().get(entityID)) {
                        boolean containsFunctionality = false;
                        for (Pair<String, Byte> functionalityPairCurr : currentNodeData.getEntityFunctionalitiesAccesses().get(entityID)) {
                            if (functionalityPairCurr.getFirst().equals(functionalityName)) {
                                containsFunctionality = true;
        
                                if (functionalityPairCurr.getSecond() != 3 && functionalityPairCurr.getSecond() != functionalityPairSucc.getSecond())
                                    functionalityPairCurr.setSecond((byte) 3); // "RW" -> 3
        
                                break;
                            }
                        }
                        if (!containsFunctionality) {
                            currentNodeData.getEntityFunctionalitiesAccesses().get(entityID).add(new Pair<>(functionalityName, functionalityPairSucc.getSecond()));
                        }
                    }

                } else {
                    currentNodeData.getEntityFunctionalitiesAccesses().put(entityID, new ArrayList<>(succData.getEntityFunctionalitiesAccesses().get(entityID)));
                }
            }

            // merge entity access probability
            for (String entityFunctionalityPair : succData.getEntityFunctionalitiesProbabilities().keySet()) {
                List<Float> probabilities = new ArrayList<>(succData.getEntityFunctionalitiesProbabilities().get(entityFunctionalityPair));

                for (int i = 0; i < probabilities.size(); i++) {
                    probabilities.set(i, probabilities.get(i) * succProbability);
                }

                if (currentNodeData.getEntityFunctionalitiesProbabilities().containsKey(entityFunctionalityPair)) {
                    currentNodeData.getEntityFunctionalitiesProbabilities().get(entityFunctionalityPair).addAll(probabilities);
                } else {
                    currentNodeData.getEntityFunctionalitiesProbabilities().put(entityFunctionalityPair, probabilities);
                }
            }
        }

        // add current to entity functionalities
        Short entityID = (short)access.getEntityAccessedId();
        if (access.getMode() != null) {
            byte mode = accessModeStringToByte(access.getMode());
            if (currentNodeData.getEntityFunctionalitiesAccesses().containsKey(entityID)) {
                boolean containsFunctionality = false;
    
                for (Pair<String, Byte> functionalityPair : currentNodeData.getEntityFunctionalitiesAccesses().get(entityID)) {
                    if (functionalityPair.getFirst().equals(functionalityName)) {
                        containsFunctionality = true;
    
                        if (functionalityPair.getSecond() != 3 && functionalityPair.getSecond() != mode)
                            functionalityPair.setSecond((byte) 3); // "RW" -> 3
    
                        break;
                    }
                }
    
                if (!containsFunctionality) {
                    currentNodeData.getEntityFunctionalitiesAccesses().get(entityID).add(new Pair<>(functionalityName, mode));
                }
    
            } else {
                List<Pair<String, Byte>> functionalitiesPairs = new ArrayList<>();
                functionalitiesPairs.add(new Pair<>(functionalityName, mode));
    
                currentNodeData.getEntityFunctionalitiesAccesses().put(entityID, functionalitiesPairs);
            }

            String entityFuncPair = entityID.toString() + "_" + functionalityName;
            if (currentNodeData.getEntityFunctionalitiesProbabilities().containsKey(entityFuncPair)) {
                currentNodeData.getEntityFunctionalitiesProbabilities().get(entityFuncPair).add(1f);
            } else {
                currentNodeData.getEntityFunctionalitiesProbabilities().put(entityFuncPair, Arrays.asList(1f));
            }
    
            // if successors >0, add current to e1e2PairCount
            if (nextValidAccesses.size() > 0) {
                for (TraceGraphNode successor : nextValidAccesses.keySet()) {
                    Access nextAccess = (Access) successor;
                    int nextEntityID = nextAccess.getEntityAccessedId();

                    if (entityID != nextEntityID) {
                        String e1e2 = entityID + "->" + nextEntityID;
                        String e2e1 = nextEntityID + "->" + entityID;

                        float count = currentNodeData.getE1e2PairCount().getOrDefault(e1e2, 0f);
                        currentNodeData.getE1e2PairCount().put(e1e2, count + nextValidAccesses.get(successor));

                        count = currentNodeData.getE1e2PairCount().getOrDefault(e2e1, 0f);
                        currentNodeData.getE1e2PairCount().put(e2e1, count + nextValidAccesses.get(successor));

                    }
                }
            }

            currentNodeData.getNextValidAccesses().put(access, 1f);
        } else {
            currentNodeData.getNextValidAccesses().putAll(nextValidAccesses);
        }

        // if predecessors >1, save in buffer
        if (access.getPrevAccessProbabilities().size() > 1 && !entityAccessDataCache.containsKey(access)) {
            System.out.println("save to cache");
            entityAccessDataCache.put(access, currentNodeData);
        }

        return currentNodeData;
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