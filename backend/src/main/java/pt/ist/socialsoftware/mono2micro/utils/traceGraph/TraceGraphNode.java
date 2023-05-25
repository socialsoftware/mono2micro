package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TraceGraphNode {
    Map<TraceGraphNode, Float> nextAccessProbabilities;
    boolean visited;

    public TraceGraphNode() {
        this.nextAccessProbabilities = new HashMap<TraceGraphNode, Float>();
        this.visited = false;
    }

    public Map<TraceGraphNode, Float> getNextAccessProbabilities() {
        return this.nextAccessProbabilities;
    }

    public boolean getVisited() {
        return this.visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public abstract void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd);
}
