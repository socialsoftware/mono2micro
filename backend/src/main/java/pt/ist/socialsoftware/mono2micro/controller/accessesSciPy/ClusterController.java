package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.service.AccessesSciPyHistoryService;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

@RestController
@RequestMapping(value = "/mono2micro/decomposition/{decompositionName}")
public class ClusterController {

	@Autowired
	AccessesSciPyHistoryService accessesSciPyHistoryService;

	@Autowired
	AccessesSciPyDecompositionService decompositionService;

	@Autowired
	AccessesSciPyDecompositionRepository decompositionRepository;

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

	@RequestMapping(value = "/cluster/{clusterNameID}/merge", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> mergeClusters(
		@PathVariable String decompositionName,
		@PathVariable Short clusterNameID,
		@RequestParam Short otherClusterID,
		@RequestParam String newName
	) {
		logger.debug("mergeClusters");

		try {
		    AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

			MergeHistoryEntry historyEntry = new MergeHistoryEntry(decomposition, clusterNameID, otherClusterID, newName);

			decomposition.mergeClusters(clusterNameID, otherClusterID, newName);
			decomposition.setOutdated(true);

			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
			decompositionRepository.save(decomposition);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/rename", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> renameCluster(
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName
	) {
		logger.debug("renameCluster");

		try {
			AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

			RenameHistoryEntry historyEntry = new RenameHistoryEntry(decomposition, clusterID, newName);

			decomposition.renameCluster(clusterID, newName);
			decompositionRepository.save(decomposition);
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
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName,
		@RequestParam String entities
	) {
		logger.debug("splitCluster");

		try {
			AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

			SplitHistoryEntry historyEntry = new SplitHistoryEntry(decomposition, clusterID, newName, entities);

			decomposition.splitCluster(clusterID, newName, entities.split(","));
			decomposition.setOutdated(true);

			decompositionRepository.save(decomposition);
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
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam Short toClusterID,
		@RequestParam String entities
	) {
		logger.debug("transferEntities");

		try {
			AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

			TransferHistoryEntry historyEntry = new TransferHistoryEntry(decomposition, clusterID, toClusterID, entities);

			decomposition.transferEntities(clusterID, toClusterID, entities.split(","));
			decomposition.setOutdated(true);

			decompositionRepository.save(decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/formCluster", method = RequestMethod.POST)
	public ResponseEntity<Map<Short, Cluster>> formCluster(
			@PathVariable String decompositionName,
			@RequestParam String newName,
			@RequestBody Map<Short, List<Short>> entities
	) {
		logger.debug("formCluster");

		try {
			AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

			FormClusterHistoryEntry historyEntry = new FormClusterHistoryEntry(decomposition, newName, entities);

			decomposition.formCluster(newName, entities.values().stream().flatMap(Collection::stream).map(Object::toString).toArray(String[]::new));
			decomposition.setOutdated(true);

			decompositionRepository.save(decomposition);
			accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
			return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}