package collectors;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Container<MySourcePosition> id;
    private List<Access> entitiesSequence;
    private List<Node> edges; // links to other nodes
    private List<Node> parents;

    public Node(Container<MySourcePosition> id) {
        this.id = id;
        this.entitiesSequence = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.parents = new ArrayList<>();
    }

    public Container<MySourcePosition> getId() {
        return id;
    }

    public void setId(Container<MySourcePosition> id) {
        this.id = id;
    }

    public List<Access> getEntitiesSequence() {
        return entitiesSequence;
    }

    public void addEntityAccess(Access entityAccess) {
        entitiesSequence.add(entityAccess);
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void addEdge(Node node) {
        if (!this.edges.contains(node))
            this.edges.add(node);
    }

    public void removeEdge(Node node) {
        this.edges.remove(node);
    }

    public Node removeLastEdge() {
        return edges.remove(edges.size()-1);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public void addParent(Node parent) {
        if (!this.parents.contains(parent))
            this.parents.add(parent);
    }

    public List<Node> getParents() {
        return parents;
    }

    public void removeParent(Node node) {
        parents.remove(node);
    }
}
