package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class Loop extends TraceGraphNode {
    List<TraceGraphNode> expressionGraph;
    List<TraceGraphNode> body;

    public Loop(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

        JSONArray expressionGraph = FunctionalityGraphTracesIterator.getRoleInSubTrace("expressionGraph", referenceElements);
        if (expressionGraph != null) {
            this.setExpression(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(expressionGraph.getInt(1))));
        }

        JSONArray body = FunctionalityGraphTracesIterator.getRoleInSubTrace("body", referenceElements);
        if (body != null) {
            this.setBody(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(body.getInt(1))));
        }
    }

    public List<TraceGraphNode> getExpression() {
        return expressionGraph;
    }

    public List<TraceGraphNode> getBody() {
        return body;
    }

    public void setExpression(List<TraceGraphNode> expressionGraph) {
        this.expressionGraph = expressionGraph;
    }

    public void setBody(List<TraceGraphNode> body) {
        this.body = body;
    }

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd) {
        Access startingNode = new Access();
        Access endingNode = new Access();
        
        TraceGraph expressionGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getExpression(), lastCallEnd, lastLoopStart, lastLoopEnd);
        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), lastCallEnd, lastLoopStart, lastLoopEnd);

        if (expressionGraph != null) {
            // enter condition
            startingNode.nextAccessProbabilities.put(expressionGraph.getFirstAccess(), 1f);

            // return to head (if no body)
            if(bodyGraph == null) {
                expressionGraph.getLastAccess().nextAccessProbabilities.put(expressionGraph.getFirstAccess(), 1f); // FIXME: change probability (P)
            }

            // exit body
            bodyGraph.getLastAccess().nextAccessProbabilities.put(endingNode, 1f); // FIXME: change probability (A)
        }

        if (bodyGraph != null) {
            // enter body
            if (expressionGraph != null) {
                expressionGraph.getLastAccess().nextAccessProbabilities.put(bodyGraph.getFirstAccess(), 1f); // FIXME: change probability (P)
            } else {
                startingNode.nextAccessProbabilities.put(bodyGraph.getFirstAccess(), 1f); // FIXME: change probability (P)
            }

            // return to head
            if (expressionGraph != null) {
                bodyGraph.getLastAccess().nextAccessProbabilities.put(expressionGraph.getFirstAccess(), 1f);
            } else {
                bodyGraph.getLastAccess().nextAccessProbabilities.put(bodyGraph.getFirstAccess(), 1f); // FIXME: change probability (P)
                
                // exit body (since expression is "empty")
                bodyGraph.getLastAccess().nextAccessProbabilities.put(endingNode, 1f); // FIXME: change probability (A)
            }
        }


    }

}

