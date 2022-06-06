package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithm;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithmFactory;
import pt.ist.socialsoftware.mono2micro.domain.similarityGenerator.SimilarityGenerator;
import pt.ist.socialsoftware.mono2micro.domain.similarityGenerator.SimilarityGeneratorFactory;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class StrategyController {

    private static final Logger logger = LoggerFactory.getLogger(Strategy.class);
    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/strategy/createStrategy", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createStrategy(
		@PathVariable String codebaseName,
		@RequestBody Strategy strategyInformation
	) {
		logger.debug("Create Strategy");

		try {
			List<Strategy> strategies = codebaseManager.getCodebaseStrategies(codebaseName, STRATEGIES_FOLDER, Collections.singletonList(strategyInformation.getType()));
			if (strategies.stream().anyMatch(strategy -> strategy.equals(strategyInformation)))
				return new ResponseEntity<>(HttpStatus.CREATED);

			Strategy strategy = codebaseManager.createCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyInformation);

			SimilarityGenerator similarityGenerator = SimilarityGeneratorFactory.getFactory().getSimilarityGenerator(strategy.getType());
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());

			similarityGenerator.createSimilarityMatrix(strategy);
			clusteringAlgorithm.createDendrogram(strategy); //Not every strategy creates a dedrogram, in that case leave this method empty in the implementation

			codebaseManager.writeCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategy);

			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/strategy/{strategyName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteStrategy(
			@PathVariable String codebaseName,
			@PathVariable String strategyName
	) {
		logger.debug("Delete Strategy");

		try {
			codebaseManager.deleteCodebaseStrategy(codebaseName, strategyName);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}



	// Specific to accesses similarity generator with SciPy clustering algorithm
	@RequestMapping(value = "/strategy/{strategyName}/image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDendrogramImage(
			@PathVariable String codebaseName,
			@PathVariable String strategyName
	) {
		logger.debug("getDendrogramImage");

		try {
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_PNG)
					.body(codebaseManager.getDendrogramImage(codebaseName, strategyName));

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/strategy/{strategyName}/getSimilarityMatrix", method = RequestMethod.GET)
	public ResponseEntity<String> getSimilarityMatrix(
			@PathVariable String codebaseName,
			@PathVariable String strategyName
	) {
		logger.debug("getSimilarityMatrix");

		try {
			return new ResponseEntity<>(codebaseManager.getSimilarityMatrixAsString(codebaseName, strategyName, "similarityMatrix.json"), HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}