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

import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}")
public class GraphController {

	private static Logger logger = LoggerFactory.getLogger(GraphController.class);

    private CodebaseManager codebaseManager = new CodebaseManager();


	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getGraphs(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getGraphs");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getGraphs(), HttpStatus.OK);
	}


	@RequestMapping(value = "/graph/{graphName}", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("getGraph: {}", graphName);
		
		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getGraph(graphName), HttpStatus.OK);
	}


	@RequestMapping(value = "/graph/{graphName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraph(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("deleteGraph");

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		boolean success = codebase.getDendrogram(dendrogramName).deleteGraph(graphName);
		if (success) {
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}