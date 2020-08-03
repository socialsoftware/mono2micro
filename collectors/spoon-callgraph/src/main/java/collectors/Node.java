package collectors;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Container id;
    private List<Access> accesses;
    private List<Node> edges; // links to other nodes
    private List<Node> parents;

    public Node(Container id) {
        this.id = id;
        this.accesses = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.parents = new ArrayList<>();
    }

    public Node(Node node) {
        this.id = node.getId();
        this.accesses = new ArrayList<>(node.getAccesses());
        this.parents = new ArrayList<>(node.getParents());
        this.edges = new ArrayList<>(node.getEdges());
    }

    public Container getId() {
        return id;
    }

    public void setId(Container id) {
        this.id = id;
    }

    public List<Access> getAccesses() {
        return accesses;
    }

    public void setAccesses(List<Access> accesses) {
        this.accesses = accesses;
    }

    public void addEntityAccess(Access entityAccess) {
//        if (entitiesSequence.size() == 0)
//            entitiesSequence.add(entityAccess);
//        else
//            if (!entitiesSequence.get(entitiesSequence.size()-1).equals(entityAccess))
//                entitiesSequence.add(entityAccess);
        accesses.add(entityAccess);
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
        return id == null ? "null" : id.toString();
    }

    public void addParent(Node parent) {
        if (!this.parents.contains(parent))
            this.parents.add(parent);
    }

    public List<Node> getParents() {
        return parents;
    }

    public void setParents(List<Node> parents) {
        this.parents = parents;
    }

    public void removeParent(Node node) {
        parents.remove(node);
    }

    public Node getLastParent() {
        if (parents.size() == 0) {
            System.err.println("Parents = 0");
            System.exit(1);
        }
        return parents.get(parents.size()-1);
    }

    public void setEdges(ArrayList<Node> edges) {
        this.edges = edges;
    }
}
