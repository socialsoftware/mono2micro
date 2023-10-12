package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class Loop extends TraceGraphNode {
    List<TraceGraphNode> expressionGraph;
    List<TraceGraphNode> body;

    public Loop(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setContextIndex(traceElementJSON.getInt(1));
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

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        HeuristicFlags expressionHeuristicFlags = new HeuristicFlags();
        HeuristicFlags bodyHeuristicFlags = new HeuristicFlags();

        TraceGraph processedSubTrace = new TraceGraph();

        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);
        
        TraceGraph expressionGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getExpression(), lastCallEnd, startingNode, endingNode, expressionHeuristicFlags);
        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), lastCallEnd, startingNode, endingNode, bodyHeuristicFlags);

        List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(expressionHeuristicFlags, bodyHeuristicFlags, null);
        appliableHeuristics.add(BranchHeuristics.LOOP_BRANCH_H); // add heuristic beacuse the loop is repeated if path taken
        Float enterLoopProbability = BranchHeuristics.calculateBranchProbability(appliableHeuristics);
        Float exitLoopProbability = 1-enterLoopProbability;

        if (expressionGraph != null && !expressionGraph.isEmpty()) {
            processedSubTrace.addGraph(expressionGraph);
            // enter condition
            processedSubTrace.addEdge(startingNode, expressionGraph.getFirstAccess(), 1f);

            // return to head (if no body)
            if(bodyGraph == null || !bodyGraph.isEmpty()) {
                processedSubTrace.addEdge(expressionGraph.getLastAccess(), expressionGraph.getFirstAccess(), enterLoopProbability);
            }

            // exit body
            processedSubTrace.addEdge(expressionGraph.getLastAccess(), endingNode, exitLoopProbability);
        }

        if (bodyGraph != null && !bodyGraph.isEmpty()) {
            processedSubTrace.addGraph(bodyGraph);
            // enter body
            if (expressionGraph != null) {
                processedSubTrace.addEdge(expressionGraph.getLastAccess(), bodyGraph.getFirstAccess(), enterLoopProbability);
            } else {
                processedSubTrace.addEdge(startingNode, bodyGraph.getFirstAccess(), enterLoopProbability);

                // not enter body (since expression is "empty")
                processedSubTrace.addEdge(startingNode, endingNode, exitLoopProbability);
            }

            // return to head
            if (expressionGraph != null && !expressionGraph.isEmpty()) {
                processedSubTrace.addEdge(bodyGraph.getLastAccess(), expressionGraph.getFirstAccess(), 1f);
            } else {
                processedSubTrace.addEdge(bodyGraph.getLastAccess(), bodyGraph.getFirstAccess(), enterLoopProbability);
                
                // exit body (since expression is "empty")
                processedSubTrace.addEdge(bodyGraph.getLastAccess(), endingNode, exitLoopProbability);
            }
        }

        if (!processedSubTrace.isEmpty()) {
            try {
                processedSubTrace.setLastAccess(endingNode);
                boolean traceGraphWasEmpty = traceGraph.isEmpty();
                traceGraph.addGraph(processedSubTrace);
                if (!traceGraphWasEmpty)
                    traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1f);
                traceGraph.setLastAccess(endingNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (heuristicFlags != null) {
            heuristicFlags.hasLoop = true;
        }

    }

}

