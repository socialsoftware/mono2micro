package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/mono2micro")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

	@Autowired
	DecompositionService decompositionService;

	@RequestMapping(value = "/decomposition/getDecompositionTypes", method = RequestMethod.GET)
	public ResponseEntity<String[]> getDecompositionTypes() {
		logger.debug("getDecompositionTypes");

		try {
			return new ResponseEntity<>(decompositionService.getDecompositionTypes(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionType}/getRequiredRepresentations", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> getRequiredRepresentations(
			@PathVariable String decompositionType
	) {
		logger.debug("getRequiredRepresentations");

		try {
			return new ResponseEntity<>(decompositionService.getRequiredRepresentations(decompositionType), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/similarity/createDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDecomposition(
			@RequestBody DecompositionRequest decompositionRequest
	) {
		logger.debug("createDecomposition");

		try {
			decompositionService.createDecomposition(decompositionRequest);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/similarity/{similarityName}/createExpertDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createExpertDecomposition(
			@PathVariable String similarityName,
			@RequestParam String expertName,
			@RequestParam Optional<MultipartFile> expertFile
	) {
		logger.debug("createExpertDecomposition");

		try {
			decompositionService.createExpertDecomposition(similarityName, expertName, expertFile);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}", method = RequestMethod.GET)
	public ResponseEntity<DecompositionDto> getDecomposition(
		@PathVariable String decompositionName
	) {
		logger.debug("getDecomposition");

		try {
			return new ResponseEntity<>(
				DecompositionDtoFactory.getDecompositionDto(decompositionService.getDecomposition(decompositionName)),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/getClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Cluster>> getClusters(
			@PathVariable String decompositionName
	) {
		logger.debug("getClusters");

		try {
			return new ResponseEntity<>(decompositionService.getDecomposition(decompositionName).getClusters(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDecomposition(
		@PathVariable String decompositionName
	) {
		logger.debug("deleteDecomposition");

		try {
			decompositionService.deleteSingleDecomposition(decompositionName);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/decomposition/{decompositionName}/merge", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> mergeClusters(
			@PathVariable String decompositionName,
			@RequestBody MergeOperation operation
	) {
		logger.debug("mergeClusters");
		try {
			decompositionService.mergeClustersOperation(decompositionName, operation);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/rename", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> renameCluster(
			@PathVariable String decompositionName,
			@RequestBody RenameOperation operation
	) {
		logger.debug("renameCluster");
		try {
			decompositionService.renameClusterOperation(decompositionName, operation);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/split", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> splitCluster(
			@PathVariable String decompositionName,
			@RequestBody SplitOperation operation
	) {
		logger.debug("splitCluster");
		try {
			decompositionService.splitClusterOperation(decompositionName, operation);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/transfer", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> transferEntities(
			@PathVariable String decompositionName,
			@RequestBody TransferOperation operation
	) {
		logger.debug("transferEntities");
		try {
			decompositionService.transferEntitiesOperation(decompositionName, operation);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/decomposition/{decompositionName}/formCluster", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> formCluster(
			@PathVariable String decompositionName,
			@RequestBody FormClusterOperation operation
	) {
		logger.debug("formCluster");
		try {
			decompositionService.formClusterOperation(decompositionName, operation);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/snapshotDecomposition", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> snapshotDecomposition(
			@PathVariable String decompositionName
	) {
		logger.debug("snapshotDecomposition");

		try {
			decompositionService.snapshotDecomposition(decompositionName);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/{representationInfo}/getEdgeWeights", method = RequestMethod.GET)
	public ResponseEntity<String> getEdgeWeights(
			@PathVariable String decompositionName,
			@PathVariable String representationInfo
	) {
		logger.debug("getEdgeWeights");

		try {
			return new ResponseEntity<>(decompositionService.getEdgeWeights(decompositionName, representationInfo), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/{representationInfo}/getSearchItems", method = RequestMethod.GET)
	public ResponseEntity<String> getSearchItems(
			@PathVariable String decompositionName,
			@PathVariable String representationInfo
	) {
		logger.debug("getSearchItems");

		try {
			return new ResponseEntity<>(decompositionService.getSearchItems(decompositionName, representationInfo), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/update", method = RequestMethod.GET)
	public ResponseEntity<DecompositionDto> update(
			@PathVariable String decompositionName
	) {
		logger.debug("update");

		try {
			return new ResponseEntity<>(DecompositionDtoFactory.getDecompositionDto(
					decompositionService.updateDecomposition(decompositionName)), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}