package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class If extends TraceGraphNode {
    List<TraceGraphNode> condition;
    List<TraceGraphNode> thenBody;
    List<TraceGraphNode> elseBody;

    public If(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

        JSONArray condition = FunctionalityGraphTracesIterator.getRoleInSubTrace("condition", referenceElements);
        if (condition != null) {
            this.setCondition(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(condition.getInt(1))));
        }

        JSONArray thenBody = FunctionalityGraphTracesIterator.getRoleInSubTrace("then", referenceElements);
        if (thenBody != null) {
            this.setThenBody(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(thenBody.getInt(1))));
        }

        JSONArray elseBody = FunctionalityGraphTracesIterator.getRoleInSubTrace("else", referenceElements);
        if (elseBody != null) {
            this.setElseBody(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(elseBody.getInt(1))));
        }
    }

    public List<TraceGraphNode> getCondition() {
        return condition;
    }

    public List<TraceGraphNode> getThenBody() {
        return thenBody;
    }

    public List<TraceGraphNode> getElseBody() {
        return elseBody;
    }

    public void setCondition(List<TraceGraphNode> condition) {
        this.condition = condition;
    }

    public void setThenBody(List<TraceGraphNode> thenBody) {
        this.thenBody = thenBody;
    }

    public void setElseBody(List<TraceGraphNode> elseBody) {
        this.elseBody = elseBody;
    }

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd) {
        Access startingNode = new Access();
        Access endingNode = new Access();
        
        Access baseNode;
        
        TraceGraph condition = FunctionalityGraphTracesIterator.processSubTrace(this.getCondition(), lastCallEnd, lastLoopStart, lastLoopEnd);
        TraceGraph thenGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getThenBody(), lastCallEnd, lastLoopStart, lastLoopEnd);
        TraceGraph elseGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getElseBody(), lastCallEnd, lastLoopStart, lastLoopEnd);

        if (condition != null) {
            startingNode.nextAccessProbabilities.put(condition.getFirstAccess(), 1f);

            baseNode = condition.getLastAccess();
        } else {
            baseNode = startingNode;
        }

        if (thenGraph != null) {
            baseNode.nextAccessProbabilities.put(thenGraph.getFirstAccess(), 1f); // FIXME: change probability (A)
            thenGraph.getLastAccess().nextAccessProbabilities.put(endingNode, 1f);
        } else {
            baseNode.nextAccessProbabilities.put(endingNode, 1f); // FIXME: change probability (A)
        }

        if (elseGraph != null) {
            baseNode.nextAccessProbabilities.put(elseGraph.getFirstAccess(), 1f); // FIXME: change probability (B)
            elseGraph.getLastAccess().nextAccessProbabilities.put(endingNode, 1f);
        } else {
            baseNode.nextAccessProbabilities.put(endingNode, 1f); // FIXME: change probability (B)
        }



        if (processedSubTrace.size() != 0) {
            processedSubTrace.get(processedSubTrace.size()-1).nextAccessProbabilities.put(startingNode, 1f);
        }

        processedSubTrace.add(startingNode);
        processedSubTrace.addAll(condition.getAllAccesses());
        processedSubTrace.addAll(thenGraph.getAllAccesses());
        processedSubTrace.addAll(elseGraph.getAllAccesses());
        processedSubTrace.add(endingNode);
    }

}