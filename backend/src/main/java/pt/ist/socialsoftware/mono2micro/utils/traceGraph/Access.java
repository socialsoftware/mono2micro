package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.json.JSONArray;
import org.json.JSONException;


public class Access extends TraceGraphNode {
    String mode = null;
    int entityAccessedId;

    public Access() {}

    public Access(JSONArray traceElementJSON) throws JSONException {
        this.setMode(traceElementJSON.getString(0));
        this.setEntityAccessedId(traceElementJSON.getInt(1));
    }


    public String getMode() {
        return mode;
    }

    public int getEntityAccessedId() {
        return entityAccessedId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setEntityAccessedId(int entityAccessedId) {
        this.entityAccessedId = entityAccessedId;
    }



    public void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd) {
        if (processedSubTrace.size() != 0) {
            processedSubTrace.get(processedSubTrace.size()-1).nextAccessProbabilities.put(this, 1f);
        }
        processedSubTrace.add(this);
    }

}