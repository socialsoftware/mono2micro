package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/dendrogram/{dendrogramName}/graph/{graphName}")
public class FunctionalityRedesignController {

    private static Logger logger = LoggerFactory.getLogger(ClusterController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();

    @RequestMapping(value = "/controller/{controllerName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<List<LocalTransaction>> addCompensating(@PathVariable String codebaseName,
                                                                  @PathVariable String dendrogramName,
                                                                  @PathVariable String graphName,
                                                                  @PathVariable String controllerName,
                                                                  @RequestBody HashMap<String, String> data) {
        logger.debug("addCompensating");

        try {
            String fromID = data.get("fromID");
            String clusterName = data.get("cluster");
            String entities = data.get("entities");
            Codebase codebase = codebaseManager.getCodebase(codebaseName);

            List<LocalTransaction> response = codebase
                .getDendrogram(dendrogramName)
                .getGraph(graphName)
                .getController(controllerName)
                .addCompensating(clusterName, entities, fromID);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
