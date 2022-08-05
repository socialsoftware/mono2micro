package pt.ist.socialsoftware.mono2micro.strategy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.DendrogramDto;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.DendrogramDtoFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDtoFactory;
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
                    StrategyDtoFactory.getFactory().getStrategyDto(strategyService.getStrategy(strategyName)),
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
            @Nullable @RequestParam List<String> sourceTypes,
            @Nullable @RequestParam List<Object> sources
    ){
        logger.debug("createStrategy");

        try {
            strategyService.createStrategy(codebaseName, strategyType, sourceTypes, sources);
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

    @RequestMapping(value = "/strategy/{strategyName}/getStrategyDendrograms", method = RequestMethod.GET)
    public ResponseEntity<List<DendrogramDto>> getStrategyDendrogram(
            @PathVariable String strategyName
    ) {
        logger.debug("getStrategyDendrogram");
        try {
            return new ResponseEntity<>(
                    DendrogramDtoFactory.getFactory().getDendrogramDtos(strategyService.getStrategyDendrograms(strategyName)),
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
