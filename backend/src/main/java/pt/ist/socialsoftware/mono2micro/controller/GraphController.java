package pt.ist.socialsoftware.mono2micro.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.DendrogramManager;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}")
public class GraphController {

	private static Logger logger = LoggerFactory.getLogger(GraphController.class);

	private DendrogramManager dendrogramManager = new DendrogramManager();


	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getGraphs(@PathVariable String dendrogramName) {
		logger.debug("getGraphs");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<>(dend.getGraphs(), HttpStatus.OK);
	}


	@RequestMapping(value = "/graph/{graphName}", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(@PathVariable String dendrogramName, @PathVariable String graphName) {
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
	


	@RequestMapping(value = "/graph/{graphName}/rename", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> renameGraph(@PathVariable String dendrogramName, @PathVariable String graphName, @RequestParam String newName) {
		logger.debug("renameGraph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.renameGraph(graphName, newName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/graph/{graphName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraph(@PathVariable String dendrogramName, @PathVariable String graphName) {
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