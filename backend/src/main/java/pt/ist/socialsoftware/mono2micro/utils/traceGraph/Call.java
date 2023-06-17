package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

public class Call extends TraceGraphNode {
    List<TraceGraphNode> body;

    public Call(JSONObject totalTrace, JSONArray totalTraceArray, JSONArray traceElementJSON) throws JSONException {
        this.setBody(FunctionalityGraphTracesIterator.translateSubTrace(totalTrace, totalTraceArray.getJSONObject(traceElementJSON.getInt(1))));
    }

    public List<TraceGraphNode> getBody() {
        return body;
    }

    public void setBody(List<TraceGraphNode> body) {
        this.body = body;
    }

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags) {

        Access startingNode = new Access();
        Access endingNode = new Access();
        
        TraceGraph bodyGraph = FunctionalityGraphTracesIterator.processSubTrace(this.getBody(), endingNode, lastLoopStart, lastLoopEnd, new HeuristicFlags());

        if (bodyGraph != null) {
            startingNode.nextAccessProbabilities.put(bodyGraph.getFirstAccess(), 1f);
            bodyGraph.getLastAccess().nextAccessProbabilities.put(endingNode, 1f);
        } else {
            startingNode.nextAccessProbabilities.put(endingNode, 1f);
        }

        

        if (processedSubTrace.size() != 0) {
            processedSubTrace.get(processedSubTrace.size()-1).nextAccessProbabilities.put(startingNode, 1f);
        }

        processedSubTrace.add(startingNode);
        processedSubTrace.addAll(bodyGraph.getAllAccesses());
        processedSubTrace.add(endingNode);

        if (heuristicFlags != null) {
            heuristicFlags.hasCall = true;
        }

    }

}
