package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
import java.util.*;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/decomposition/{decompositionName}")
public class FunctionalityRedesignController {

    private static Logger logger = LoggerFactory.getLogger(FunctionalityRedesignController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();


    //TODO: REFACTOR TO USE THE STATICCOLLECTION OBJECT
//    @RequestMapping(value = "/controller/{controllerName}/getOrCreateRedesign", method = RequestMethod.GET)
//    public ResponseEntity<Controller> getOrCreateRedesign(
//            @PathVariable String codebaseName,
//            @PathVariable String dendrogramName,
//            @PathVariable String decompositionName,
//            @PathVariable String controllerName
//    ) {
//
//        logger.debug("getOrCreateRedesign");
//
//        try {
//            Codebase codebase = codebaseManager.getCodebase(codebaseName);
//            Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
//            Decomposition decomposition = dendrogram.getDecomposition(decompositionName);
//            Controller controller = decomposition.getController(controllerName);
//            if(controller.getFunctionalityRedesigns()
//                    .stream()
//                    .noneMatch(e -> e.getName().equals(Constants.DEFAULT_REDESIGN_NAME))){
//                controller.createFunctionalityRedesign(
//                        Constants.DEFAULT_REDESIGN_NAME,
//                        true,
//                        decomposition.getControllerLocalTransactionsGraph(
//                                codebase,
//                                controllerName,
//                                dendrogram.getTraceType(),
//                                dendrogram.getTracesMaxLimit()
//                        )
//                );
//            };
//            codebaseManager.writeCodebase(codebase);
//            return new ResponseEntity<>(controller, HttpStatus.OK);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<Controller> addCompensating(
        @PathVariable String codebaseName,
        @PathVariable String dendrogramName,
        @PathVariable String decompositionName,
        @PathVariable String controllerName,
        @PathVariable String redesignName,
        @RequestBody HashMap<String, Object> data
    ) {
        logger.debug("addCompensating");

        try {
            int fromID = (Integer) data.get("fromID");
            Short clusterID = ((Integer) data.get("cluster")).shortValue();
            ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");


            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = decomposition.getController(controllerName);

            controller.getFunctionalityRedesign(redesignName).addCompensating(clusterID, accesses, fromID);
            Metrics.calculateRedesignComplexities(controller, redesignName, decomposition);
            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/sequenceChange", method = RequestMethod.POST)
    public ResponseEntity<Controller> sequenceChange(@PathVariable String codebaseName,
                                                     @PathVariable String dendrogramName,
                                                     @PathVariable String decompositionName,
                                                     @PathVariable String controllerName,
                                                     @PathVariable String redesignName,
                                                     @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            String localTransactionID = data.get("localTransactionID");
            String newCaller = data.get("newCaller");

            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName).getController(controllerName);
            controller.getFunctionalityRedesign(redesignName).sequenceChange(localTransactionID, newCaller);

            Metrics.calculateRedesignComplexities(controller, redesignName, decomposition);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(controller, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/dcgi", method = RequestMethod.POST)
    public ResponseEntity<Controller> dcgi(
        @PathVariable String codebaseName,
        @PathVariable String dendrogramName,
        @PathVariable String decompositionName,
        @PathVariable String controllerName,
        @PathVariable String redesignName,
        @RequestBody HashMap<String, String> data
    ) {
        logger.debug("dcgi");
        try {
            Short fromClusterID = Short.parseShort(data.get("fromCluster"));
            Short toClusterID = Short.parseShort(data.get("toCluster"));
            String localTransactions = data.get("localTransactions");

            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = decomposition.getController(controllerName);

            controller.getFunctionalityRedesign(redesignName).dcgi(fromClusterID, toClusterID, localTransactions);

            Metrics.calculateRedesignComplexities(controller, redesignName, decomposition);
            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }

    }

    //TODO: REFACTOR TO USE STATICCOLLECTION OBJECT
//    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/pivotTransaction", method = RequestMethod.POST)
//    public ResponseEntity<Object> pivotTransaction(
//        @PathVariable String codebaseName,
//        @PathVariable String dendrogramName,
//        @PathVariable String decompositionName,
//        @PathVariable String controllerName,
//        @PathVariable String redesignName,
//        @RequestParam String transactionID,
//        @RequestParam Optional<String> newRedesignName
//    ) {
//        logger.debug("pivotTransaction");
//        try {
//            Codebase codebase = codebaseManager.getCodebase(codebaseName);
//            Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);
//            Decomposition decomposition = dendrogram.getDecomposition(decompositionName);
//            Controller controller = decomposition.getController(controllerName);
//
//            if(newRedesignName.isPresent())
//                if(!controller.checkNameValidity(newRedesignName.get()))
//                    return new ResponseEntity<>("Name is already selected",HttpStatus.FORBIDDEN);
//
//
//            controller.getFunctionalityRedesign(redesignName).definePivotTransaction(Integer.parseInt(transactionID));
//            Metrics.calculateRedesignComplexities(controller, redesignName, decomposition);
//
//            if(newRedesignName.isPresent()) {
//                controller.changeFunctionalityRedesignName(redesignName, newRedesignName.get());
//
//                DirectedAcyclicGraph<LocalTransaction, DefaultEdge> controllerLocalTransactionsGraph = decomposition.getControllerLocalTransactionsGraph(
//                    codebase,
//                    controllerName,
//                    dendrogram.getTraceType(),
//                    dendrogram.getTracesMaxLimit()
//                );
//
//                controller.createFunctionalityRedesign(
//                    Constants.DEFAULT_REDESIGN_NAME,
//                    false,
//                    controllerLocalTransactionsGraph
//                );
//            }
//
//            Metrics.calculateRedesignComplexities(controller, Constants.DEFAULT_REDESIGN_NAME, decomposition);
//            codebaseManager.writeCodebase(codebase);
//
//            return new ResponseEntity<>(controller, HttpStatus.OK);
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//    }

    @RequestMapping(value="/controller/{controllerName}/redesign/{redesignName}/changeLTName", method = RequestMethod.POST)
    public ResponseEntity<Controller> changeLTName(
        @PathVariable String codebaseName,
        @PathVariable String dendrogramName,
        @PathVariable String decompositionName,
        @PathVariable String controllerName,
        @PathVariable String redesignName,
        @RequestParam String transactionID,
        @RequestParam String newName
    ){
        logger.debug("changeLTName");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = decomposition.getController(controllerName);
            controller.getFunctionalityRedesign(redesignName).changeLTName(transactionID, newName);
            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/controller/{controllerName}/redesign/{redesignName}/deleteRedesign", method = RequestMethod.DELETE)
    public ResponseEntity<Controller> deleteRedesign(
        @PathVariable String codebaseName,
        @PathVariable String dendrogramName,
        @PathVariable String decompositionName,
        @PathVariable String controllerName,
        @PathVariable String redesignName
    ) {
        logger.debug("deleteRedesign");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = decomposition.getController(controllerName);
            controller.deleteRedesign(redesignName);
            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/controller/{controllerName}/redesign/{redesignName}/useForMetrics", method = RequestMethod.POST)
    public ResponseEntity<Controller> useForMetrics(
        @PathVariable String codebaseName,
        @PathVariable String dendrogramName,
        @PathVariable String decompositionName,
        @PathVariable String controllerName,
        @PathVariable String redesignName
    ) {
        logger.debug("useForMetrics");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Decomposition decomposition = codebase.getDendrogram(dendrogramName).getDecomposition(decompositionName);
            Controller controller = decomposition.getController(controllerName);
            controller.changeFRUsedForMetrics(redesignName);
            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
