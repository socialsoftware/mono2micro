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
        Access lastAccess = null;
        
        if (processedSubTrace.size() > 0) {
            lastAccess = processedSubTrace.get(processedSubTrace.size()-1);
        }

        switch (this.getContent()) {
            case HeuristicLabelType.CONTINUE:
                    if (lastAccess != null && lastLoopStart != null) {
                        lastAccess.addSuccessor(lastLoopStart, 1f);
                    }
                    heuristicFlags.goingToLoopHead = true;
                break;
        
            case HeuristicLabelType.BREAK:
                    if (lastAccess != null && lastLoopEnd != null){
                        lastAccess.addSuccessor(lastLoopEnd, 1f);
                    }
                    heuristicFlags.hasBreak = true;
                break;

            case HeuristicLabelType.RETURN:
                    if (lastAccess != null && lastCallEnd != null) {
                        lastAccess.addSuccessor(lastCallEnd, 1f);
                    }
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