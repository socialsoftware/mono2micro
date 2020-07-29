package collectors;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Container<MySourcePosition> id;
    private JsonArray entitiesSequence;
    private List<Node> edges; // links to other nodes

    public Node(Container<MySourcePosition> id) {
        this.id = id;
        this.entitiesSequence = new JsonArray();
        this.edges = new ArrayList<>();
    }

    public Container<MySourcePosition> getId() {
        return id;
    }

    public void setId(Container<MySourcePosition> id) {
        this.id = id;
    }

    public JsonArray getEntitiesSequence() {
        return entitiesSequence;
    }

    public void addEntityAccess(JsonArray entityAccess) {
        entitiesSequence.add(entityAccess);
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void addEdge(Node node) {
        if (!this.edges.contains(node))
            this.edges.add(node);
    }

    public void removeLastEdge() {
        edges.remove(edges.size()-1);
    }

    @Override
    public String toString() {
        return id.list.get(id.list.size() - 1).toString();
    }
}
