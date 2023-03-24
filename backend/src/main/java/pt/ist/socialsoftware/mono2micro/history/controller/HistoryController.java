package pt.ist.socialsoftware.mono2micro.history.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;

import java.util.Map;

@RestController
@RequestMapping(value = "/mono2micro/history/{decompositionName}")
public class HistoryController {
    @Autowired
    HistoryService historyService;

    @Autowired
    DecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    @GetMapping(value = "/undoOperation")
    public ResponseEntity<HttpStatus> undoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("undoOperation");

        try {
            historyService.undoOperation(decompositionRepository.findByName(decompositionName));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/redoOperation")
    public ResponseEntity<HttpStatus> redoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("redoOperation");

        try {
            historyService.redoOperation(decompositionRepository.findByName(decompositionName));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/canUndoRedo")
    public ResponseEntity<Map<String, Boolean>> canUndoRedo(
            @PathVariable String decompositionName
    ) {
        logger.debug("canUndoRedo");

        try {
            return new ResponseEntity<>(historyService.canUndoRedo(decompositionRepository.findByName(decompositionName)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}