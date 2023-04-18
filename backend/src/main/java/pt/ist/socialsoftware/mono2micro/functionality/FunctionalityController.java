package pt.ist.socialsoftware.mono2micro.functionality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.controller.DecompositionController;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.dto.FunctionalityDto;

import javax.naming.NameAlreadyBoundException;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping(value = "/mono2micro/decomposition/{decompositionName}/functionality/{functionalityName}/redesign/{redesignName}")
public class FunctionalityController {
    private static final Logger logger = LoggerFactory.getLogger(FunctionalityController.class);

    @Autowired
    FunctionalityService functionalityService;

    @PostMapping(value = "/addCompensating")
    public ResponseEntity<FunctionalityDto> addCompensating(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, Object> data
    ) {
        logger.debug("addCompensating");

        try {
            Functionality functionality = functionalityService.addCompensating(decompositionName, functionalityName, redesignName, data);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/sequenceChange")
    public ResponseEntity<FunctionalityDto> sequenceChange(@PathVariable String decompositionName,
                                                        @PathVariable String functionalityName,
                                                        @PathVariable String redesignName,
                                                        @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            Functionality functionality = functionalityService.sequenceChange(decompositionName, functionalityName, redesignName, data);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @PostMapping(value = "/dcgi")
    public ResponseEntity<FunctionalityDto> dcgi(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestBody HashMap<String, String> data
    ) {
        logger.debug("dcgi");
        try {
            Functionality functionality = functionalityService.dcgi(decompositionName, functionalityName, redesignName, data);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/pivotTransaction")
    public ResponseEntity<FunctionalityDto> pivotTransaction(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam Optional<String> newRedesignName
    ) {
        logger.debug("pivotTransaction");
        try {

            Functionality functionality = functionalityService.pivotTransaction(decompositionName, functionalityName, redesignName, transactionID, newRedesignName);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);
        } catch (NameAlreadyBoundException e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping(value="/changeLTName")
    public ResponseEntity<FunctionalityDto> changeLTName(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName,
            @RequestParam String transactionID,
            @RequestParam String newName
    ){
        logger.debug("changeLTName");
        try {
            Functionality functionality = functionalityService.changeLTName(decompositionName, functionalityName, redesignName, transactionID, newName);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value="/deleteRedesign")
    public ResponseEntity<FunctionalityDto> deleteRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("deleteRedesign");
        try {
            Functionality functionality = functionalityService.deleteRedesign(decompositionName, functionalityName, redesignName);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/useForMetrics")
    public ResponseEntity<FunctionalityDto> useForMetrics(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName,
            @PathVariable String redesignName
    ) {
        logger.debug("useForMetrics");
        try {
            Functionality functionality = functionalityService.useForMetrics(decompositionName, functionalityName, redesignName);
            return new ResponseEntity<>(new FunctionalityDto(functionality, functionalityService.getFunctionalityRedesigns(functionality)), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}