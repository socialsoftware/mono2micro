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

public class AbstractCall extends TraceGraphNode {
    List<List<TraceGraphNode>> overrideOptions;

    public AbstractCall(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setContextIndex(traceElementJSON.getInt(1));

        overrideOptions = new ArrayList<>();

        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

        List<JSONArray> overrides = FunctionalityGraphTracesIterator.getAllOfRoleInSubTrace("op", referenceElements);
        if (overrides != null && !overrides.isEmpty()) {
            for (JSONArray override : overrides) {
                this.addOverrideOption(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(override.getInt(1))));
            }
        }
    }

    public List<List<TraceGraphNode>> getOverrideOptions() {
        return overrideOptions;
    }

    public void setOverrideOptions(List<List<TraceGraphNode>> overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    public void addOverrideOption(List<TraceGraphNode> option) {
        this.overrideOptions.add(option);
    }

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {

        if (this.getOverrideOptions().isEmpty()) return;

        TraceGraph processedSubTrace = new TraceGraph();

        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);

        float optionProbability = 1f / this.getOverrideOptions().size();

        int numberOfEmptyOptions = 0;

        for (List<TraceGraphNode> overrideOption : this.getOverrideOptions()) {
            TraceGraph override = FunctionalityGraphTracesIterator.processSubTrace(overrideOption, endingNode, null, null, new HeuristicFlags());

            if (!override.isEmpty()) {
                processedSubTrace.addGraph(override);

                processedSubTrace.addEdge(startingNode, override.getFirstAccess(), optionProbability);
                processedSubTrace.addEdge(override.getLastAccess(), endingNode, 1f);
            } else {
                numberOfEmptyOptions++;
            }
            
        }

        if (numberOfEmptyOptions > 0) {
            processedSubTrace.addEdge(startingNode, endingNode, numberOfEmptyOptions * optionProbability);
        }

        
        if (!processedSubTrace.isEmpty()) {
            boolean endingConnected = processedSubTrace.getGraph().containsVertex(endingNode) && !Graphs.predecessorListOf(processedSubTrace.getGraph(), endingNode).isEmpty();
            if (endingConnected) processedSubTrace.setLastAccess(endingNode);

            boolean traceGraphHadLast = traceGraph.getLastAccess() != null;
            traceGraph.addGraph(processedSubTrace);
            if (traceGraphHadLast)
                traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1.0f);

            if (endingConnected) traceGraph.setLastAccess(endingNode);
        }

        traceGraph.validate();

    }

}

