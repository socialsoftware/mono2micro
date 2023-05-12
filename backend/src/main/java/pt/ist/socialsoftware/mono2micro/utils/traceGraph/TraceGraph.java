package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;

public class TraceGraph {
    Access firstAccess;
    List<Access> allAccesses;

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
        return this.getAllAccesses().get(this.getAllAccesses().size()-1);
    }
}
