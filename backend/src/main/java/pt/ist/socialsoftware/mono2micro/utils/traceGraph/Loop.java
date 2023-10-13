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

        AccessDto continueNode;

        processedSubTrace.addVertex(startingNode);
        
        TraceGraph expressionGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getExpression(), lastCallEnd, startingNode, endingNode, expressionHeuristicFlags);

        TraceGraph expressionGraphCopy = null;
        if (expressionGraph != null && !expressionGraph.isEmpty()) {
            expressionGraphCopy = expressionGraph.createDuplicate();
            continueNode = expressionGraphCopy.getFirstAccess();
        } else {
            continueNode = endingNode;
        }

        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), lastCallEnd, continueNode, endingNode, bodyHeuristicFlags);

        List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(expressionHeuristicFlags, bodyHeuristicFlags, null);
        appliableHeuristics.add(BranchHeuristics.LOOP_BRANCH_H); // add heuristic beacuse the loop is repeated if path taken
        Float enterLoopProbability = BranchHeuristics.calculateBranchProbability(appliableHeuristics);
        Float exitLoopProbability = 1-enterLoopProbability;


        if (expressionGraph != null && !expressionGraph.isEmpty())
            processedSubTrace.addGraph(expressionGraph);

        if (bodyGraph != null && !bodyGraph.isEmpty())
            processedSubTrace.addGraph(bodyGraph);

        if (expressionGraph != null && !expressionGraph.isEmpty())
            processedSubTrace.addGraph(expressionGraphCopy);


        if (expressionGraph != null && !expressionGraph.isEmpty()) {
            // enter condition
            processedSubTrace.addEdge(startingNode, expressionGraph.getFirstAccess(), 1f);

            // return to head (if no body)
            // will go to a copy to avoid creating a loop in the graph,
            // which works because we only want to consider the code loop
            // running once (1 expression + 1 body + 1 expression + exit)
            if(bodyGraph == null || bodyGraph.isEmpty()) {
                processedSubTrace.addEdge(expressionGraph.getLastAccess(), expressionGraphCopy.getFirstAccess(), enterLoopProbability);
            }
            
            // exit body
            processedSubTrace.addEdge(expressionGraphCopy.getLastAccess(), endingNode, exitLoopProbability);
        }

        if (bodyGraph != null && !bodyGraph.isEmpty()) {
            // enter body
            if (expressionGraph != null && !expressionGraph.isEmpty()) {
                processedSubTrace.addEdge(expressionGraph.getLastAccess(), bodyGraph.getFirstAccess(), enterLoopProbability);
            } else {
                processedSubTrace.addEdge(startingNode, bodyGraph.getFirstAccess(), enterLoopProbability);

                // not enter body (since expression is "empty")
                processedSubTrace.addEdge(startingNode, endingNode, exitLoopProbability);
            }

            // return to head
            if (expressionGraph != null && !expressionGraph.isEmpty()) {
                processedSubTrace.addEdge(bodyGraph.getLastAccess(), expressionGraphCopy.getFirstAccess(), 1f);
            } else {               
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

