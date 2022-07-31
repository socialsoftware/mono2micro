package pt.ist.socialsoftware.mono2micro.strategy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.service.StrategyService;

import java.util.List;

public class StrategyController {
    private static final Logger logger = LoggerFactory.getLogger(Strategy.class);

    @Autowired
    StrategyService strategyService;

    @RequestMapping(value = "/codebase/{codebaseName}/strategy/{strategyType}/createStrategy", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createStrategy(
            @PathVariable String codebaseName,
            @PathVariable String strategyType,
            @RequestParam List<String> sourceTypes,
            @RequestParam List<Object> sources
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

}
