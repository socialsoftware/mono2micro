package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
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

	private static Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/cluster/{clusterName}/merge", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> mergeClusters(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String otherCluster, @RequestParam String newName) {
		logger.debug("mergeClusters");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).mergeClusters(clusterName, otherCluster, newName);
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/cluster/{clusterName}/rename", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> renameCluster(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String newName) {
		logger.debug("renameCluster");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).renameCluster(clusterName, newName);
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/cluster/{clusterName}/split", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> splitCluster(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String newName, @RequestParam String entities) {
		logger.debug("splitCluster");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).splitCluster(clusterName, newName, entities.split(","));
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/cluster/{clusterName}/transferEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> transferEntities(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String toCluster, @RequestParam String entities) {
		logger.debug("transferEntities");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.getDendrogram(dendrogramName).getGraph(graphName).transferEntities(clusterName, toCluster, entities.split(","));
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/controllerClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Cluster>>> getControllerClusters(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("getControllerClusters");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getGraph(graphName).getControllerClusters(), HttpStatus.OK);
	}


	@RequestMapping(value = "/clusterControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Controller>>> getClusterControllers(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("getClusterControllers");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getGraph(graphName).getClusterControllers(), HttpStatus.OK);
	}
}