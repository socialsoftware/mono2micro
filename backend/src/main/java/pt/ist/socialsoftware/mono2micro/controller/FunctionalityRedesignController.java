package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/graph/{graphName}")
public class FunctionalityRedesignController {

    private static Logger logger = LoggerFactory.getLogger(FunctionalityRedesignController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<Controller> addCompensating(@PathVariable String codebaseName,
                                                      @PathVariable String dendrogramName,
                                                      @PathVariable String graphName,
                                                      @PathVariable String controllerName,
                                                      @PathVariable String redesignName,
                                                      @RequestBody HashMap<String, String> data) {
        logger.debug("addCompensating");

        try {
            String fromID = data.get("fromID");
            String clusterName = data.get("cluster");
            String entities = data.get("entities");
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = graph.getController(controllerName);
            controller.getFunctionalityRedesign(redesignName).addCompensating(clusterName, entities, fromID);

            Metrics metrics = new Metrics(graph);
            metrics.calculateRedesignComplexities(controller, redesignName);
            codebaseManager.writeCodebase(codebaseName, codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/sequenceChange", method = RequestMethod.POST)
    public ResponseEntity<Controller> sequenceChange(@PathVariable String codebaseName,
                                                     @PathVariable String dendrogramName,
                                                     @PathVariable String graphName,
                                                     @PathVariable String controllerName,
                                                     @PathVariable String redesignName,
                                                     @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            String localTransactionID = data.get("localTransactionID");
            String newCaller = data.get("newCaller");

            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = codebase.getDendrogram(dendrogramName).getGraph(graphName).getController(controllerName);
            controller.getFunctionalityRedesign(redesignName).sequenceChange(localTransactionID, newCaller);

            Metrics metrics = new Metrics(graph);
            metrics.calculateRedesignComplexities(controller, redesignName);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/dcgi", method = RequestMethod.POST)
    public ResponseEntity<Controller> dcgi(@PathVariable String codebaseName,
                                           @PathVariable String dendrogramName,
                                           @PathVariable String graphName,
                                           @PathVariable String controllerName,
                                           @PathVariable String redesignName,
                                           @RequestBody HashMap<String, String> data) {
        logger.debug("dcgi");
        try {
            String fromCluster = data.get("fromCluster");
            String toCluster = data.get("toCluster");
            String localTransactions = data.get("localTransactions");

            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = graph.getController(controllerName);

            controller.getFunctionalityRedesign(redesignName).dcgi(fromCluster, toCluster, localTransactions);

            Metrics metrics = new Metrics(graph);
            metrics.calculateRedesignComplexities(controller, redesignName);
            codebaseManager.writeCodebase(codebaseName, codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value = "/controller/{controllerName}/redesign/{redesignName}/pivotTransaction", method = RequestMethod.POST)
    public ResponseEntity<Controller> pivotTransaction(@PathVariable String codebaseName,
                                                       @PathVariable String dendrogramName,
                                                       @PathVariable String graphName,
                                                       @PathVariable String controllerName,
                                                       @PathVariable String redesignName,
                                                       @RequestParam String transactionID,
                                                       @RequestParam Optional<String> newRedesignName) {
        logger.debug("pivotTransaction");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = graph.getController(controllerName);

            if(newRedesignName.isPresent())
                if(!controller.checkNameValidity(newRedesignName.get()))
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);


            controller.getFunctionalityRedesign(redesignName).definePivotTransaction(transactionID);
            Metrics metrics = new Metrics(graph);
            metrics.calculateRedesignComplexities(controller, redesignName);

            if(newRedesignName.isPresent()) {
                controller.changeFunctionalityRedesignName(redesignName, newRedesignName.get());
                controller.createFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME);
            }

            metrics.calculateRedesignComplexities(controller, Constants.DEFAULT_REDESIGN_NAME);
            codebaseManager.writeCodebase(codebaseName, codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/controller/{controllerName}/redesign/{redesignName}/changeLTName", method = RequestMethod.POST)
    public ResponseEntity<Controller> changeLTName(@PathVariable String codebaseName,
                                                   @PathVariable String dendrogramName,
                                                   @PathVariable String graphName,
                                                   @PathVariable String controllerName,
                                                   @PathVariable String redesignName,
                                                   @RequestParam String transactionID,
                                                   @RequestParam String newName){

        logger.debug("changeLTName");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = graph.getController(controllerName);
            controller.getFunctionalityRedesign(redesignName).changeLTName(transactionID, newName);
            codebaseManager.writeCodebase(codebaseName, codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/controller/{controllerName}/redesign/{redesignName}/deleteRedesign", method = RequestMethod.DELETE)
    public ResponseEntity<Controller> deleteRedesign(@PathVariable String codebaseName,
                                                     @PathVariable String dendrogramName,
                                                     @PathVariable String graphName,
                                                     @PathVariable String controllerName,
                                                     @PathVariable String redesignName) {
        logger.debug("deleteRedesign");
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Graph graph = codebase.getDendrogram(dendrogramName).getGraph(graphName);
            Controller controller = graph.getController(controllerName);
            controller.deleteRedesign(redesignName);
            codebaseManager.writeCodebase(codebaseName, codebase);

            return new ResponseEntity<>(controller, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
