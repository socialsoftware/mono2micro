package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dendrogram {
	private List<Graph> graphs = new ArrayList<>();
	private List<Controller> controllers = new ArrayList<>();
	private String linkageType;

	public Dendrogram() {
	}

	public void setLinkageType(String linkageType) {
		this.linkageType = linkageType;
	}

	public String getLinkageType() {
		return this.linkageType;
	}

	public List<Graph> getGraphs() {
		return this.graphs;
	}

	public boolean deleteGraph(String graphName) {
		for (int i = 0; i < this.graphs.size(); i++) {
			if (this.graphs.get(i).getName().equals(graphName)) {
				this.graphs.remove(i);
				return true;
			}
		}
		return false;
	}

	public List<String> getGraphsNames() {
		List<String> graphsNames = new ArrayList<>();
		for (Graph graph : this.graphs)
			graphsNames.add(graph.getName());
		return graphsNames;
	}

	public Graph getGraph(String graphName) {
		for (Graph graph : this.graphs) {
			if (graph.getName().equals(graphName))
				return graph;
		}
		return null;
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
		calculateMetrics(graph.getName());
	}

	public void mergeClusters(String graphName, String cluster1, String cluster2, String newName) {
		getGraph(graphName).mergeClusters(cluster1, cluster2, newName);
		calculateMetrics(graphName);
	}

	public boolean renameCluster(String graphName, String clusterName, String newName) {
		return getGraph(graphName).renameCluster(clusterName, newName);
	}

	public boolean renameGraph(String graphName, String newName) {
		if (getGraphsNames().contains(newName))
			return false;
		getGraph(graphName).setName(newName);
		return true;
	}

	public void splitCluster(String graphName, String clusterName, String newName, String[] entities) {
		getGraph(graphName).splitCluster(clusterName, newName, entities);
		calculateMetrics(graphName);
	}

	public List<Controller> getControllers() {
		return this.controllers;
	}

	public void addController(Controller controller) {
		this.controllers.add(controller);
	}

	public Controller getController(String controllerName) {
		for (Controller controller : this.controllers) {
			if (controller.getName().equals(controllerName))
				return controller;
		}
		return null;
	}

	public boolean containsController(String controllerName) {
		if (getController(controllerName) == null)
			return false;
		return true;
	}

	public Map<String,List<Cluster>> getControllerClusters(String graphName) {
		Map<String,List<Cluster>> result = new HashMap<>();

		Graph graph = getGraph(graphName);
		for (Controller controller : this.controllers) {
			List<Cluster> touchedClusters = new ArrayList<>();
			for (String entity : controller.getEntities()) {
				for (Cluster cluster : graph.getClusters()) {
					if (!touchedClusters.contains(cluster) && cluster.containsEntity(entity)) {
						touchedClusters.add(cluster);
					}
				}
			}
			result.put(controller.getName(), touchedClusters);
		}
		return result;
	}

	public Map<String,List<Controller>> getClusterControllers(String graphName) {
		Map<String,List<Controller>> result = new HashMap<>();

		Graph graph = getGraph(graphName);
		for (Cluster cluster : graph.getClusters()) {
			List<Controller> touchedControllers = new ArrayList<>();
			for (String clusterEntity : cluster.getEntities()) {
				for (Controller controller : this.controllers) {
					if (!touchedControllers.contains(controller) && controller.getEntities().contains(clusterEntity)) {
						touchedControllers.add(controller);
					}
				}
			}
			result.put(cluster.getName(), touchedControllers);
		}
		return result;
	}

	public void transferEntities(String graphName, String fromCluster, String toCluster, String[] entities) {
		getGraph(graphName).transferEntities(fromCluster, toCluster, entities);
		calculateMetrics(graphName);
	}

	public void calculateMetrics(String graphName) {
		getGraph(graphName).calculateMetrics(this.controllers);
	}

}
