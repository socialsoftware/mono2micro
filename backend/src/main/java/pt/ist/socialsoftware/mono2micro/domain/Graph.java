package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	private String name;
	private List<Node> nodes;
	private List<Edge> edges;

	public Graph() {

	}

	public Graph(String name) {
		this.name = name;
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getNodes() {
		return this.nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Edge> getEdges() {
		return this.edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

}
