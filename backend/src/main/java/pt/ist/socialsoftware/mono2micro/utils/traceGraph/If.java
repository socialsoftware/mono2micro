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

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags) {
        HeuristicFlags conditionHeuristicFlags = new HeuristicFlags();
        HeuristicFlags thenHeuristicFlags = new HeuristicFlags();
        HeuristicFlags elseHeuristicFlags = new HeuristicFlags();

        Float thenProbability;

        Access startingNode = new Access();
        Access endingNode = new Access();
        
        Access baseNode;
        
        TraceGraph condition = FunctionalityGraphTracesIterator.processSubTrace(this.getCondition(), lastCallEnd, lastLoopStart, lastLoopEnd, conditionHeuristicFlags);
        TraceGraph thenGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getThenBody(), lastCallEnd, lastLoopStart, lastLoopEnd, thenHeuristicFlags);
        TraceGraph elseGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getElseBody(), lastCallEnd, lastLoopStart, lastLoopEnd, elseHeuristicFlags);

        List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(conditionHeuristicFlags, thenHeuristicFlags, elseHeuristicFlags);
        if (elseGraph == null) elseHeuristicFlags.postDominant = true;
        thenProbability = BranchHeuristics.calculateBranchProbability(appliableHeuristics);

        if (condition != null && condition.getAllAccesses().size() != 0) {
            startingNode.addSuccessor(condition.getFirstAccess(), 1f);

            baseNode = condition.getLastAccess();
        } else {
            baseNode = startingNode;
        }

        if (thenGraph != null && thenGraph.getAllAccesses().size() != 0) {
            baseNode.addSuccessor(thenGraph.getFirstAccess(), thenProbability);
            thenGraph.getLastAccess().addSuccessor(endingNode, 1f);
        } else {
            baseNode.addSuccessor(endingNode, thenProbability);
        }

        if (elseGraph != null && elseGraph.getAllAccesses().size() != 0) {
            baseNode.addSuccessor(elseGraph.getFirstAccess(), 1-thenProbability);
            elseGraph.getLastAccess().addSuccessor(endingNode, 1f);
        } else {
            baseNode.addSuccessor(endingNode, 1-thenProbability);
        }



        if (processedSubTrace.size() != 0) {
            processedSubTrace.get(processedSubTrace.size()-1).addSuccessor(startingNode, 1f);
        }

        processedSubTrace.add(startingNode);
        if (condition != null) processedSubTrace.addAll(condition.getAllAccesses());
        if (thenGraph != null) processedSubTrace.addAll(thenGraph.getAllAccesses());
        if (elseGraph != null) processedSubTrace.addAll(elseGraph.getAllAccesses());
        processedSubTrace.add(endingNode);
    }

}
