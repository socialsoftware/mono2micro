package pt.ist.socialsoftware.mono2micro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/decomposition/{decompositionName}")
public class ClusterController {

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/cluster/{clusterNameID}/merge", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> mergeClusters(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterNameID,
		@RequestParam Short otherClusterID,
		@RequestParam String newName
	) {
		logger.debug("mergeClusters");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Decomposition decomposition = dendrogram.getDecomposition(decompositionName);

			decomposition.mergeClusters(
				clusterNameID,
				otherClusterID,
				newName
			);

			decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				dendrogram.getProfile(),
				decomposition.getEntityIDToClusterID()
			));

			decomposition.calculateMetrics(
				codebase,
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTraceType(),
					false);

			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/rename", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> renameCluster(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName
	) {
		logger.debug("renameCluster");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Decomposition decomposition = dendrogram.getDecomposition(decompositionName);

			decomposition.renameCluster(
				clusterID,
				newName
			);

			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/split", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> splitCluster(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam String newName,
		@RequestParam String entities
	) {
		logger.debug("splitCluster");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Decomposition decomposition = dendrogram.getDecomposition(decompositionName);

			decomposition.splitCluster(
				clusterID,
				newName,
				entities.split(",")
			);

			decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				dendrogram.getProfile(),
				decomposition.getEntityIDToClusterID()
			));

			decomposition.calculateMetrics(
				codebase,
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTraceType(),
					false);

			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterID}/transferEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> transferEntities(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName,
		@PathVariable Short clusterID,
		@RequestParam Short toClusterID,
		@RequestParam String entities
	) {
		logger.debug("transferEntities");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Decomposition decomposition = dendrogram.getDecomposition(decompositionName);

			decomposition.transferEntities(
				clusterID,
				toClusterID,
				entities.split(",")
			);

			decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				dendrogram.getProfile(),
				decomposition.getEntityIDToClusterID()
			));

			decomposition.calculateMetrics(
				codebase,
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTraceType(),
					false);

			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/controllersClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Set<Cluster>>> getControllersClusters(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName
	) {
		logger.debug("getControllersClusters");

		try {
			Decomposition decomposition = codebaseManager.getDendrogramDecompositionWithFields(
				codebaseName,
				dendrogramName,
				decompositionName,
				new HashSet<String>() {{ add("clusters"); add("controllers"); }}
			);

			Utils.GetControllersClustersAndClustersControllersResult result =
				Utils.getControllersClustersAndClustersControllers(
					decomposition.getClusters().values(),
					decomposition.getControllers().values()
				);

			return new ResponseEntity<>(
				result.controllersClusters,
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/clustersControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<Short, Set<Controller>>> getClustersControllers(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String decompositionName
	) {
		logger.debug("getClustersControllers");

		try {
			Decomposition decomposition = codebaseManager.getDendrogramDecompositionWithFields(
				codebaseName,
				dendrogramName,
				decompositionName,
				new HashSet<String>() {{ add("clusters"); add("controllers"); }}
			);

			Utils.GetControllersClustersAndClustersControllersResult result =
				Utils.getControllersClustersAndClustersControllers(
					decomposition.getClusters().values(),
					decomposition.getControllers().values()
				);

			return new ResponseEntity<>(
				result.clustersControllers,
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/clustersCommits", method = RequestMethod.GET)
	public ResponseEntity<String> getClustersCommits(
			@PathVariable String codebaseName,
			@PathVariable String dendrogramName,
			@PathVariable String decompositionName
	) {
		logger.debug("getClustersCommits");

		try {
			Decomposition decomposition = codebaseManager.getDendrogramDecompositionWithFields(
					codebaseName,
					dendrogramName,
					decompositionName,
					new HashSet<String>() {{ add("clusters"); add("controllers"); }}
			);
			ObjectMapper objectMapper = new ObjectMapper();

			// Open and extract the commit cluster json file
			InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/" + decompositionName + "/commit-clusters.json");
			String commitClusters = IOUtils.toString(is, "UTF-8");
			is.close();

			return new ResponseEntity<>(
					commitClusters,
					HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}