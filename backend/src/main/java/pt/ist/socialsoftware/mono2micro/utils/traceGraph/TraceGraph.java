package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.TopologicalOrderIterator;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

public class TraceGraph {
    private AccessDto firstAccess;
    private AccessDto lastAccess;
    private Graph<AccessDto, DefaultWeightedEdge> graph;
    
    private Set<AccessDto> vertexesLockedToNewConnections;

    private List<AccessDto> list;


    public TraceGraph() {
        //graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        graph = GraphTypeBuilder.<AccessDto, DefaultWeightedEdge> directed().weighted(true).edgeClass(DefaultWeightedEdge.class).buildGraph();
        vertexesLockedToNewConnections = new HashSet<>();

        list = new ArrayList<>();
    }

    public AccessDto getFirstAccess() {
        return firstAccess;
    }

    public void setFirstAccess(AccessDto firstAccess) {
        if (!this.graph.containsVertex(firstAccess))
            throw new RuntimeException("trying to set first access but access not in graph");
        this.firstAccess = firstAccess;

        this.removeFromList(this.firstAccess);
        this.addToList(0, this.firstAccess);
    }

    public AccessDto getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(AccessDto lastAccess, List<AccessDto> internalEnds) {
        if (lastAccess == null)
                throw new RuntimeException("trying to set last access as null");

        boolean allInternalEndsLocked = internalEnds != null; // true by default if there are internal endings

        if (internalEnds != null) {
            for (AccessDto accessDto : internalEnds) {
                if (!this.isVertexLockedToNewConnections(accessDto) || this.graph.containsEdge(accessDto, lastAccess)) {
                    allInternalEndsLocked = false;
    
                    // confirm internal ending vertex is connected to last access candidate
                    if (!this.graph.containsEdge(accessDto, lastAccess)) {
                        throw new RuntimeException("setting last access, but internal endings are not all connected");
                    } 
    
                    break;
                }
            }
        }

        if (!allInternalEndsLocked) {
            if (!this.graph.containsVertex(lastAccess))
                throw new RuntimeException("trying to set last access but access not in graph");

            this.lastAccess = lastAccess;

            this.removeFromList(this.lastAccess);
            this.addToList(this.lastAccess);
        } else {
            this.lastAccess = null; // if all internal endings are locked, everything is connected and doesn't need last access
        }

    }

    public Graph<AccessDto, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public boolean isEmpty() {
        return this.graph.vertexSet().stream().filter(e -> e.getEntityID() != -1).collect(Collectors.toList()).size() == 0;
    }

    public void addEdge(AccessDto from, AccessDto to, float probability) {
        if (isVertexLockedToNewConnections(from)) return;

        if (from == null || to == null) {
            throw new RuntimeException("Attempted to create edge with null vertex.");
        }else if (from.equals(to)) {
            throw new RuntimeException("Attempted to create edge with same start and end vertexes.");
        } else if (this.graph.containsEdge(from, to)) {
            throw new RuntimeException("Attempted to create edge that already exists.");
        }

        this.addVertex(from);
        this.addVertex(to);

        DefaultWeightedEdge edge = this.graph.addEdge(from, to);

        if (edge == null) {
            throw new RuntimeException("Edge was not created, when it should have." + from + " " + to);
        }

        this.graph.setEdgeWeight(edge, probability);

        double outgoingProbability = this.graph.outgoingEdgesOf(from).stream().reduce(0d, (subtotal, element) -> subtotal + this.graph.getEdgeWeight(element), Double::sum);
        if (outgoingProbability > 1.1d) {
            String trace = "";
            for (DefaultWeightedEdge e : this.graph.outgoingEdgesOf(from)) {
                if (!trace.equals("")) trace += "+";
                trace += this.graph.getEdgeWeight(e);
            }
            throw new RuntimeException("Added edge (" + probability + ") breaks the total probability of outgoing edges (" + trace + "=" + outgoingProbability + " is more than 1)."); 
        }
    }

    public void addVertex(AccessDto vertex) {
        if (vertex == null) return;

        if (!this.graph.containsVertex(vertex)) {
            this.graph.addVertex(vertex);
        }

        if (this.firstAccess == null) {
            this.setFirstAccess(vertex);
        }

        if (!this.list.contains(vertex)) {
            this.addToList(vertex);
        }
    }

    public void addGraph(TraceGraph other) {
        boolean hasLastAccess = this.lastAccess == null;

        if (this.firstAccess == null) {
            this.addVertex(other.firstAccess);
            this.setFirstAccess(other.firstAccess);
        }
        
        if (hasLastAccess) {
            this.addVertex(other.lastAccess);
        }

        for (DefaultWeightedEdge edge : other.getGraph().edgeSet()) {
            this.addEdge(other.getGraph().getEdgeSource(edge), other.getGraph().getEdgeTarget(edge), (float)other.getGraph().getEdgeWeight(edge));
        }
        
        for (AccessDto accessDto : other.toList()) {
            this.removeFromList(accessDto);
            this.addToList(accessDto);
        }

        for (AccessDto accessDto : other.vertexesLockedToNewConnections) {
            this.lockVertexToNewConnections(accessDto);
        }
    }

    public void addToList(AccessDto access) {
        this.list.add(access);
    }

    public void addToList(int index, AccessDto access) {
        this.list.add(index, access);
    }

    public void removeFromList(AccessDto access) {
        this.list.remove(access);
    }

    public List<AccessDto> toList() {
		return this.list;
	}

    public boolean isVertexLockedToNewConnections(AccessDto vertex) {
        if (vertex == null) return true; //FIXME: should resturn false

        return this.vertexesLockedToNewConnections.contains(vertex);
    }

    public void lockVertexToNewConnections(AccessDto vertex) {
        if (!isVertexLockedToNewConnections(vertex)) {
            this.vertexesLockedToNewConnections.add(vertex);
        }
    }

    public TraceGraph createDuplicate() {
        TraceGraph duplicate = new TraceGraph();

        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(this.graph);

        //duplicate.addVertex(this.getFirstAccess());

        Map<AccessDto, AccessDto> copiedVertices = new HashMap<>();

        // calculate all possible path lengths
        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            AccessDto vertex = it.next();
            AccessDto vertexCopy;
            if (copiedVertices.containsKey(vertex)) {
                vertexCopy = copiedVertices.get(vertex);
            } else {
                vertexCopy = new AccessDto();
                vertexCopy.setEntityID(vertex.getEntityID());
                vertexCopy.setMode(vertex.getMode());
                duplicate.addVertex(vertexCopy);

                copiedVertices.put(vertex, vertexCopy);
            }

            List<AccessDto> successors = Graphs.successorListOf(this.graph, vertex);

            for (AccessDto successor : successors) {
                AccessDto successorCopy;
                if (copiedVertices.containsKey(successor)) {
                    successorCopy = copiedVertices.get(successor);
                } else {
                    successorCopy = new AccessDto();
                    successorCopy.setEntityID(successor.getEntityID());
                    successorCopy.setMode(successor.getMode());

                    copiedVertices.put(successor, successorCopy);
                }


                duplicate.addEdge(vertexCopy, successorCopy, (float)this.graph.getEdgeWeight(this.graph.getEdge(vertex, successor)));
                
            }

            for (AccessDto accessDto : this.vertexesLockedToNewConnections) {
                duplicate.lockVertexToNewConnections(accessDto);
            }

            
            if (this.getLastAccess() == vertex) {
                try {
                    duplicate.setLastAccess(vertexCopy, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        duplicate.validate();

        return duplicate;
    }

    public void validate() {
        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(this.getGraph());
        if (iterator.hasNext()) {
            iterator.next(); // skip first
            for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
                AccessDto vertex = iterator.next();
                if (this.getGraph().inDegreeOf(vertex) == 0)
                    throw new RuntimeException("Resulting graph has a node other than the root with no predecessor."); 
            }
        }
    }

    public void cleanAuxiliaryNodes() {
        Iterator<AccessDto> iterator = new TopologicalOrderIterator<AccessDto, DefaultWeightedEdge>(this.graph);

        boolean first = true;
        for(Iterator<AccessDto> it = iterator; it.hasNext(); ) {
            if (first) { // don't remove root node
                first = false;
                continue;
            }

            AccessDto vertex = it.next();

            if (vertex.getEntityID() == -1) {
                List<AccessDto> predecessors = Graphs.predecessorListOf(this.graph, vertex);
                List<AccessDto> successors = Graphs.successorListOf(this.graph, vertex);

                for (AccessDto predecessor : predecessors) {
                    for (AccessDto successor : successors) {
                        double pathProbability = this.graph.getEdgeWeight(this.graph.getEdge(predecessor, vertex)) * this.graph.getEdgeWeight(this.graph.getEdge(vertex, successor));

                        if (this.graph.containsEdge(predecessor, successor)) {
                            DefaultWeightedEdge curEdge = this.graph.getEdge(predecessor, successor);
                            this.graph.setEdgeWeight(curEdge, pathProbability + this.graph.getEdgeWeight(curEdge));
                        } else {
                            DefaultWeightedEdge newEdge = this.graph.addEdge(predecessor, successor);
                            this.graph.setEdgeWeight(newEdge, pathProbability);
                        }
                    }
                }

                this.graph.removeVertex(vertex);


            }
        }

    }

}
