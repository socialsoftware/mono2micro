package pt.ist.socialsoftware.mono2micro.decomposition.controller;

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
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.*;

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

    @RequestMapping(value = "/accessesSciPyDecomposition/{decompositionName}/getFunctionalitiesAndFunctionalitiesClusters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFunctionalitiesAndFunctionalitiesClusters(
            @PathVariable String decompositionName
    ) {
        logger.debug("getFunctionalitiesAndFunctionalitiesClusters");

        try {
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) decompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);

            Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
                    decomposition.getEntityIDToClusterName(),
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

            Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(
                    decomposition.getEntityIDToClusterName(),
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

    @RequestMapping(value = "/decomposition/{decompositionName}/cluster/{clusterName}/merge", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Cluster>> mergeClusters(
            @PathVariable String decompositionName,
            @PathVariable String clusterName,
            @RequestParam String otherClusterName,
            @RequestParam String newName
    ) {
        logger.debug("mergeClusters");
        try {
            return new ResponseEntity<>(decompositionService.mergeClustersOperation(decompositionName, clusterName, otherClusterName, newName), HttpStatus.OK);

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
            return new ResponseEntity<>(decompositionService.renameClusterOperation(decompositionName, clusterName, newName), HttpStatus.OK);

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
            return new ResponseEntity<>(decompositionService.splitClusterOperation(decompositionName, clusterName, newName, entities), HttpStatus.OK);

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
            return new ResponseEntity<>(decompositionService.transferEntitiesOperation(decompositionName, clusterName, toClusterName, entities), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/decomposition/{decompositionName}/formCluster", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Cluster>> formCluster(
            @PathVariable String decompositionName,
            @RequestParam String newName,
            @RequestBody Map<String, List<Short>> entities
    ) {
        logger.debug("formCluster");
        try {
            return new ResponseEntity<>(decompositionService.formClusterOperation(decompositionName, newName, entities), HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}