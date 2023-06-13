package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class Label extends TraceGraphNode {
    String content;

    public Label(JSONArray traceElementJSON) throws JSONException{
        this.setContent(traceElementJSON.getString(0).substring(1));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags) {
        // TODO: implement (set flags for detected label)

        Access lastAccess = processedSubTrace.get(processedSubTrace.size()-1);

        switch (this.getContent()) {
            case HeuristicLabelType.CONTINUE:
                    lastAccess.nextAccessProbabilities.put(lastLoopStart, 1f); // FIXME: check probability
                    heuristicFlags.goingToLoopHead = true;
                break;
        
            case HeuristicLabelType.BREAK:
                    lastAccess.nextAccessProbabilities.put(lastLoopEnd, 1f); // FIXME: check probability
                    heuristicFlags.hasBreak = true;
                break;

            case HeuristicLabelType.RETURN:
                    lastAccess.nextAccessProbabilities.put(lastCallEnd, 1f); // FIXME: check probability
                    heuristicFlags.hasReturn = true;
                break;

            case HeuristicLabelType.ZERO_COMPARISON:
                    heuristicFlags.zeroComparison = true;
                break;
            default:
                break;
        }

    }
}