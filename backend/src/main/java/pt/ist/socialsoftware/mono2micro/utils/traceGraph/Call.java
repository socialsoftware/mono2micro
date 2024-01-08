package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class Call extends TraceGraphNode {
    List<TraceGraphNode> body;

    public Call(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setContextIndex(traceElementJSON.getInt(1));
        this.setBody(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(traceElementJSON.getInt(1))));
    }

    public List<TraceGraphNode> getBody() {
        return body;
    }

    public void setBody(List<TraceGraphNode> body) {
        this.body = body;
    }

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        List<AccessDto> internalEndings = new ArrayList<>();

        TraceGraph processedSubTrace = new TraceGraph();

        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);
        
        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), endingNode, null, null, new HeuristicFlags());

        if (bodyGraph == null || bodyGraph.isEmpty()) return;

        if (bodyGraph != null && !bodyGraph.isEmpty()) {
            processedSubTrace.addGraph(bodyGraph);

            processedSubTrace.addEdge(startingNode, bodyGraph.getFirstAccess(), 1f);

            if (bodyGraph.getLastAccess() != null) {
                processedSubTrace.addEdge(bodyGraph.getLastAccess(), endingNode, 1f);

                internalEndings.add(bodyGraph.getLastAccess());
            }

        } else {
            processedSubTrace.addEdge(startingNode, endingNode, 1f);

            internalEndings.add(startingNode);
        }

        

        if (!processedSubTrace.isEmpty()) {
            traceGraph.addGraph(processedSubTrace);
            if (traceGraph.getLastAccess() != null)
                traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1.0f);

            traceGraph.setLastAccess(endingNode, internalEndings);
        }

        if (heuristicFlags != null) {
            heuristicFlags.hasCall = true;
        }

        traceGraph.validate();

    }

}
