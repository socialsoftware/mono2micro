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
        List<AccessDto> internalEndings = new ArrayList<>();

        if (this.getOverrideOptions().isEmpty()) return;

        TraceGraph processedSubTrace = new TraceGraph();

        AccessDto startingNode = new AccessDto();
        AccessDto endingNode = new AccessDto();

        processedSubTrace.addVertex(startingNode);

        float optionProbability = 1f / this.getOverrideOptions().size();

        int numberOfOptions = 0;

        for (List<TraceGraphNode> overrideOption : this.getOverrideOptions()) {
            TraceGraph override = FunctionalityGraphTracesIterator.processSubTrace(overrideOption, endingNode, null, null, new HeuristicFlags());

            if (override != null && !override.isEmpty()) {
                processedSubTrace.addGraph(override);

                processedSubTrace.addEdge(startingNode, override.getFirstAccess(), optionProbability);
                processedSubTrace.addEdge(override.getLastAccess(), endingNode, 1f);
                numberOfOptions++;

                internalEndings.add(override.getLastAccess());
            }
            
        }

        if (this.getOverrideOptions().isEmpty() || numberOfOptions < this.getOverrideOptions().size()) {
            processedSubTrace.addEdge(startingNode, endingNode, 1f - numberOfOptions * optionProbability);

            internalEndings.add(startingNode);
        }

        
        if (!processedSubTrace.isEmpty()) {
            traceGraph.addGraph(processedSubTrace);
            if (traceGraph.getLastAccess() != null)
                traceGraph.addEdge(traceGraph.getLastAccess(), startingNode, 1.0f);

            traceGraph.setLastAccess(endingNode, internalEndings);
        }

        traceGraph.validate();

    }

}

