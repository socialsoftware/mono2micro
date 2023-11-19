package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class Switch extends TraceGraphNode {
    List<TraceGraphNode> expression;
    List<List<TraceGraphNode>> cases;

    public Switch(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setContextIndex(traceElementJSON.getInt(1));

        cases = new ArrayList<>();

        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

        JSONArray expression = FunctionalityGraphTracesIterator.getRoleInSubTrace("expression", referenceElements);

        if (expression != null) {
            this.setExpression(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(expression.getInt(1))));
        }

        List<JSONArray> allCases = FunctionalityGraphTracesIterator.getAllOfRoleInSubTrace("case", referenceElements);
        if (allCases != null && !allCases.isEmpty()) {
            for (JSONArray c : allCases) {
                this.addCase((FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(c.getInt(1)))));
            }
        }
    }

    public List<TraceGraphNode> getExpression() {
        return expression;
    }

    public void setExpression(List<TraceGraphNode> expression) {
        this.expression = expression;
    }

    public List<List<TraceGraphNode>> getCases() {
        return cases;
    }

    public void setCases(List<List<TraceGraphNode>> cases) {
        this.cases = cases;
    }

    public void addCase(List<TraceGraphNode> c) {
        this.cases.add(c);
    }

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {

        if ((this.getExpression() == null || this.getExpression().isEmpty()) && this.getCases().isEmpty()) return;
        
        //TODO: find a way of applying the heuristics to the switch
        //HeuristicFlags expressionHeuristicFlags = new HeuristicFlags();
        
        TraceGraph processedSubTrace = new TraceGraph();
        
        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);

        float optionProbability = 1f / this.getCases().size();

        AccessDto baseNode;
        
        TraceGraph expressionGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getExpression(), lastCallEnd, lastLoopStart, lastLoopEnd, new HeuristicFlags());

        //List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(conditionHeuristicFlags, thenHeuristicFlags, elseHeuristicFlags);

        if (expressionGraph != null && !expressionGraph.isEmpty()) {
            processedSubTrace.addGraph(expressionGraph);

            processedSubTrace.addEdge(startingNode, expressionGraph.getFirstAccess(), 1f);

            baseNode = expressionGraph.getLastAccess();
        } else {
            baseNode = startingNode;
        }

        for (List<TraceGraphNode> caseTrace : this.getCases()) {
            TraceGraph caseGraph = FunctionalityGraphTracesIterator.processSubTrace(caseTrace, endingNode, lastLoopStart, lastLoopEnd, new HeuristicFlags());

            if (!caseGraph.isEmpty()) {
                processedSubTrace.addGraph(caseGraph);
    
                processedSubTrace.addEdge(baseNode, caseGraph.getFirstAccess(), optionProbability);
                processedSubTrace.addEdge(caseGraph.getLastAccess(), endingNode, 1f);
            } else {
                if (processedSubTrace.getGraph().containsEdge(startingNode, endingNode)) {
                    DefaultWeightedEdge edge = processedSubTrace.getGraph().getEdge(startingNode, endingNode);
                    processedSubTrace.getGraph().setEdgeWeight(edge, processedSubTrace.getGraph().getEdgeWeight(edge) + optionProbability);
                } else {
                    processedSubTrace.addEdge(startingNode, endingNode, optionProbability);
                }
            }
            
        }
        
        
        if (!processedSubTrace.isEmpty()) {
            try {
                boolean endingConnected = !Graphs.predecessorListOf(processedSubTrace.getGraph(), endingNode).isEmpty();
                if (endingConnected) processedSubTrace.setLastAccess(endingNode);

                boolean traceGraphHadLast = traceGraph.getLastAccess() != null;
                traceGraph.addGraph(processedSubTrace);
                if (traceGraphHadLast)
                    traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1.0f);

                if (endingConnected) traceGraph.setLastAccess(endingNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

