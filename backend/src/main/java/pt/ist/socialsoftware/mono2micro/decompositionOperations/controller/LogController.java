package pt.ist.socialsoftware.mono2micro.decompositionOperations.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.service.PositionLogService;

import java.util.Map;

@RestController
@RequestMapping(value = "/mono2micro/log/{decompositionName}")
public class LogController {
    @Autowired
    PositionLogService logService;

    @Autowired
    DecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @RequestMapping(value = "/undoOperation", method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> undoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("undoOperation");

        try {
            logService.undoOperation(decompositionRepository.findByName(decompositionName));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/redoOperation", method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> redoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("redoOperation");

        try {
            logService.redoOperation(decompositionRepository.findByName(decompositionName));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/canUndoRedo", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> canUndoRedo(
            @PathVariable String decompositionName
    ) {
        logger.debug("canUndoRedo");

        try {
            return new ResponseEntity<>(logService.canUndoRedo(decompositionRepository.findByName(decompositionName)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}