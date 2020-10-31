package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

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
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Graph graph = dendrogram.getGraph(graphName);

			graph.mergeClusters(
				clusterName,
				otherCluster,
				newName
			);

			graph.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				graph.getEntityIDToClusterName()
			));

			graph.calculateMetrics(
				codebase,
				dendrogram.getProfile(),
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTypeOfTraces()
			);

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
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Graph graph = dendrogram.getGraph(graphName);

			graph.renameCluster(
				clusterName,
				newName
			);

			// it should not be necessary to do this due to just a renaming
			// but for safety i'll keep the existent behaviour
			graph.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				graph.getEntityIDToClusterName()
			));

			// it should not be necessary to recalculate metrics due to just a renaming
			// but for safety i'll keep the existent behaviour
			graph.calculateMetrics(
				codebase,
				dendrogram.getProfile(),
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTypeOfTraces()
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
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Graph graph = dendrogram.getGraph(graphName);

			graph.splitCluster(
				clusterName,
				newName,
				entities.split(",")
			);

			graph.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				graph.getEntityIDToClusterName()
			));

			graph.calculateMetrics(
				codebase,
				dendrogram.getProfile(),
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTypeOfTraces()
			);

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
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
			Graph graph = dendrogram.getGraph(graphName);

			graph.transferEntities(
				clusterName,
				toCluster,
				entities.split(",")
			);

			graph.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				graph.getEntityIDToClusterName()
			));

			graph.calculateMetrics(
				codebase,
				dendrogram.getProfile(),
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTypeOfTraces()
			);

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
		@PathVariable String graphName
	) {
		logger.debug("getControllersClusters");

		try {

			String dendrogramProfile = codebaseManager.getCodebaseDendrogramWithFields(
				codebaseName,
				dendrogramName,
				new HashSet<String>() {{ add("profile"); }}
			).getProfile();

			Codebase codebase = codebaseManager.getCodebaseWithFields(
				codebaseName,
				new HashSet<String>() {{ add("profiles"); add("controllers"); }}
			);

			Set<String> profileControllers = codebase.getProfile(dendrogramProfile);

			Graph graph = codebaseManager.getDendrogramGraphWithFields(
				codebaseName,
				dendrogramName,
				graphName,
				new HashSet<String>() {{ add("clusters"); add("controllers"); }}
			);

			Utils.GetControllersClustersAndClustersControllersResult result =
				Utils.getControllersClustersAndClustersControllers(
					profileControllers,
					(List<Cluster>) graph.getClusters().values(),
					graph.getControllers()
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
	public ResponseEntity<Map<String, Set<Controller>>> getClustersControllers(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@PathVariable String graphName
	) {
		logger.debug("getClustersControllers");

		try {
			String dendrogramProfile = codebaseManager.getCodebaseDendrogramWithFields(
				codebaseName,
				dendrogramName,
				new HashSet<String>() {{ add("profile"); }}
			).getProfile();

			Codebase codebase = codebaseManager.getCodebaseWithFields(
				codebaseName,
				new HashSet<String>() {{ add("profiles"); }}
			);

			Set<String> profileControllers = codebase.getProfile(dendrogramProfile);

			Graph graph = codebaseManager.getDendrogramGraphWithFields(
				codebaseName,
				dendrogramName,
				graphName,
				new HashSet<String>() {{ add("clusters"); add("controllers"); }}
			);

			Utils.GetControllersClustersAndClustersControllersResult result =
				Utils.getControllersClustersAndClustersControllers(
					profileControllers,
					(List<Cluster>) graph.getClusters().values(),
					graph.getControllers()
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
}