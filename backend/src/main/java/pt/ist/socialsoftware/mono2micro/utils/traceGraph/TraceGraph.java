package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

public class TraceGraph {
    private AccessDto firstAccess;
    private AccessDto lastAccess;
    private Graph<AccessDto, DefaultWeightedEdge> graph;
    
    private Set<AccessDto> vertexesLockedToNewConnections;

    private List<AccessDto> list;


    public TraceGraph() {
        //graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        graph = GraphTypeBuilder.<AccessDto, DefaultWeightedEdge> directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true).edgeClass(DefaultWeightedEdge.class).buildGraph();
        vertexesLockedToNewConnections = new HashSet<>();

        list = new ArrayList<>();
    }

    public AccessDto getFirstAccess() {
        return firstAccess;
    }

    public void setFirstAccess(AccessDto firstAccess) throws Exception {
        if (!this.graph.containsVertex(firstAccess))
            throw new Exception("trying to set first access but access not in graph");
        this.firstAccess = firstAccess;
    }

    public AccessDto getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(AccessDto lastAccess) throws Exception {
        if (!this.graph.containsVertex(lastAccess))
            throw new Exception("trying to set last access but access not in graph");
        this.lastAccess = lastAccess;

    }

    public Graph<AccessDto, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public boolean isEmpty() {
        //TODO: change it check if all nodes are aux, aka entityId = -1
        return this.graph.vertexSet().size() == 0; //FIXME: is never going to be empty because it always has 1 startingNode
    }

    public void addEdge(AccessDto from, AccessDto to, float probability) {
        this.addVertex(from);
        this.addVertex(to);

        DefaultWeightedEdge edge = this.graph.addEdge(from, to);
        this.graph.setEdgeWeight(edge, probability);
    }

    public void addVertex(AccessDto vertex) {
        if (!this.graph.containsVertex(vertex)) {
            this.graph.addVertex(vertex);
        }

        if (this.firstAccess == null) {
            this.firstAccess = vertex;
        }

        if (this.lastAccess == null) {
            this.lastAccess = vertex;
        }

        if (!this.list.contains(vertex)) {
            this.list.add(vertex);
        }
    }

    public void addGraph(TraceGraph other) {
        Graphs.addGraph(this.graph, other.getGraph());
        
        if (this.firstAccess == null) {
            this.firstAccess = other.firstAccess;
        }
        
        if (this.lastAccess == null) {
            this.lastAccess = other.lastAccess;
        }
        
        this.list.addAll(other.toList());
    }

    public List<AccessDto> toList() {
        if (this.firstAccess != this.lastAccess) {
            this.list.remove(this.lastAccess);
            this.list.add(this.lastAccess); // ensure it is the last member of the list
        }
		return this.list;
	}

    public boolean isVertexLockedToNewConnections(AccessDto vertex) {
        return this.vertexesLockedToNewConnections.contains(vertex);
    }

    public void lockVertexToNewConnections(AccessDto vertex) {
        if (!isVertexLockedToNewConnections(vertex)) {
            this.vertexesLockedToNewConnections.add(vertex);
        }
    }
}
