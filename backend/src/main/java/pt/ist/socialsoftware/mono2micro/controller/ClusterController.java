package pt.ist.socialsoftware.mono2micro.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.DendrogramManager;

@RestController
@RequestMapping(value = "/mono2micro")
public class ClusterController {

	private static Logger logger = LoggerFactory.getLogger(ClusterController.class);

	private DendrogramManager dendrogramManager = new DendrogramManager();


	@RequestMapping(value = "/mergeClusters", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> mergeClusters(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("cluster1") String cluster1, @RequestParam("cluster2") String cluster2,
			@RequestParam("newName") String newName) {
		logger.debug("mergeClusters {} with {}", cluster1, cluster2);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.mergeClusters(graphName, cluster1, cluster2, newName);
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}


	@RequestMapping(value = "/renameCluster", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> renameCluster(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("clusterName") String clusterName, @RequestParam("newName") String newName) {
		logger.debug("renameCluster {}", clusterName);
		
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.renameCluster(graphName, clusterName, newName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
		} else {
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/splitCluster", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> splitCluster(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("clusterName") String clusterName, @RequestParam("newName") String newName, 
			@RequestParam("entities") String entities) {
		logger.debug("splitCluster: {}", clusterName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.splitCluster(graphName, clusterName, newName, entities.split(","));
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}


	@RequestMapping(value = "/transferEntities", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> transferEntities(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("fromCluster") String fromCluster, @RequestParam("toCluster") String toCluster, 
			@RequestParam("entities") String entities) {
		logger.debug("transferEntities: {}", fromCluster);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.transferEntities(graphName, fromCluster, toCluster, entities.split(","));
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}


	@RequestMapping(value = "/controllerClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Cluster>>> getControllerClusters(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getControllerClusters: in graph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Map<String,List<Cluster>>>(dend.getControllerClusters(graphName), HttpStatus.OK);
	}


	@RequestMapping(value = "/clusterControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Controller>>> getClusterControllers(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getClusterControllers: in graph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Map<String,List<Controller>>>(dend.getClusterControllers(graphName), HttpStatus.OK);
	}


	@RequestMapping(value = "/controllers", method = RequestMethod.GET)
	public ResponseEntity<List<Controller>> getControllers(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("getControllers");
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<List<Controller>>(dend.getControllers(), HttpStatus.OK);
	}


	@RequestMapping(value = "/controller", method = RequestMethod.GET)
	public ResponseEntity<Controller> getController(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("controllerName") String controllerName) {
		logger.debug("getController");
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Controller>(dend.getController(controllerName), HttpStatus.OK);
	}
}