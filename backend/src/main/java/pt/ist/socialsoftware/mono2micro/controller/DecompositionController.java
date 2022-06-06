package pt.ist.socialsoftware.mono2micro.controller;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithm;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithmFactory;
import pt.ist.socialsoftware.mono2micro.domain.source.Source;
import pt.ist.socialsoftware.mono2micro.domain.strategy.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.AccessesSciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.RequestDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/strategy/{strategyName}")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/createDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDecomposition(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@RequestBody RequestDto requestDto
	) {
		logger.debug("createDecomposition");

		try {
			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());
			clusteringAlgorithm.createDecomposition(strategy, requestDto);

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
			RequestDto requestDto;

			switch (type) {
				case ACCESSES_SCIPY:
					requestDto = new AccessesSciPyRequestDto(expertName, expertFile);
					break;
				default:
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());
			clusteringAlgorithm.createDecomposition(strategy, requestDto);
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
		@PathVariable String strategyName
	) {
		logger.debug("getDecompositions");

		try {
			return new ResponseEntity<>(
				codebaseManager.getStrategyDecompositions(
					codebaseName,
					strategyName
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
		@PathVariable String decompositionName
	) {
		logger.debug("getDecomposition");

		try {
			return new ResponseEntity<>(
				codebaseManager.getStrategyDecomposition(
					codebaseName,
					STRATEGIES_FOLDER,
					strategyName,
					decompositionName
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
			Strategy strategy = codebaseManager.getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);
			strategy.removeDecompositionName(decompositionName);
			codebaseManager.writeCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategy);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getLocalTransactionsGraphForFunctionality", method = RequestMethod.GET)
	public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getFunctionalityLocalTransactionsGraph(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@RequestParam String functionalityName
	) {
		logger.debug("getFunctionalityLocalTransactionsGraph");

		try {

			// TODO: abstract strategy call to make this a generic function, probably needs decompositions with subclasses
			AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) codebaseManager.getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);

			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
				codebaseName,
				STRATEGIES_FOLDER,
				strategyName,
				decompositionName
			);

			Source source = codebaseManager.getCodebaseSource(codebaseName, ACCESSES);

			DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
					.createLocalTransactionGraphFromScratch(
							source.getInputFilePath(),
							strategy.getTracesMaxLimit(),
							strategy.getTraceType(),
							decomposition.getEntityIDToClusterID());

			return new ResponseEntity<>(
				Utils.getSerializableLocalTransactionsGraph(functionalityLocalTransactionsGraph),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getEdgeWeights", method = RequestMethod.GET)
	public ResponseEntity<String> getEdgeWeights(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName
	) {
		logger.debug("getEdgeWeights");

		try {
			JSONArray copheneticDistances = codebaseManager.getCopheneticDistances(codebaseName, strategyName);
			JSONArray entities = codebaseManager.getSimilarityMatrix(codebaseName, strategyName, "similarityMatrix.json").getJSONArray("entities");
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName);

			JSONArray edgesJSON = new JSONArray();
			int k = 0;
			for (int i = 0; i < entities.length(); i++) {
				for (int j = i + 1; j < entities.length(); j++) {
					int e1ID = entities.getInt(i);
					int e2ID = entities.getInt(j);

					JSONArray functionalitiesJSON = new JSONArray();

					for (Functionality functionality : decomposition.getFunctionalities().values())
						if (functionality.getEntities().containsKey((short) e1ID) && functionality.getEntities().containsKey((short) e2ID))
							functionalitiesJSON.put(functionality.getName());

					if (functionalitiesJSON.length() != 0) {
						JSONObject edgeJSON = new JSONObject();
						edgeJSON.put("e1ID", e1ID);
						edgeJSON.put("e2ID", e2ID);
						edgeJSON.put("dist", copheneticDistances.getDouble(k));
						edgeJSON.put("functionalities", functionalitiesJSON);
						edgesJSON.put(edgeJSON);
					}
					k++;
				}
			}
			return new ResponseEntity<>(edgesJSON.toString(), HttpStatus.OK);

		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getGraphPositions", method = RequestMethod.GET)
	public ResponseEntity<String> getGraphPositions(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName
	) {
		logger.debug("getGraphPositions");

		try {
			String graphPositions = codebaseManager.getGraphPositions(codebaseName, strategyName, decompositionName);

			if (graphPositions == null)
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			else return new ResponseEntity<>(graphPositions, HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	// Permanently saves clusters and entities' positions
	@RequestMapping(value = "/decomposition/{decompositionName}/saveGraphPositions", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> saveGraphPositions(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName,
			@RequestBody String graphPositions
	) {
		logger.debug("saveGraphPositions");

		try {
			codebaseManager.saveGraphPositions(codebaseName, strategyName, decompositionName, graphPositions);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/deleteGraphPositions", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraphPositions(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName
	) {
		logger.debug("deleteGraphPositions");

		try {
			codebaseManager.deleteGraphPositions(codebaseName, strategyName, decompositionName);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}