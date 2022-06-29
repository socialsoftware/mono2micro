package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/strategy/{strategyName}/decomposition/{decompositionName}")
public class ClusterController {

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/cluster/{clusterNameID}/merge", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> mergeClusters(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterNameID,
		@RequestParam Short otherClusterID,
		@RequestParam String newName
	) {
		logger.debug("mergeClusters");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			decomposition.mergeClusters(
				clusterNameID,
				otherClusterID,
				newName
			);
			decomposition.setOutdated(true);

			codebaseManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/rename", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> renameCluster(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName
	) {
		logger.debug("renameCluster");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			decomposition.renameCluster(
				clusterID,
				newName
			);

			codebaseManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/split", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> splitCluster(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName,
		@RequestParam String entities
	) {
		logger.debug("splitCluster");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			decomposition.splitCluster(
				clusterID,
				newName,
				entities.split(",")
			);
			decomposition.setOutdated(true);

			codebaseManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/transferEntities", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> transferEntities(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam Short toClusterID,
		@RequestParam String entities
	) {
		logger.debug("transferEntities");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			decomposition.transferEntities(
				clusterID,
				toClusterID,
				entities.split(",")
			);
			decomposition.setOutdated(true);

			codebaseManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/formCluster", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> formCluster(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName,
			@RequestParam String newName,
			@RequestBody Map<Short, List<Short>> entities // <clusterId, entitiesIds[]> This additional information might be useful in the future
	) {
		logger.debug("formCluster");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			decomposition.formCluster(
					newName,
					entities.values().stream().flatMap(Collection::stream).map(Object::toString).toArray(String[]::new)
			);
			decomposition.setOutdated(true);

			codebaseManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/getFunctionalitiesAndFunctionalitiesClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getFunctionalitiesAndFunctionalitiesClusters(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName
	) {
		logger.debug("getFunctionalitiesAndFunctionalitiesClusters");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
				codebaseName,
				STRATEGIES_FOLDER,
				strategyName,
				decompositionName
			);

			decomposition.updateOutdatedFunctionalitiesAndMetrics();

			Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
					decomposition.getEntityIDToClusterID(),
					decomposition.getClusters(),
					decomposition.getFunctionalities().values()
			);

			Map<String, Object> response = new HashMap<>();
			response.put("functionalities", decomposition.getFunctionalities());
			response.put("functionalitiesClusters", functionalitiesClusters);

			return new ResponseEntity<>(
				response,
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/getClustersAndClustersFunctionalities", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getClustersAndClustersFunctionalities(
		@PathVariable String codebaseName,
		@PathVariable String strategyName,
		@PathVariable String decompositionName
	) {
		logger.debug("getClustersAndClustersFunctionalities");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) codebaseManager.getStrategyDecomposition(
				codebaseName,
				STRATEGIES_FOLDER,
				strategyName,
				decompositionName
			);

			decomposition.updateOutdatedFunctionalitiesAndMetrics();

			Map<Short, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(
					decomposition.getEntityIDToClusterID(),
					decomposition.getClusters(),
					decomposition.getFunctionalities().values()
			);

			Map<String, Object> response = new HashMap<>();
			response.put("clusters", decomposition.getClusters());
			response.put("clustersFunctionalities", clustersFunctionalities);

			return new ResponseEntity<>(
				response,
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}