package pt.ist.socialsoftware.mono2micro.strategy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.service.AccessesSciPyStrategyService;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSciPyStrategyController {

    private static final Logger logger = LoggerFactory.getLogger(Strategy.class);

    @Autowired
    AccessesSciPyStrategyService strategyService;

    @RequestMapping(value = "/strategy/createAccessesSciPyStrategy", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createStrategy(
            @RequestBody AccessesSciPyStrategyDto strategyDto
    ) {
        logger.debug("Create Accesses SciPy Strategy");

        try {
            strategyService.createStrategy(strategyDto);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    // Specific to accesses similarity generator with SciPy clustering algorithm
    @RequestMapping(value = "/strategy/{strategyName}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getDendrogramImage(
            @PathVariable String strategyName
    ) {
        logger.debug("getDendrogramImage");

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(strategyService.getDendrogramImage(strategyName));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
