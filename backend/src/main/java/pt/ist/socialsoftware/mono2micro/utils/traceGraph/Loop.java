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

        JSONArray expressionGraph = FunctionalityGraphTracesIterator.getRoleInSubTrace("expression", referenceElements);
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

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags) {
        HeuristicFlags expressionHeuristicFlags = new HeuristicFlags();
        HeuristicFlags bodyHeuristicFlags = new HeuristicFlags();

        Access startingNode = new Access();
        Access endingNode = new Access();
        
        TraceGraph expressionGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getExpression(), lastCallEnd, startingNode, endingNode, expressionHeuristicFlags);
        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), lastCallEnd, startingNode, endingNode, bodyHeuristicFlags);

        List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(expressionHeuristicFlags, bodyHeuristicFlags, null);
        appliableHeuristics.add(BranchHeuristics.LOOP_BRANCH_H); // add heuristic beacuse the loop is repeated if path taken
        Float enterLoopProbability = BranchHeuristics.calculateBranchProbability(appliableHeuristics);
        Float exitLoopProbability = 1-enterLoopProbability;

        if (expressionGraph != null && expressionGraph.getAllAccesses().size() != 0) {
            // enter condition
            startingNode.addSuccessor(expressionGraph.getFirstAccess(), 1f);

            // return to head (if no body)
            if(bodyGraph == null || bodyGraph.getAllAccesses().size() == 0) {
                expressionGraph.getLastAccess().addSuccessor(expressionGraph.getFirstAccess(), enterLoopProbability);
            }

            // exit body
            expressionGraph.getLastAccess().addSuccessor(endingNode, exitLoopProbability);
        }

        if (bodyGraph != null && bodyGraph.getAllAccesses().size() != 0) {
            // enter body
            if (expressionGraph != null) {
                expressionGraph.getLastAccess().addSuccessor(bodyGraph.getFirstAccess(), enterLoopProbability);
            } else {
                startingNode.addSuccessor(bodyGraph.getFirstAccess(), enterLoopProbability);

                // not enter body (since expression is "empty")
                startingNode.addSuccessor(endingNode, exitLoopProbability);
            }

            // return to head
            if (expressionGraph != null && expressionGraph.getAllAccesses().size() != 0) {
                bodyGraph.getLastAccess().addSuccessor(expressionGraph.getFirstAccess(), 1f);
            } else {
                bodyGraph.getLastAccess().addSuccessor(bodyGraph.getFirstAccess(), enterLoopProbability);
                
                // exit body (since expression is "empty")
                bodyGraph.getLastAccess().addSuccessor(endingNode, exitLoopProbability);
            }
        }

        if (processedSubTrace.size() != 0) {
            processedSubTrace.get(processedSubTrace.size()-1).addSuccessor(startingNode, 1f);
        }

        processedSubTrace.add(startingNode);
        if (expressionGraph != null) processedSubTrace.addAll(expressionGraph.getAllAccesses());
        if (bodyGraph != null) processedSubTrace.addAll(bodyGraph.getAllAccesses());
        processedSubTrace.add(endingNode);

        if (heuristicFlags != null) {
            heuristicFlags.hasLoop = true;
        }

    }

}

