package pt.ist.socialsoftware.mono2micro.functionality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.controller.DecompositionController;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import javax.naming.NameAlreadyBoundException;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping(value = "/mono2micro/decomposition/{decompositionName}/functionality/{functionalityName}")
public class FunctionalityController {
    private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    @Autowired
    FunctionalityService functionalityService;

    @RequestMapping(value = "/getOrCreateRedesign", method = RequestMethod.GET)
    public ResponseEntity<Functionality> getOrCreateRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName
    ) {

        logger.debug("getOrCreateRedesign");

        try {
            return new ResponseEntity<>(functionalityService.getOrCreateRedesign(decompositionName, functionalityName), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/redesign/{redesignName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<Functionality> addCompensating(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, Object> data
    ) {
        logger.debug("addCompensating");

        try {
            return new ResponseEntity<>(functionalityService.addCompensating(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/redesign/{redesignName}/sequenceChange", method = RequestMethod.POST)
    public ResponseEntity<Functionality> sequenceChange(@PathVariable String decompositionName,
                                                        @PathVariable String functionalityName,
                                                        @PathVariable String redesignName,
                                                        @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            return new ResponseEntity<>(functionalityService.sequenceChange(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/redesign/{redesignName}/dcgi", method = RequestMethod.POST)
    public ResponseEntity<Functionality> dcgi(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, String> data
    ) {
        logger.debug("dcgi");
        try {

            return new ResponseEntity<>(functionalityService.dcgi(decompositionName, functionalityName, redesignName, data), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/redesign/{redesignName}/pivotTransaction", method = RequestMethod.POST)
    public ResponseEntity<Object> pivotTransaction(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam Optional<String> newRedesignName
    ) {
        logger.debug("pivotTransaction");
        try {

            return new ResponseEntity<>(functionalityService.pivotTransaction(decompositionName, functionalityName, redesignName, transactionID, newRedesignName), HttpStatus.OK);
        } catch (NameAlreadyBoundException e){
            return new ResponseEntity<>("Name is already selected", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/redesign/{redesignName}/changeLTName", method = RequestMethod.POST)
    public ResponseEntity<Functionality> changeLTName(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam String newName
    ){
        logger.debug("changeLTName");
        try {

            return new ResponseEntity<>(functionalityService.changeLTName(decompositionName, functionalityName, redesignName, transactionID, newName), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/redesign/{redesignName}/deleteRedesign", method = RequestMethod.DELETE)
    public ResponseEntity<Functionality> deleteRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("deleteRedesign");
        try {
            functionalityService.deleteRedesign(decompositionName, functionalityName, redesignName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/redesign/{redesignName}/useForMetrics", method = RequestMethod.POST)
    public ResponseEntity<Functionality> useForMetrics(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("useForMetrics");
        try {
            return new ResponseEntity<>(functionalityService.useForMetrics(decompositionName, functionalityName, redesignName), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}