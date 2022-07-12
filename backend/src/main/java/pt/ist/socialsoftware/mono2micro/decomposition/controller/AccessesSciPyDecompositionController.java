package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Functionality;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.naming.NameAlreadyBoundException;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSciPyDecompositionController {
    private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    @Autowired
    AccessesSciPyDecompositionService decompositionService;

    @RequestMapping(value = "/strategy/{strategyName}/createAccessesSciPyDecomposition", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createDecomposition(
            @PathVariable String strategyName,
            @RequestParam String cutType,
            @RequestParam float cutValue
    ) {
        logger.debug("createDecomposition");

        try {
            decompositionService.createDecomposition(strategyName, cutType, cutValue);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/strategy/{strategyName}/createAccessesSciPyExpertDecomposition", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createExpertDecomposition(
            @PathVariable String strategyName,
            @RequestParam String expertName,
            @RequestParam Optional<MultipartFile> expertFile
    ) {
        logger.debug("createExpertDecomposition");

        try {
            decompositionService.createExpertDecomposition(strategyName, expertName, expertFile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/updatedAccessesSciPyDecomposition", method = RequestMethod.GET)
    public ResponseEntity<Decomposition> updatedAccessesSciPyDecomposition(
            @PathVariable String decompositionName
    ) {
        logger.debug("updatedAccessesSciPyDecomposition");

        try {
            Decomposition decomposition = decompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);

            return new ResponseEntity<>(decomposition, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getLocalTransactionsGraphForFunctionality", method = RequestMethod.GET)
    public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getLocalTransactionsGraphForFunctionality(
            @PathVariable String decompositionName,
            @RequestParam String functionalityName
    ) {
        logger.debug("getLocalTransactionsGraphForFunctionality");

        try {
            return new ResponseEntity<>(
                    decompositionService.getLocalTransactionGraphForFunctionality(decompositionName, functionalityName),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getSearchItems", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, String>>> getSearchItems(
            @PathVariable String decompositionName
    ) {
        logger.debug("getSearchItems");

        try {
            return new ResponseEntity<>(decompositionService.getSearchItems(decompositionName), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getEdgeWeights", method = RequestMethod.GET)
    public ResponseEntity<String> getEdgeWeights(
            @PathVariable String decompositionName
    ) {
        logger.debug("getEdgeWeights");

        try {
            return new ResponseEntity<>(decompositionService.getEdgeWeights(decompositionName), HttpStatus.OK);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getGraphPositions", method = RequestMethod.GET)
    public ResponseEntity<String> getGraphPositions(
            @PathVariable String decompositionName
    ) {
        logger.debug("getGraphPositions");

        try {
            String graphPositions = decompositionService.getGraphPositions(decompositionName);

            if (graphPositions == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else return new ResponseEntity<>(graphPositions, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Permanently saves clusters and entities' positions
    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/saveGraphPositions", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> saveGraphPositions(
            @PathVariable String decompositionName,
            @RequestBody String graphPositions
    ) {
        logger.debug("saveGraphPositions");

        decompositionService.saveGraphPositions(decompositionName, graphPositions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/deleteGraphPositions", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteGraphPositions(
            @PathVariable String decompositionName
    ) {
        logger.debug("deleteGraphPositions");

        decompositionService.deleteGraphPositions(decompositionName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getFunctionalitiesAndFunctionalitiesClusters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFunctionalitiesAndFunctionalitiesClusters(
            @PathVariable String decompositionName
    ) {
        logger.debug("getFunctionalitiesAndFunctionalitiesClusters");

        try {
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) decompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);

            Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
                    decomposition.getEntityIDToClusterID(),
                    decomposition.getClusters(),
                    decomposition.getFunctionalities().values()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("functionalities", decomposition.getFunctionalities());
            response.put("functionalitiesClusters", functionalitiesClusters);

            return new ResponseEntity<>(
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getClustersAndClustersFunctionalities", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getClustersAndClustersFunctionalities(
            @PathVariable String decompositionName
    ) {
        logger.debug("getClustersAndClustersFunctionalities");

        try {
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) decompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);

            Map<Short, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(
                    decomposition.getEntityIDToClusterID(),
                    decomposition.getClusters(),
                    decomposition.getFunctionalities().values()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("clusters", decomposition.getClusters());
            response.put("clustersFunctionalities", clustersFunctionalities);

            return new ResponseEntity<>(
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterNameID}/merge", method = RequestMethod.POST)
    public ResponseEntity<Map<Short, Cluster>> mergeClusters(
            @PathVariable String decompositionName,
            @PathVariable Short clusterNameID,
            @RequestParam Short otherClusterID,
            @RequestParam String newName
    ) {
        logger.debug("mergeClusters");
        try {
            return new ResponseEntity<>(decompositionService.mergeClusters(decompositionName, clusterNameID, otherClusterID, newName), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterID}/rename", method = RequestMethod.POST)
    public ResponseEntity<Map<Short, Cluster>> renameCluster(
            @PathVariable String decompositionName,
            @PathVariable Short clusterID,
            @RequestParam String newName
    ) {
        logger.debug("renameCluster");
        try {

            return new ResponseEntity<>(decompositionService.renameCluster(decompositionName, clusterID, newName), HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterID}/split", method = RequestMethod.POST)
    public ResponseEntity<Map<Short, Cluster>> splitCluster(
            @PathVariable String decompositionName,
            @PathVariable Short clusterID,
            @RequestParam String newName,
            @RequestParam String entities
    ) {
        logger.debug("splitCluster");
        try {
            return new ResponseEntity<>(decompositionService.splitCluster(decompositionName, clusterID, newName, entities), HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterID}/transferEntities", method = RequestMethod.POST)
    public ResponseEntity<Map<Short, Cluster>> transferEntities(
            @PathVariable String decompositionName,
            @PathVariable Short clusterID,
            @RequestParam Short toClusterID,
            @RequestParam String entities
    ) {
        logger.debug("transferEntities");
        try {
            return new ResponseEntity<>(decompositionService.transferEntities(decompositionName, clusterID, toClusterID, entities), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/formCluster", method = RequestMethod.POST)
    public ResponseEntity<Map<Short, Cluster>> formCluster(
            @PathVariable String decompositionName,
            @RequestParam String newName,
            @RequestBody Map<Short, List<Short>> entities
    ) {
        logger.debug("formCluster");
        try {
            return new ResponseEntity<>(decompositionService.formCluster(decompositionName, newName, entities), HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/functionality/{functionalityName}/getOrCreateRedesign", method = RequestMethod.GET)
    public ResponseEntity<Functionality> getOrCreateRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName
    ) {

        logger.debug("getOrCreateRedesign");

        try {
            return new ResponseEntity<>(decompositionService.getOrCreateRedesign(decompositionName, functionalityName), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<Functionality> addCompensating(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, Object> data
    ) {
        logger.debug("addCompensating");

        try {
            return new ResponseEntity<>(decompositionService.addCompensating(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/sequenceChange", method = RequestMethod.POST)
    public ResponseEntity<Functionality> sequenceChange(@PathVariable String decompositionName,
                                                        @PathVariable String functionalityName,
                                                        @PathVariable String redesignName,
                                                        @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            return new ResponseEntity<>(decompositionService.sequenceChange(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/dcgi", method = RequestMethod.POST)
    public ResponseEntity<Functionality> dcgi(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, String> data
    ) {
        logger.debug("dcgi");
        try {

            return new ResponseEntity<>(decompositionService.dcgi(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/pivotTransaction", method = RequestMethod.POST)
    public ResponseEntity<Object> pivotTransaction(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam Optional<String> newRedesignName
    ) {
        logger.debug("pivotTransaction");
        try {

            return new ResponseEntity<>(decompositionService.pivotTransaction(decompositionName, functionalityName, redesignName, transactionID, newRedesignName), HttpStatus.OK);
        } catch (NameAlreadyBoundException e){
            return new ResponseEntity<>("Name is already selected", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/changeLTName", method = RequestMethod.POST)
    public ResponseEntity<Functionality> changeLTName(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam String newName
    ){
        logger.debug("changeLTName");
        try {

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/deleteRedesign", method = RequestMethod.DELETE)
    public ResponseEntity<Functionality> deleteRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("deleteRedesign");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);
            functionality.deleteRedesign(redesignName);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}/useForMetrics", method = RequestMethod.POST)
    public ResponseEntity<Functionality> useForMetrics(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("useForMetrics");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);
            functionality.changeFRUsedForMetrics(redesignName);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}