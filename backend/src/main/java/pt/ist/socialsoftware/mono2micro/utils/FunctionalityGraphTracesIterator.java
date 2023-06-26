package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class FunctionalityGraphTracesIterator {
    private final JSONObject _codebaseAsJSON;
    private Map<String, TraceGraph> _traceGraphs;
    private String requestedFunctionality;

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
    }

    public Iterator<String> getFunctionalitiesNames() {
        return _codebaseAsJSON.keys();
    }

    public void getFunctionalityWithName(String functionalityName) throws JSONException {
        requestedFunctionality = functionalityName;
    }

    public TraceDto getLongestTrace() throws JSONException {
        TraceDto longestTrace = getLongestTrace(_traceGraphs.get(requestedFunctionality).getFirstAccess());
        _traceGraphs.get(requestedFunctionality).resetVisited();
        return longestTrace;
    }

    private TraceDto getLongestTrace(Access access) throws JSONException {
        if (access.getNextAccessProbabilities().size() == 0 || access.getVisited()) {
            return accessListToTraceDto(List.of(access));
        }

        access.setVisited(true);

        List<TraceDto> traces = new ArrayList<>();
        for (TraceGraphNode next : access.getNextAccessProbabilities().keySet()) {
            traces.add(getLongestTrace((Access)next));
        }

        int longestSize = -1;
        int longestIndex = -1;

        for (int i = 0; i < traces.size(); i++) {
            int traceSize = traces.get(i).getUncompressedSize();
            if (longestIndex == -1 || traceSize > longestSize) {
                longestIndex = i;
                longestSize = traceSize;
            }
        }

        TraceDto longestTrace = traces.get(longestIndex);
        List<ReducedTraceElementDto> newAccessList = new ArrayList<>();
        if (access.getMode() != null)
            newAccessList.add(accessToAccessDto(access));
        newAccessList.addAll(longestTrace.getAccesses());
        longestTrace.setElements(newAccessList);

        return longestTrace;
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

        traceType = TraceType.LONGEST; // FIXME: REMOVE

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

    public TraceDto accessListToTraceDto(List<Access> accessList) {
        List<ReducedTraceElementDto> traceElementList = new ArrayList<>();

        AccessDto accessDto;
        for (Access access : accessList) {
            if (access.getMode() == null) continue; // skip the first node

            accessDto = accessToAccessDto(access);

            traceElementList.add(accessDto);
        }

        return new TraceDto(0, 0, traceElementList);
    }

    public AccessDto accessToAccessDto(Access access) {
        AccessDto accessDto = new AccessDto();
        accessDto.setEntityID((short)access.getEntityAccessedId());
        accessDto.setMode((byte) (access.getMode().equals("R") ? 1 : 2));
        accessDto.setOccurrences(1);

        return accessDto;
    }
    
}