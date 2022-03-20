package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithm;
import pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm.ClusteringAlgorithmFactory;
import pt.ist.socialsoftware.mono2micro.domain.similarityGenerators.SimilarityGenerator;
import pt.ist.socialsoftware.mono2micro.domain.similarityGenerators.SimilarityGeneratorFactory;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.util.List;

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
			List<Strategy> strategies = codebaseManager.getCodebaseStrategies(codebaseName, strategyInformation.getType());
			if (strategies.stream().anyMatch(strategy -> strategy.equals(strategyInformation)))
				return new ResponseEntity<>(HttpStatus.CREATED);

			Strategy strategy = codebaseManager.createCodebaseStrategy(codebaseName, strategyInformation);

			SimilarityGenerator similarityGenerator = SimilarityGeneratorFactory.getFactory().getSimilarityGenerator(strategy.getType());
			ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());

			similarityGenerator.createSimilarityMatrix(strategy);
			clusteringAlgorithm.createDendrogram(strategy); //Not every strategy creates a dedrogram, in that case leave this method empty in the implementation

			codebaseManager.writeCodebaseStrategy(codebaseName, strategy);

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
}