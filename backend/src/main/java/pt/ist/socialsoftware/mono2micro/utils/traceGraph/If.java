package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class If extends TraceGraphNode {
    List<TraceGraphNode> condition;
    List<TraceGraphNode> thenBody;
    List<TraceGraphNode> elseBody;

    public If(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setContextIndex(traceElementJSON.getInt(1));

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

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        HeuristicFlags conditionHeuristicFlags = new HeuristicFlags();
        HeuristicFlags thenHeuristicFlags = new HeuristicFlags();
        HeuristicFlags elseHeuristicFlags = new HeuristicFlags();

        Float thenProbability;

        TraceGraph processedSubTrace = new TraceGraph();

        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);
        
        AccessDto baseNode;
        
        TraceGraph condition = FunctionalityGraphTracesIterator.processSubTrace(this.getCondition(), lastCallEnd, lastLoopStart, lastLoopEnd, conditionHeuristicFlags);
        TraceGraph thenGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getThenBody(), lastCallEnd, lastLoopStart, lastLoopEnd, thenHeuristicFlags);
        TraceGraph elseGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getElseBody(), lastCallEnd, lastLoopStart, lastLoopEnd, elseHeuristicFlags);

        List<String> appliableHeuristics = BranchHeuristics.getAppliableHeuristics(conditionHeuristicFlags, thenHeuristicFlags, elseHeuristicFlags);
        if (elseGraph == null) elseHeuristicFlags.postDominant = true;
        thenProbability = BranchHeuristics.calculateBranchProbability(0.5f, appliableHeuristics);

        if (condition != null && !condition.isEmpty()) {
            processedSubTrace.addGraph(condition);

            processedSubTrace.addEdge(startingNode, condition.getFirstAccess(), 1f);

            baseNode = condition.getLastAccess();
        } else {
            baseNode = startingNode;
        }

        if (!(thenGraph == null || thenGraph.isEmpty()) || !(elseGraph == null || elseGraph.isEmpty())) {
            if (thenGraph != null && !thenGraph.isEmpty()) {
                processedSubTrace.addGraph(thenGraph);
                
                processedSubTrace.addEdge(baseNode, thenGraph.getFirstAccess(), thenProbability);
    
                processedSubTrace.addEdge(thenGraph.getLastAccess(), endingNode, 1f);
    
            } else {
                processedSubTrace.addEdge(baseNode, endingNode, thenProbability);
            }
    
            if (elseGraph != null && !elseGraph.isEmpty()) {
                processedSubTrace.addGraph(elseGraph);
                
                processedSubTrace.addEdge(baseNode, elseGraph.getFirstAccess(), 1-thenProbability);
    
                processedSubTrace.addEdge(elseGraph.getLastAccess(), endingNode, 1f);
    
            } else {
                processedSubTrace.addEdge(baseNode, endingNode, 1-thenProbability);
            }

        } else {
            processedSubTrace.addEdge(baseNode, endingNode, 1);
        }


        if (!processedSubTrace.isEmpty()) {
            boolean thenIsLocked = false;
            boolean elseIsLocked = false;
            
            try {
                if (thenGraph != null && thenGraph.getLastAccess() != null) {
                    thenIsLocked = processedSubTrace.isVertexLockedToNewConnections(thenGraph.getLastAccess());
                }

                if (elseGraph != null && elseGraph.getLastAccess() != null) {
                    elseIsLocked = processedSubTrace.isVertexLockedToNewConnections(elseGraph.getLastAccess());
                }


                if (!(thenIsLocked && elseIsLocked)) processedSubTrace.setLastAccess(endingNode);

                boolean traceGraphHadLast = traceGraph.getLastAccess() != null;
                traceGraph.addGraph(processedSubTrace);
                if (traceGraphHadLast)
                    traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1.0f);

                if (!(thenIsLocked && elseIsLocked)) traceGraph.setLastAccess(endingNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
