package pt.ist.socialsoftware.mono2micro.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.DendrogramManager;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

@RestController
@RequestMapping(value = "/mono2micro")
public class GraphController {

	private static Logger logger = LoggerFactory.getLogger(GraphController.class);

	private DendrogramManager dendrogramManager = new DendrogramManager();


	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getGraphs(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("getGraphs");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<>(dend.getGraphs(), HttpStatus.OK);
	}


	@RequestMapping(value = "/graph", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getGraph: {}", graphName);
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		List<Graph> graphs = dend.getGraphs();
		for (Graph graph : graphs) {
			if (graph.getName().equals(graphName)) {
				return new ResponseEntity<>(graph, HttpStatus.OK);
			}
		}

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}


	@RequestMapping(value = "/renameGraph", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> renameGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("newName") String newName) {
		logger.debug("renameGraph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.renameGraph(graphName, newName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
		} else {
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/deleteGraph", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("deleteGraph");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.deleteGraph(graphName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}