package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.service.AccessesSciPyHistoryService;
import pt.ist.socialsoftware.mono2micro.fileManager.FileManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/strategy/{strategyName}/decomposition/{decompositionName}")
public class ClusterController {

	@Autowired
	AccessesSciPyHistoryService accessesSciPyHistoryService;

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private final FileManager fileManager = FileManager.getInstance();

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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			MergeHistoryEntry historyEntry = new MergeHistoryEntry(decomposition, clusterNameID, otherClusterID, newName);

			decomposition.mergeClusters(
				clusterNameID,
				otherClusterID,
				newName
			);
			decomposition.setOutdated(true);

			fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			RenameHistoryEntry historyEntry = new RenameHistoryEntry(decomposition, clusterID, newName);

			decomposition.renameCluster(
				clusterID,
				newName
			);

			fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			SplitHistoryEntry historyEntry = new SplitHistoryEntry(decomposition, clusterID, newName, entities);

			decomposition.splitCluster(
				clusterID,
				newName,
				entities.split(",")
			);
			decomposition.setOutdated(true);

			fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			TransferHistoryEntry historyEntry = new TransferHistoryEntry(decomposition, clusterID, toClusterID, entities);

			decomposition.transferEntities(
				clusterID,
				toClusterID,
				entities.split(",")
			);
			decomposition.setOutdated(true);

			fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
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
			@RequestBody Map<Short, List<Short>> entities
	) {
		logger.debug("formCluster");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
			);

			FormClusterHistoryEntry historyEntry = new FormClusterHistoryEntry(decomposition, newName, entities);

			decomposition.formCluster(
					newName,
					entities.values().stream().flatMap(Collection::stream).map(Object::toString).toArray(String[]::new)
			);
			decomposition.setOutdated(true);

			fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/undoOperation", method = RequestMethod.GET)
	public ResponseEntity<Map<Short, Cluster>> undoOperation(
			@PathVariable String codebaseName,
			@PathVariable String strategyName,
			@PathVariable String decompositionName
	) {
		logger.debug("undoOperation");

		try {
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
					codebaseName,
					STRATEGIES_FOLDER,
					strategyName,
					decompositionName
			);

			accessesSciPyHistoryService.undoOperation(decomposition);

			return new ResponseEntity<>(
					decomposition.getClusters(),
					HttpStatus.OK
			);

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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
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
			AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
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