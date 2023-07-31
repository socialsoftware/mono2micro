package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TraceGraphNode {
    Map<TraceGraphNode, Float> nextAccessProbabilities;
    Map<TraceGraphNode, Float> prevAccessProbabilities;
    boolean visited;
    boolean lockedToNewConnections;

    public TraceGraphNode() {
        this.nextAccessProbabilities = new HashMap<TraceGraphNode, Float>();
        this.prevAccessProbabilities = new HashMap<TraceGraphNode, Float>();
        this.visited = false;
        this.lockedToNewConnections = false;
    }

    public Map<TraceGraphNode, Float> getNextAccessProbabilities() {
        return this.nextAccessProbabilities;
    }

    public Map<TraceGraphNode, Float> getPrevAccessProbabilities() {
        return this.prevAccessProbabilities;
    }

    public void addSuccessor(TraceGraphNode successor, Float probability) {
        if (!this.getLockedToNewConnections()) {
            this.nextAccessProbabilities.put(successor, probability);
    
            successor.prevAccessProbabilities.put(this, probability);
        }

    }

    public boolean getVisited() {
        return this.visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean getLockedToNewConnections() {
        return this.lockedToNewConnections;
    }

    public void setLockedToNewConnections(boolean locked) {
        this.lockedToNewConnections = locked;
    }

    public abstract void nodeToAccessGraph(List<Access> processedSubTrace, TraceGraphNode lastCallEnd, TraceGraphNode lastLoopStart, TraceGraphNode lastLoopEnd, HeuristicFlags heuristicFlags);

    /**
     * Creates connections around empty (or auxiliar) nodes and adds them to the removal stack.
     * @param toRemoveStack , empty nodes that are to be removed and have been bypassed
     */
    public void bypassEmptySuccessors(TraceGraphNode lastValidNode, Float lastValidNodeToCurrentProbability, List<TraceGraphNode> toRemoveStack) {
        // FIXME: needs rework to correct prev access probabilities
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
