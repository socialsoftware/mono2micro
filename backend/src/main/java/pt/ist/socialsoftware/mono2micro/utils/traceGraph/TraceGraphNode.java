package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

public abstract class TraceGraphNode {
    int id;
    static int idCounter = 0;

    private int contextIndex;

    Map<TraceGraphNode, Float> nextAccessProbabilities;
    Map<TraceGraphNode, Float> prevAccessProbabilities;
    boolean visited;
    boolean lockedToNewConnections;

    public TraceGraphNode() {
        this.nextAccessProbabilities = new HashMap<TraceGraphNode, Float>();
        this.prevAccessProbabilities = new HashMap<TraceGraphNode, Float>();
        this.visited = false;
        this.lockedToNewConnections = false;

        id = idCounter++;
    }

    public int getId() {
        return id;
    }

    public int getContextIndex() {
        return contextIndex;
    }

    public void setContextIndex(int contextIndex) {
        this.contextIndex = contextIndex;
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

    public abstract void nodeToAccessGraph(TraceGraph traceGraph, AccessDto lastCallEnd, AccessDto lastLoopStart, AccessDto lastLoopEnd, HeuristicFlags heuristicFlags);

    /**
     * Creates connections around empty (or auxiliar) nodes and adds them to the removal stack.
     * @param toRemoveStack , empty nodes that are to be removed and have been bypassed
     */
    public TraceGraphNode bypassEmptySuccessors(TraceGraphNode lastValidNode, List<TraceGraphNode> cleanedNodes, List<TraceGraphNode> toRemoveStack) {
        /* boolean isAuxNode = ((Access)this).getMode() == null;

        if (this.nextAccessProbabilities.size() == 0)
            return !isAuxNode? this: null;

        TraceGraphNode currentLastValidNode = isAuxNode? lastValidNode: this;

        Map<TraceGraphNode, Float> oldNextAccessProbabilities = new HashMap<>(this.nextAccessProbabilities);

        Map<TraceGraphNode, Float> newNextAccessProbabilities = new HashMap<>();

        this.nextAccessProbabilities.clear();
        
        TraceGraphNode closestValidNode;
        for (TraceGraphNode traceGraphNode : oldNextAccessProbabilities.keySet()) {
            closestValidNode = traceGraphNode.bypassEmptySuccessors(currentLastValidNode, cleanedNodes, toRemoveStack);

            if (closestValidNode != null) {
                newNextAccessProbabilities.put(closestValidNode, this.nextAccessProbabilities.get(traceGraphNode));
            }
        }

        lastValidNode.nextAccessProbabilities.putAll(newNextAccessProbabilities);

         */

         return null;
    }
   
}