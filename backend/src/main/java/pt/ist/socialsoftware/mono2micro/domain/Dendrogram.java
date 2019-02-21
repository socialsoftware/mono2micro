package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Dendrogram {
	private List<Graph> graphs = new ArrayList<>();
	private List<Controller> controllers = new ArrayList<>();
	private List<Entity> entities = new ArrayList<>();

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

	public List<Graph> getGraphs() {
		return this.graphs;
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
	}

	public void mergeClusters(String graphName, String cluster1, String cluster2, String newName) {
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).mergeClusters(cluster1, cluster2, newName);
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

	public void splitCluster(String graphName, String clusterName, String newName, String[] entities) {
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).splitCluster(clusterName, newName, entities);
			}
		}
	}

	public List<Entity> getEntities() {
		return this.entities;
	}


	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

	public Entity getEntity(String entityName) {
		for (Entity entity : this.entities) {
			if (entity.getName().equals(entityName))
				return entity;
		}
		return null;
	}

	public boolean containsEntity(String entityName) {
		for (Entity entity : this.entities) {
			if (entity.getName().equals(entityName))
				return true;
		}
		return false;
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
		for (Controller controller : this.controllers) {
			if (controller.getName().equals(controllerName))
				return true;
		}
		return false;
	}

	public List<Cluster> getControllerClusters(String graphName, String controllerName) {
		List<Cluster> result = new ArrayList<>();

		Graph graph = getGraph(graphName);
		Controller controller = getController(controllerName);

		for (String entity : controller.getEntities()) {
			for (Cluster cluster : graph.getClusters()) {
				if (!result.contains(cluster) && cluster.containsEntity(entity)) {
					result.add(cluster);
				}
			}
		}
		return result;
	}

	public void transferEntities(String graphName, String fromCluster, String toCluster, String[] entities) {
		this.getGraph(graphName).transferEntities(fromCluster, toCluster, entities);
	}

}
