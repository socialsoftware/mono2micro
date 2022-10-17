package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesDecompositionService;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.dto.FunctionalityDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesController {
    private static final Logger logger = LoggerFactory.getLogger(AccessesController.class);

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    @Autowired
    FunctionalityService functionalityService;

    @RequestMapping(value = "/accesses/{decompositionName}/getLocalTransactionsGraphForFunctionality", method = RequestMethod.GET)
    public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getLocalTransactionsGraphForFunctionality(
            @PathVariable String decompositionName,
            @RequestParam String functionalityName
    ) {
        logger.debug("getLocalTransactionsGraphForFunctionality");

        try {
            return new ResponseEntity<>(
                    accessesDecompositionService.getLocalTransactionGraphForFunctionality(decompositionName, functionalityName),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/accesses/{decompositionName}/getFunctionalitiesAndFunctionalitiesClusters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFunctionalitiesAndFunctionalitiesClusters(
            @PathVariable String decompositionName
    ) {
        logger.debug("getFunctionalitiesAndFunctionalitiesClusters");

        try {
            AccessesDecomposition decomposition = (AccessesDecomposition) decompositionService.updateDecomposition(decompositionName);

            Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
                    decomposition.getEntityIDToClusterName(),
                    decomposition.getClusters(),
                    decomposition.getFunctionalities().values()
            );

            Map<String, Object> response = new HashMap<>();
            Map<String, Functionality> functionalities = decomposition.getFunctionalities();
            Map<String, List<FunctionalityRedesign>> functionalityRedesignsPerFunctionality = functionalities.values().stream()
                    .collect(Collectors.toMap(Functionality::getName, functionalityService::getFunctionalityRedesigns));
            Map<String, FunctionalityDto> functionalitiesWithRedesigns = functionalities.values().stream()
                    .map(functionality -> new FunctionalityDto(functionality, functionalityRedesignsPerFunctionality.get(functionality.getName())))
                    .collect(Collectors.toMap(FunctionalityDto::getName, functionalityDto -> functionalityDto));
            response.put("functionalities", functionalitiesWithRedesigns);
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

    @RequestMapping(value = "/accesses/{decompositionName}/getClustersAndClustersFunctionalities", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getClustersAndClustersFunctionalities(
            @PathVariable String decompositionName
    ) {
        logger.debug("getClustersAndClustersFunctionalities");

        try {
            AccessesDecomposition decomposition = (AccessesDecomposition) decompositionService.updateDecomposition(decompositionName);

            Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);

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
}