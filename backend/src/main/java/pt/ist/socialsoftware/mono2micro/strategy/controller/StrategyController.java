package pt.ist.socialsoftware.mono2micro.strategy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ClusteringFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDtoFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.service.StrategyService;

import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class StrategyController {
    private static final Logger logger = LoggerFactory.getLogger(StrategyController.class);

    @Autowired
    StrategyService strategyService;

    @GetMapping(value = "/strategy/{strategyName}/getStrategy")
    public ResponseEntity<StrategyDto> getStrategy(
            @PathVariable String strategyName
    ) {
        logger.debug("getStrategy");
        try {
            return new ResponseEntity<>(
                    new StrategyDto(strategyService.getStrategy(strategyName)),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/codebase/{codebaseName}/createStrategy")
    public ResponseEntity<HttpStatus> createStrategy(
            @PathVariable String codebaseName,
            @Nullable @RequestParam String algorithmType,
            @Nullable @RequestParam List<String> strategyTypes
    ){
        logger.debug("createStrategy");

        try {
            strategyService.createStrategy(codebaseName, algorithmType, strategyTypes);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/strategy/getAlgorithms")
    public ResponseEntity<List<String>> getAlgorithms() {
        logger.debug("getAlgorithms");
        try {
            return new ResponseEntity<>(
                    ClusteringFactory.algorithmTypes,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/strategy/{strategyName}/getStrategyDecompositions")
    public ResponseEntity<List<DecompositionDto>> getStrategyDecompositions(
            @PathVariable String strategyName
    ) {
        logger.debug("getStrategyDecompositions");
        try {
            return new ResponseEntity<>(
                    DecompositionDtoFactory.getDecompositionDtos(strategyService.getStrategyDecompositions(strategyName)),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/strategy/{strategyName}/getStrategySimilarities")
    public ResponseEntity<List<SimilarityDto>> getStrategySimilarities(
            @PathVariable String strategyName
    ) {
        logger.debug("getStrategySimilarities");
        try {
            return new ResponseEntity<>(
                    SimilarityDtoFactory.getSimilarityDtos(strategyService.getStrategySimilarities(strategyName)),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/strategy/{strategyName}/delete")
    public ResponseEntity<HttpStatus> deleteStrategy(
            @PathVariable String strategyName
    ) {
        logger.debug("Delete Strategy");

        try {
            strategyService.deleteSingleStrategy(strategyName);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
