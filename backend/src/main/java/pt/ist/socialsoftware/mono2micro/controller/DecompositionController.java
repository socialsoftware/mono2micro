package pt.ist.socialsoftware.mono2micro.controller;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithm;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithmFactory;
import pt.ist.socialsoftware.mono2micro.domain.source.Source;
import pt.ist.socialsoftware.mono2micro.domain.strategy.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.AccessesSciPyInfoDto;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.InfoDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/strategy/{strategyName}")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/createDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDecomposition(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@RequestBody InfoDto infoDto
	) {
		logger.debug("createDecomposition");

		try {
			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, strategyName);
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());
			clusteringAlgorithm.createDecomposition(strategy, infoDto);

			return new ResponseEntity<>(HttpStatus.OK);


		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	// Unfortunately, Multipart files do not work well with JsonSubTypes, so this controller had to be created
	@RequestMapping(value = "/createExpertDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createExpertDecomposition(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@RequestParam String type,
			@RequestParam String expertName,
			@RequestParam Optional<MultipartFile> expertFile
	) {
		logger.debug("createExpertDecomposition");

		try {
			InfoDto infoDto;

			switch (type) {
				case ACCESSES_SCIPY:
					infoDto = new AccessesSciPyInfoDto(expertName, expertFile);
					break;
				default:
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, strategyName);
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());
			clusteringAlgorithm.createDecomposition(strategy, infoDto);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decompositions", method = RequestMethod.GET)
	public ResponseEntity<List<Decomposition>> getDecompositions(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDecompositions");

		try {
			return new ResponseEntity<>(
				codebaseManager.getStrategyDecompositionsWithFields(
					codebaseName,
					strategyName,
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
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDecomposition");

		try {
			return new ResponseEntity<>(
				codebaseManager.getStrategyDecompositionWithFields(
					codebaseName,
					strategyName,
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
		@PathVariable String strategyName,
		@PathVariable String decompositionName
	) {
		logger.debug("deleteDecomposition");

		try {
			codebaseManager.deleteStrategyDecomposition(codebaseName, strategyName, decompositionName);
			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, strategyName);
			strategy.removeDecompositionName(decompositionName);
			codebaseManager.writeCodebaseStrategy(codebaseName, strategy);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getLocalTransactionsGraphForController", method = RequestMethod.GET)
	public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getControllerLocalTransactionsGraph(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@RequestParam String controllerName
	) {
		logger.debug("getControllerLocalTransactionsGraph");

		try {

			// TODO: abstract strategy call to make this a generic function, probably needs decompositions with subclasses
			AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) codebaseManager.getCodebaseStrategy(codebaseName, strategyName);

			Decomposition decomposition = codebaseManager.getStrategyDecompositionWithFields(
				codebaseName,
				strategyName,
				decompositionName,
				new HashSet<String>() {{
					add("controllers"); add("entityIDToClusterID");
				}}
			);

			Source source = codebaseManager.getCodebaseSource(codebaseName, ACCESSES);

			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> controllerLocalTransactionsGraph = decomposition.getControllerLocalTransactionsGraph(
				source.getInputFilePath(),
				controllerName,
				strategy.getTraceType(),
				strategy.getTracesMaxLimit()
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