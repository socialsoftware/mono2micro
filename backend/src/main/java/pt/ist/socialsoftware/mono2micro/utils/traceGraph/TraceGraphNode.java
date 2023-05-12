package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.List;
import java.util.Map;

public abstract class TraceGraphNode {
    Map<TraceGraphNode, Float> nextAccessProbabilities;
    boolean visited;

    public Map<TraceGraphNode, Float> getNextAccessProbabilities() {
        return nextAccessProbabilities;
    }

    public boolean getVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public abstract void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd);
}
