package pt.ist.socialsoftware.mono2micro.controller;

import java.util.List;
import java.util.Map;

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
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.manager.DendrogramManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/graph/{graphName}")
public class ClusterController {

	private static Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private CodebaseManager codebaseManager = new CodebaseManager();


	@RequestMapping(value = "/cluster/{clusterName}/merge", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> mergeClusters(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String otherCluster, @RequestParam String newName) {
		logger.debug("mergeClusters {} with {}", clusterName, otherCluster);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		codebase.getDendrogram(dendrogramName).mergeClusters(graphName, clusterName, otherCluster, newName);
		codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
	}


	@RequestMapping(value = "/cluster/{clusterName}/rename", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> renameCluster(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String newName) {
		logger.debug("renameCluster {}", clusterName);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		boolean success = codebase.getDendrogram(dendrogramName).renameCluster(graphName, clusterName, newName);
		if (success) {
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/cluster/{clusterName}/split", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> splitCluster(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String newName, @RequestParam String entities) {
		logger.debug("splitCluster: {}", clusterName);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		codebase.getDendrogram(dendrogramName).splitCluster(graphName, clusterName, newName, entities.split(","));
		codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
	}


	@RequestMapping(value = "/cluster/{clusterName}/transferEntities", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> transferEntities(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName, @PathVariable String clusterName, @RequestParam String toCluster, @RequestParam String entities) {
		logger.debug("transferEntities: {}", clusterName);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		codebase.getDendrogram(dendrogramName).transferEntities(graphName, clusterName, toCluster, entities.split(","));
		codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
	}


	@RequestMapping(value = "/controllerClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Cluster>>> getControllerClusters(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("getControllerClusters: in graph {}", graphName);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		return new ResponseEntity<Map<String,List<Cluster>>>(codebase.getDendrogram(dendrogramName).getControllerClusters(graphName), HttpStatus.OK);
	}


	@RequestMapping(value = "/clusterControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Controller>>> getClusterControllers(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String graphName) {
		logger.debug("getClusterControllers: in graph {}", graphName);

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		return new ResponseEntity<Map<String,List<Controller>>>(codebase.getDendrogram(dendrogramName).getClusterControllers(graphName), HttpStatus.OK);
	}
}