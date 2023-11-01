package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import org.json.JSONArray;
import org.json.JSONException;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

public class Label extends TraceGraphNode {
    String content;

    public Label(JSONArray traceElementJSON, int contextIndex) throws JSONException{
        this.setContextIndex(contextIndex);
        this.setContent(traceElementJSON.getString(0).substring(1));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        AccessDto lastAccess = null;
        
        if (!traceGraph.isEmpty()) {
            lastAccess = traceGraph.getLastAccess();
        }

        switch (this.getContent()) {
            case HeuristicLabelType.CONTINUE:
                    if (lastAccess != null && lastLoopStart != null && !traceGraph.isVertexLockedToNewConnections(lastAccess)) {
                        traceGraph.addEdge(lastAccess, lastLoopStart, 1f);
                        traceGraph.lockVertexToNewConnections(lastAccess);
                    }
                    heuristicFlags.goingToLoopHead = true;
                break;
        
            case HeuristicLabelType.BREAK:
                    if (lastAccess != null && lastLoopEnd != null && !traceGraph.isVertexLockedToNewConnections(lastAccess)){
                        traceGraph.addEdge(lastAccess, lastLoopEnd, 1f);
                        traceGraph.lockVertexToNewConnections(lastAccess);
                    }
                    heuristicFlags.hasBreak = true;
                break;

            case HeuristicLabelType.RETURN:
                    if (lastAccess != null && lastCallEnd != null && !traceGraph.isVertexLockedToNewConnections(lastAccess)) {
                        traceGraph.addEdge(lastAccess, lastCallEnd, 1f); //FIXME: lastCallEnd may not be in the graph
                        traceGraph.lockVertexToNewConnections(lastAccess);
                    }
                    heuristicFlags.hasReturn = true;
                break;

            case HeuristicLabelType.ZERO_COMPARISON:
                    heuristicFlags.zeroComparison = true;
                break;

            case HeuristicLabelType.OBJECT_COMPARISON:
                heuristicFlags.objectComparison = true;
                break;

            case HeuristicLabelType.LATER_CHANGED_C_VARIABLE:
                heuristicFlags.laterChangedCVariable = true;
                break;
            default:
                break;
        }

    }
}