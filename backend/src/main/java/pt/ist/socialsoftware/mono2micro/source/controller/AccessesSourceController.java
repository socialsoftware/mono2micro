package pt.ist.socialsoftware.mono2micro.source.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.CodebaseController;
import pt.ist.socialsoftware.mono2micro.source.service.AccessesSourceService;

import javax.management.openmbean.KeyAlreadyExistsException;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSourceController {
    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    @Autowired
    AccessesSourceService accessesSourceService;

    @RequestMapping(value = "/source/{sourceName}/addAccessesProfile", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> addAccessesProfile(
            @PathVariable String sourceName,
            @RequestParam String profile
    ) {
        logger.debug("addAccessesProfile");

        try {
            accessesSourceService.addAccessesProfile(sourceName, profile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/source/{sourceName}/moveAccessesFunctionalities", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> moveAccessesFunctionalities(
            @PathVariable String sourceName,
            @RequestBody String[] functionalities,
            @RequestParam String targetProfile
    ) {
        logger.debug("moveAccessesFunctionalities");

        try {
            accessesSourceService.moveAccessesFunctionalities(sourceName, functionalities, targetProfile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/source/{sourceName}/deleteAccessesProfile", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteAccessesProfile(
            @PathVariable String sourceName,
            @RequestParam String profile
    ) {
        logger.debug("deleteAccessesProfile");

        try {
            accessesSourceService.deleteAccessesProfile(sourceName, profile);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
