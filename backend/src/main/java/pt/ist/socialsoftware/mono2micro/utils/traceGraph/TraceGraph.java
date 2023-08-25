package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.List;

public class TraceGraph {
    Access firstAccess;
    List<Access> allAccesses;

    public TraceGraph() {}

    public TraceGraph(List<Access> allAccesses, Access firstAccess) {
        this.allAccesses = allAccesses;
        this.firstAccess = firstAccess;
    }

    public Access getFirstAccess() {
        return firstAccess;
    }

    public List<Access> getAllAccesses() {
        return allAccesses;
    }

    public void setFirstAccess(Access firstAccess) {
        this.firstAccess = firstAccess;
    }

    public void setAllAccesses(List<Access> allAccesses) {
        this.allAccesses = allAccesses;
    }

    public Access getLastAccess() {
        if (this.getAllAccesses().size() == 0) return null;
        return this.getAllAccesses().get(this.getAllAccesses().size()-1);
    }

    public void resetVisited() {
        for (Access access : allAccesses) {
            access.setVisited(false);
        }
    }

    public void removeEmptyNodes() {
        List<TraceGraphNode> toRemoveStack = new ArrayList<>();
        //firstAccess.bypassEmptySuccessors(null, null, toRemoveStack);

        for (TraceGraphNode node : toRemoveStack) {
            if (node == firstAccess) continue;
            allAccesses.remove(node);
        }

        firstAccess = allAccesses.get(0);
        resetVisited();
    }
}
