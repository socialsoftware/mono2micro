package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesDecompositionService;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.util.*;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesController {
    private static final Logger logger = LoggerFactory.getLogger(AccessesController.class);

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    @GetMapping(value = "/accesses/{decompositionName}/getLocalTransactionsGraphForFunctionality")
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

    @GetMapping(value = "/accesses/{decompositionName}/getFunctionalitiesAndFunctionalitiesClusters")
    public ResponseEntity<Map<String, Object>> getFunctionalitiesAndFunctionalitiesClusters(
            @PathVariable String decompositionName
    ) {
        logger.debug("getFunctionalitiesAndFunctionalitiesClusters");

        try {

            return new ResponseEntity<>(
                    accessesDecompositionService.getFunctionalitiesAndFunctionalitiesClusters(decompositionName),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/accesses/{decompositionName}/getClustersAndClustersFunctionalities")
    public ResponseEntity<Map<String, Object>> getClustersAndClustersFunctionalities(
            @PathVariable String decompositionName
    ) {
        logger.debug("getClustersAndClustersFunctionalities");

        try {
            return new ResponseEntity<>(
                    accessesDecompositionService.getClustersAndClustersFunctionalities(decompositionName),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}