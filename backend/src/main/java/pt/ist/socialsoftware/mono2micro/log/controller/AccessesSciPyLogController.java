package pt.ist.socialsoftware.mono2micro.log.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.log.service.AccessesSciPyLogService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(value = "/mono2micro/accessesSciPyLog/{decompositionName}")
public class AccessesSciPyLogController {
    @Autowired
    AccessesSciPyLogService logService;

    @Autowired
    AccessesSciPyDecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccessesSciPyLogController.class);

    @RequestMapping(value = "/saveGraphPositions", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> saveGraphPositions(
            @PathVariable String decompositionName,
            @RequestBody String graphPositions
    ) {
        logger.debug("saveGraphPositions");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);

            logService.saveGraphPositions(decomposition, graphPositions);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/getGraphPositions", method = RequestMethod.GET)
    public ResponseEntity<String> getGraphPositions(
            @PathVariable String decompositionName
    ) {
        logger.debug("getGraphPositions");

        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);

            String graphPositions = logService.getGraphPositions(decomposition);

            if (graphPositions == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else return new ResponseEntity<>(graphPositions, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/deleteGraphPositions", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteGraphPositions(
            @PathVariable String decompositionName
    ) {
        logger.debug("deleteGraphPositions");

        try {

            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            logService.deleteGraphPositions(decomposition);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/undoOperation", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Cluster>> undoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("undoOperation");

        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            logService.undoOperation(decomposition);
            return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}