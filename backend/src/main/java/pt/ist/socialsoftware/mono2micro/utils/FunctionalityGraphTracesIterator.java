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

import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Call;
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

    public FunctionalityGraphTracesIterator(InputStream file) throws IOException, JSONException {
        _codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();

        // create graph representation
        _traceGraphs = new HashMap<String, TraceGraph>();

        Iterator<String> functionalities = getFunctionalitiesNames();
        while (functionalities.hasNext()) {
            String functionality = functionalities.next();

            _traceGraphs.put(functionality, getFunctionalityTraceGraph(getFunctionalityWithName(functionality)));
        }
    }

    public Iterator<String> getFunctionalitiesNames() {
        return _codebaseAsJSON.keys();
    }

    public JSONObject getFunctionalityWithName(String functionalityName) throws JSONException {
        return _codebaseAsJSON.getJSONObject(functionalityName);
    }

    public TraceDto getLongestTrace() throws JSONException {
        return null;
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
        return null;
    }


    TraceGraph getFunctionalityTraceGraph(JSONObject object) throws JSONException {
        JSONObject mainTrace = object.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = translateSubTrace(object, mainTrace);

        return processSubTrace(preProcessedTraces);
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
                        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

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
        return processSubTrace(subTrace, null, null, null);
    }

    public static TraceGraph processSubTrace(List<TraceGraphNode> subTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd) {
        if (subTrace == null || subTrace.isEmpty()) return null;

        List<Access> processedSubTrace = new ArrayList<Access>();
        for (int i = 0; i < subTrace.size(); i++) {
            subTrace.get(i).nodeToAccessGraph(processedSubTrace, lastCallEnd, lastLoopStart, lastLoopEnd);
        }

        TraceGraph resultingGraph = new TraceGraph();
        resultingGraph.setAllAccesses(processedSubTrace);
        resultingGraph.setFirstAccess(processedSubTrace.get(0));

        return resultingGraph;
    }    
    
}