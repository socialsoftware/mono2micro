package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Dendrogram {
	private ArrayList<Node> nodes = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Graph> graphs = new ArrayList<>();

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

	public ArrayList<Graph> getGraphs() {
		return this.graphs;
	}

	public List<String> getGraphsNames() {
		List<String> graphsNames = new ArrayList<>();
		for (Graph graph : this.graphs)
			graphsNames.add(graph.getName());
		return graphsNames;
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
	}

	public void mergeClusters(String graphName, String cluster1, String cluster2) {
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).mergeClusters(cluster1, cluster2);
			}
		}
	}

	public boolean renameCluster(String graphName, String clusterName, String newName) {
		boolean success = true;
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				success = graphs.get(i).renameCluster(clusterName, newName);
			}
		}
		return success;
	}

	public boolean renameGraph(String graphName, String newName) {
		if (this.getGraphsNames().contains(newName))
			return false;
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).setName(newName);
			}
		}
		return true;
	}

}
