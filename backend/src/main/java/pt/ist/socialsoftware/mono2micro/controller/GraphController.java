package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}")
public class GraphController {

	private static Logger logger = LoggerFactory.getLogger(GraphController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getGraphs(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getGraphs");

		try {
			return new ResponseEntity<>(
				codebaseManager.getDendrogramGraphsWithFields(
					codebaseName,
					dendrogramName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		}
	}

	@RequestMapping(value = "/graph/{graphName}", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getGraph");

		try {
			return new ResponseEntity<>(
				codebaseManager.getDendrogramGraphWithFields(
					codebaseName,
					dendrogramName,
					graphName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/graph/{graphName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraph(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName
	) {
		logger.debug("deleteGraph");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME And each dendrogram directory could have its own graphs etc...
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).deleteGraph(graphName);
			codebaseManager.writeCodebase(codebase);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}