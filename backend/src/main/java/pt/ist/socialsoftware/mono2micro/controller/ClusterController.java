package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/graph/{graphName}")
public class ClusterController {

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/cluster/{clusterName}/merge", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> mergeClusters(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName,
		@PathVariable String clusterName,
		@RequestParam String otherCluster,
		@RequestParam String newName
	) {
		logger.debug("mergeClusters");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).mergeClusters(clusterName, otherCluster, newName);
			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterName}/rename", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> renameCluster(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName,
		@PathVariable String clusterName,
		@RequestParam String newName
	) {
		logger.debug("renameCluster");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).renameCluster(clusterName, newName);
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

	@RequestMapping(value = "/cluster/{clusterName}/split", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> splitCluster(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName,
		@PathVariable String clusterName,
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
			codebase.getDendrogram(dendrogramName).getGraph(graphName).splitCluster(clusterName, newName, entities.split(","));
			codebaseManager.writeCodebase(codebase);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/cluster/{clusterName}/transferEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> transferEntities(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName,
		@PathVariable String clusterName,
		@RequestParam String toCluster,
		@RequestParam String entities
	) {
		logger.debug("transferEntities");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			// FIXME Each dendrogram directory would have a folder for controllers and another for clusters
			// FIXME Each controller and cluster would have its own json file

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).transferEntities(clusterName, toCluster, entities.split(","));
			codebaseManager.writeCodebase(codebase);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/controllerClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Cluster>>> getControllerClusters(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName
	) {
		logger.debug("getControllerClusters");

		try {
			return new ResponseEntity<>(
				codebaseManager.getGraphWithControllersAndClustersWithFields(
					codebaseName,
					dendrogramName,
					graphName,
					new HashSet<String>() {{ add("name"); add("entities"); }},
					new HashSet<String>() {{ add("name"); add("entities"); }}
				)
					.getControllerClusters(),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/clusterControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Controller>>> getClusterControllers(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName
	) {
		logger.debug("getClusterControllers");

		try {
			return new ResponseEntity<>(
				codebaseManager.getGraphWithControllersAndClustersWithFields(
					codebaseName,
					dendrogramName,
					graphName,
					new HashSet<String>() {{ add("name"); add("entities"); }},
					new HashSet<String>() {{ add("name"); add("entities"); }}
				)
					.getClusterControllers(),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}