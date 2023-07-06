package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
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

    public void addSuccessor(TraceGraphNode successor, Float probability) {
        this.nextAccessProbabilities.put(successor, probability);
    }

    public boolean getVisited() {
        return this.visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public abstract void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags);

    /**
     * Creates connections around empty (or auxiliar) nodes and adds them to the removal stack.
     * @param toRemoveStack , empty nodes that are to be removed and have been bypassed
     */
    public void bypassEmptySuccessors(TraceGraphNode lastValidNode, Float lastValidNodeToCurrentProbability, List<TraceGraphNode> toRemoveStack) {
        if (lastValidNodeToCurrentProbability == null) {
            lastValidNodeToCurrentProbability = 1.0f;
        }

        if (lastValidNode != null && ((Access)this).getMode() != null) {
            lastValidNode.addSuccessor(this, lastValidNodeToCurrentProbability);
        }

        if (this.getVisited() && ((Access)this).getMode() != null) return;

        this.setVisited(true);

        List<TraceGraphNode> nextAccessList = new ArrayList<>(this.getNextAccessProbabilities().keySet());

        if (nextAccessList.size() == 0) return;

        Float prob;
        if (((Access)this).getMode() == null) {
            toRemoveStack.add(this);

            for (TraceGraphNode successor: nextAccessList) {
                prob = lastValidNodeToCurrentProbability*this.getNextAccessProbabilities().get(successor);
                successor.bypassEmptySuccessors(lastValidNode, prob, toRemoveStack);
            }

        } else {

            for (TraceGraphNode successor: nextAccessList) {
                prob = this.getNextAccessProbabilities().get(successor);

                if (((Access)successor).getMode() == null) {
                    this.nextAccessProbabilities.remove(successor);
                }

                successor.bypassEmptySuccessors(this, prob, toRemoveStack);
            }

        }

        
    }
}
