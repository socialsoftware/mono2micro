package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants.TraceType;
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
        if (access.getNextAccessProbabilities().size() == 0 || currentPath.contains(access)) {
            access.setVisited(true);

            List<Access> accessList = new ArrayList<>();
            accessList.add(access);
            resultingPathData = new PathData((new ArrayList<>(accessList)), 1f, new ArrayList<>(accessList), new ArrayList<>(accessList), new ArrayList<>(accessList));

        } else {
            access.setVisited(true);
            currentPath.add(access);

            List<PathData> successorsPathData = new ArrayList<>();
            PathData succPathData;
            for (TraceGraphNode successor: access.getNextAccessProbabilities().keySet()) {
                
                if (pathDataCache.containsKey(successor)) { //FIXME: doesn't allow loops to continue until end of trace
                    succPathData = pathDataCache.get(successor);
                } else {
                    succPathData = computeTraceTypes((Access)successor, pathDataCache, new ArrayList<>(currentPath));
                }

                float succProbability = access.getNextAccessProbabilities().get(successor);
                // update prob
                succPathData.setMostProbablePath(new ArrayList<>(succPathData.getMostProbablePath()));
                succPathData.getMostProbablePath().set(0, new PathDataAccess(succPathData.getMostProbablePath().get(0).getAccess(), succProbability));
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
                
                succPathData.getMostDifferentAccessesPath().set(0, new PathDataAccess(succPathData.getMostDifferentAccessesPath().get(0).getAccess(), succProbability));
                succPathData.getMostDifferentAccessesPath().add(0, new PathDataAccess(access, 1.0f));
                if (!alreadyExists) {
                    succPathData.getMostDifferentAccesses().add(new PathDataAccess(access));
                }
                
                // add current access to trace
                succPathData.setLongestPath(new ArrayList<>(succPathData.getLongestPath()));
                succPathData.getLongestPath().set(0, new PathDataAccess(succPathData.getLongestPath().get(0).getAccess(), succProbability));
                succPathData.getLongestPath().add(0, new PathDataAccess(access, 1.0f));

                successorsPathData.add(succPathData);
            }

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

                if (firstLoop || pathData.getLongestPath().size() > highestLength) {
                    highestLength = pathData.getLongestPath().size();
                    highestLengthIndex = successorsPathData.indexOf(pathData);
                }
                
                if (firstLoop) firstLoop = false;
            }
            

            resultingPathData = new PathData(   new ArrayList<>(successorsPathData.get(highestLengthIndex).getLongestPath()),
                                                new ArrayList<>(successorsPathData.get(highestProbIndex).getMostProbablePath()),
                                                successorsPathData.get(highestProbIndex).getMostProbablePathProbability(),
                                                new ArrayList<>(successorsPathData.get(biggerDiffAccessListSizeIndex).getMostDifferentAccessesPath()),
                                                new ArrayList<>(successorsPathData.get(biggerDiffAccessListSizeIndex).getMostDifferentAccesses())
                                            );

            if (access.getPrevAccessProbabilities().size() > 1) {
                pathDataCache.put(access, new PathData(         resultingPathData.getLongestPath(),
                                                                resultingPathData.getMostProbablePath(),
                                                                resultingPathData.getMostProbablePathProbability(),
                                                                resultingPathData.getMostDifferentAccessesPath(),
                                                                resultingPathData.getMostDifferentAccesses()
                                                            ));
            }

        }

        return resultingPathData;
    }

    public TraceDto getLongestTrace() throws JSONException {
        initializeFunctionalityPathData(requestedFunctionality);

        return pathDataAccessListToTraceDto(_functionalityPathData.get(requestedFunctionality).getLongestPath());
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

        processedSubTrace.removeEmptyNodes();

        return processedSubTrace;
    }

    public static List<TraceGraphNode> translateSubTrace(JSONObject totalTrace, JSONObject subTrace) throws JSONException {
        JSONArray totalTraceArray = totalTrace.getJSONArray("t");
        JSONArray subTraceArray = subTrace.getJSONArray("a");

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
                        translatedTrace.add(new Label(traceElementJSON));

                        break;
                    default: // Is an AccessDto
                        translatedTrace.add(new Access(traceElementJSON));

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

    public void fillEntityDataStructures(
            Map<String, Float> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities
    ) {
        fillEntityDataStructures(_traceGraphs.get(requestedFunctionality).getFirstAccess(), new ArrayList<>(), 1f, e1e2PairCount, entityFunctionalities, requestedFunctionality);
    }

    public static void fillEntityDataStructures(
            Access access,
            List<Access> currentPath,
            Float runningProbability,
            Map<String, Float> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities,
            String functionalityName
    ) {

        short entityID = (short)access.getEntityAccessedId();

        // fill entity functionalities
        if (access.getMode() != null) {
            byte mode = accessModeStringToByte(access.getMode());

            if (entityFunctionalities.containsKey(entityID)) {
                boolean containsFunctionality = false;

                for (Pair<String, Byte> functionalityPair : entityFunctionalities.get(entityID)) {
                    if (functionalityPair.getFirst().equals(functionalityName)) {
                        containsFunctionality = true;

                        if (functionalityPair.getSecond() != 3 && functionalityPair.getSecond() != mode)
                            functionalityPair.setSecond((byte) 3); // "RW" -> 3

                        break;
                    }
                }

                if (!containsFunctionality) {
                    entityFunctionalities.get(entityID).add(new Pair<>(functionalityName, mode));
                }

            } else {
                List<Pair<String, Byte>> functionalitiesPairs = new ArrayList<>();
                functionalitiesPairs.add(new Pair<>(functionalityName, mode));

                entityFunctionalities.put(entityID, functionalitiesPairs);
            }
        }

        // fill e1e2 pair count
        for (TraceGraphNode nextNode : access.getNextAccessProbabilities().keySet()) {
            List<Access> newCurrentPath = new ArrayList<>(currentPath);
            newCurrentPath.add(access);

            Access nextAccess = (Access)nextNode;

            short nextEntityID = (short)nextAccess.getEntityAccessedId();

            if (access.getMode() != null && nextAccess.getMode() != null && entityID != nextEntityID) {
                String e1e2 = entityID + "->" + nextEntityID;
                String e2e1 = nextEntityID + "->" + entityID;

                float count = e1e2PairCount.getOrDefault(e1e2, 0f);
                e1e2PairCount.put(e1e2, count + runningProbability * access.getNextAccessProbabilities().get(nextNode));

                count = e1e2PairCount.getOrDefault(e2e1, 0f);
                e1e2PairCount.put(e2e1, count + runningProbability * access.getNextAccessProbabilities().get(nextNode));
            }

            fillEntityDataStructures(nextAccess, newCurrentPath, runningProbability*access.getNextAccessProbabilities().get(nextNode), e1e2PairCount, entityFunctionalities, functionalityName);
        }

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