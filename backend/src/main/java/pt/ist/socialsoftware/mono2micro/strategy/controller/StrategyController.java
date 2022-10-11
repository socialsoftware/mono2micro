package pt.ist.socialsoftware.mono2micro.strategy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
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
    private static final Logger logger = LoggerFactory.getLogger(Strategy.class);

    @Autowired
    StrategyService strategyService;

    @RequestMapping(value = "/strategy/{strategyName}/getStrategy", method = RequestMethod.GET)
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

    @RequestMapping(value = "/codebase/{codebaseName}/strategy/{strategyType}/createStrategy", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createStrategy(
            @PathVariable String codebaseName,
            @PathVariable String strategyType,
            @Nullable @RequestParam List<String> representationTypes,
            @Nullable @RequestParam List<Object> representations
    ){
        logger.debug("createStrategy");

        try {
            strategyService.createStrategy(codebaseName, strategyType, representationTypes, representations);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/strategy/{strategyName}/getStrategyDecompositions", method = RequestMethod.GET)
    public ResponseEntity<List<DecompositionDto>> getStrategyDecompositions(
            @PathVariable String strategyName
    ) {
        logger.debug("getStrategyDecompositions");
        try {
            return new ResponseEntity<>(
                    DecompositionDtoFactory.getFactory().getDecompositionDtos(strategyService.getStrategyDecompositions(strategyName)),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/strategy/{strategyName}/getStrategySimilarities", method = RequestMethod.GET)
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

    @RequestMapping(value = "/strategy/{strategyName}/delete", method = RequestMethod.DELETE)
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
