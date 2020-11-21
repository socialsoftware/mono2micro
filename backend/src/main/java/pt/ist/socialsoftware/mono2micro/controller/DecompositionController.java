package pt.ist.socialsoftware.mono2micro.controller;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/decompositions", method = RequestMethod.GET)
	public ResponseEntity<List<Decomposition>> getDecompositions(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDecompositions");

		try {
			return new ResponseEntity<>(
				codebaseManager.getDendrogramDecompositionsWithFields(
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

	@RequestMapping(value = "/decomposition/{decompositionName}", method = RequestMethod.GET)
	public ResponseEntity<Decomposition> getDecomposition(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDecomposition");

		try {
			return new ResponseEntity<>(
				codebaseManager.getDendrogramDecompositionWithFields(
					codebaseName,
					dendrogramName,
					decompositionName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDecomposition(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName
	) {
		logger.debug("deleteDecomposition");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME And each dendrogram directory could have its own decompositions etc...
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).deleteDecomposition(decompositionName);
			codebaseManager.writeCodebase(codebase);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getLocalTransactionsGraphForController", method = RequestMethod.GET)
	public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getControllerLocalTransactionsGraph(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@RequestParam String controllerName
	) {
		logger.debug("getControllerLocalTransactionsGraph");

		try {

			Codebase codebase = codebaseManager.getCodebaseWithFields(
				codebaseName,
				new HashSet<String>() {{ add("datafilePath"); }}
			);

			Dendrogram dendrogram = codebaseManager.getCodebaseDendrogramWithFields(
				codebaseName,
				dendrogramName,
				new HashSet<String>() {{
					add("tracesMaxLimit"); add("traceType");
				}}
			);

			Decomposition decomposition = codebaseManager.getDendrogramDecompositionWithFields(
				codebaseName,
				dendrogramName,
				decompositionName,
				new HashSet<String>() {{
					add("controllers"); add("entityIDToClusterName");
				}}
			);

			DirectedAcyclicGraph<Decomposition.LocalTransaction, DefaultEdge> controllerLocalTransactionsGraph = decomposition.getControllerLocalTransactionsGraph(
				codebase,
				controllerName,
				dendrogram.getTraceType(),
				dendrogram.getTracesMaxLimit()
			);

			return new ResponseEntity<>(
				Utils.getSerializableLocalTransactionsGraph(controllerLocalTransactionsGraph),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}