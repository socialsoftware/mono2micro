package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Map;

@RestController
@RequestMapping(value = "/mono2micro")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

	@Autowired
	DecompositionService decompositionService;

	@RequestMapping(value = "/similarity/createDecomposition", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDecomposition(
			@RequestBody DecompositionDto decompositionDto
	) {
		logger.debug("createDecomposition");

		try {
			decompositionService.createDecomposition(decompositionDto);
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
				DecompositionDtoFactory.getFactory().getDecompositionDto(decompositionService.getDecomposition(decompositionName)),
				HttpStatus.OK
			);

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

	@RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterName}/merge", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> mergeClusters(
			@PathVariable String decompositionName,
			@PathVariable String clusterName,
			@RequestParam String otherClusterName,
			@RequestParam String newName
	) {
		logger.debug("mergeClusters");
		try {
			decompositionService.mergeClusters(decompositionName, clusterName, otherClusterName, newName);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterName}/rename", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> renameCluster(
			@PathVariable String decompositionName,
			@PathVariable String clusterName,
			@RequestParam String newName
	) {
		logger.debug("renameCluster");
		try {
			decompositionService.renameCluster(decompositionName, clusterName, newName);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterName}/split", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> splitCluster(
			@PathVariable String decompositionName,
			@PathVariable String clusterName,
			@RequestParam String newName,
			@RequestParam String entities
	) {
		logger.debug("splitCluster");
		try {
			decompositionService.splitCluster(decompositionName, clusterName, newName, entities);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterName}/transferEntities", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Cluster>> transferEntities(
			@PathVariable String decompositionName,
			@PathVariable String clusterName,
			@RequestParam String toClusterName,
			@RequestParam String entities
	) {
		logger.debug("transferEntities");
		try {
			decompositionService.transferEntities(decompositionName, clusterName, toClusterName, entities);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}