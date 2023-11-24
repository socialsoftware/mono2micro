package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import org.json.JSONArray;
import org.json.JSONException;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;


public class Access extends TraceGraphNode {
    String mode = null;
    int entityAccessedId;

    public Access(int contextIndex) {
        this.setContextIndex(contextIndex);
    }

    public Access(JSONArray traceElementJSON, int contextIndex) throws JSONException {
        this.setMode(traceElementJSON.getString(0));
        this.setEntityAccessedId(traceElementJSON.getInt(1));
        this.setContextIndex(contextIndex);
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



    public void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags) {
        AccessDto access = new AccessDto();
        access.setEntityID((short)this.entityAccessedId);
        access.setMode(FunctionalityGraphTracesIterator.accessModeStringToByte(this.mode));

        if (!traceGraph.isEmpty())
            traceGraph.addEdge(traceGraph.getLastAccess(), access, 1.0f);
        else
            traceGraph.addVertex(access);

        try {
            traceGraph.setLastAccess(access);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (heuristicFlags != null && getMode() == "W") {
            heuristicFlags.hasStore = true;
        }
        
        traceGraph.validate();
    }

}
