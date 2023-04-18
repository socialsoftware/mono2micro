package pt.ist.socialsoftware.mono2micro.history.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;

import java.io.IOException;

@RestController
@RequestMapping(value = "/mono2micro/positionHistory/{decompositionName}")
public class PositionHistoryController {
    @Autowired
    PositionHistoryService historyService;

    @Autowired
    DecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(PositionHistoryController.class);

    @PostMapping(value = "/saveGraphPositions")
    public ResponseEntity<HttpStatus> saveGraphPositions(
            @PathVariable String decompositionName,
            @RequestBody String graphPositions
    ) {
        logger.debug("saveGraphPositions");
        try {
            Decomposition decomposition = decompositionRepository.findByName(decompositionName);

            historyService.saveGraphPositions(decomposition, graphPositions);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getGraphPositions")
    public ResponseEntity<String> getGraphPositions(
            @PathVariable String decompositionName
    ) {
        logger.debug("getGraphPositions");

        try {
            Decomposition decomposition = decompositionRepository.findByName(decompositionName);

            String graphPositions = historyService.getGraphPositions(decomposition);

            if (graphPositions == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else return new ResponseEntity<>(graphPositions, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}