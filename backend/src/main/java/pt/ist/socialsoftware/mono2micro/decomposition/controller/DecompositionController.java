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
import javax.servlet.http.HttpServletResponse;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(value = "/mono2micro")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

	@Autowired
	DecompositionService decompositionService;

	@PostMapping(value = "/similarity/createDecomposition")
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

	@PostMapping(value = "/similarity/{similarityName}/createExpertDecomposition")
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

	@GetMapping(value = "/decomposition/{decompositionName}")
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

	@GetMapping(value = "/decomposition/{decompositionName}/getClusters")
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

	@GetMapping(value = "/decomposition/{decompositionName}/export")
	public void exportDecomposition(
			HttpServletResponse response,
			@PathVariable String decompositionName
	) {
		logger.debug("exportDecomposition");

		try {
			response.setHeader("Content-Disposition", "attachment; filename=m2m_decomposition_data.zip");
			response.setContentType("application/zip");
			decompositionService.exportDecompositionToContextMapper(decompositionName, new ZipOutputStream(response.getOutputStream()));
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();

		} catch (NoSuchFileException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/decomposition/{decompositionName}/delete")
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


	@PostMapping(value = "/decomposition/{decompositionName}/merge")
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

	@PostMapping(value = "/decomposition/{decompositionName}/rename")
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

	@PostMapping(value = "/decomposition/{decompositionName}/split")
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

	@PostMapping(value = "/decomposition/{decompositionName}/transfer")
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


	@PostMapping(value = "/decomposition/{decompositionName}/formCluster")
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

	@GetMapping(value = "/decomposition/{decompositionName}/snapshotDecomposition")
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

	@GetMapping(value = "/decomposition/{decompositionName}/{representationInfo}/getEdgeWeights")
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

	@GetMapping(value = "/decomposition/{decompositionName}/{representationInfo}/getSearchItems")
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

	@GetMapping(value = "/decomposition/{decompositionName}/update")
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