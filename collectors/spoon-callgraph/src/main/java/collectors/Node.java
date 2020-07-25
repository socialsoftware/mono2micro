package collectors;

import com.google.gson.JsonArray;
import spoon.reflect.cu.SourcePosition;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String id;
    private JsonArray entitiesSequence;
    private List<Node> edges; // links to other nodes

    public Node(String id) {
        this.id = id;
        this.entitiesSequence = new JsonArray();
        this.edges = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        this.edges.add(node);
    }
}
