package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;

public class Dendrogram {
	private ArrayList<Node> nodes = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();

    private static Dendrogram instance = null; 

	public Dendrogram() {
	}

	public static Dendrogram getInstance() { 
        if (instance == null) 
            instance = new Dendrogram(); 

        return instance; 
	}
	
	public void destroy() {
		instance = null;
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public void addNode(Node node) {
		this.nodes.add(node);
	}

	public ArrayList<Edge> getEdges() {
		return this.edges;
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

}
