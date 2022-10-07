package pt.ist.socialsoftware.mono2micro.log.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.log.service.PositionLogService;

import java.io.IOException;

@RestController
@RequestMapping(value = "/mono2micro/positionLog/{decompositionName}")
public class PositionLogController {
    @Autowired
    PositionLogService logService;

    @Autowired
    DecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(PositionLogController.class);

    @RequestMapping(value = "/saveGraphPositions", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> saveGraphPositions(
            @PathVariable String decompositionName,
            @RequestBody String graphPositions
    ) {
        logger.debug("saveGraphPositions");
        try {
            LogDecomposition decomposition = (LogDecomposition) decompositionRepository.findByName(decompositionName);

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
            LogDecomposition decomposition = (LogDecomposition) decompositionRepository.findByName(decompositionName);

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

            LogDecomposition decomposition = (LogDecomposition) decompositionRepository.findByName(decompositionName);
            logService.deleteGraphPositions(decomposition);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
