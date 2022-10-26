package pt.ist.socialsoftware.mono2micro.representation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.CodebaseController;
import pt.ist.socialsoftware.mono2micro.representation.service.AccessesRepresentationService;

import javax.management.openmbean.KeyAlreadyExistsException;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesRepresentationController {
    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    @Autowired
    AccessesRepresentationService accessesRepresentationService;

    @RequestMapping(value = "/representation/{representationName}/addAccessesProfile", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> addAccessesProfile(
            @PathVariable String representationName,
            @RequestParam String profile
    ) {
        logger.debug("addAccessesProfile");

        try {
            accessesRepresentationService.addAccessesProfile(representationName, profile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/representation/{representationName}/moveAccessesFunctionalities", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> moveAccessesFunctionalities(
            @PathVariable String representationName,
            @RequestBody String[] functionalities,
            @RequestParam String targetProfile
    ) {
        logger.debug("moveAccessesFunctionalities");

        try {
            accessesRepresentationService.moveAccessesFunctionalities(representationName, functionalities, targetProfile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/representation/{representationName}/deleteAccessesProfile", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteAccessesProfile(
            @PathVariable String representationName,
            @RequestParam String profile
    ) {
        logger.debug("deleteAccessesProfile");

        try {
            accessesRepresentationService.deleteAccessesProfile(representationName, profile);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/representation/{codebaseName}/getIdToEntity", method = RequestMethod.GET)
    public ResponseEntity<String> getIdToEntity(@PathVariable String codebaseName) {
        logger.debug("getIdToEntity");

        try {
            return new ResponseEntity<>(accessesRepresentationService.getIdToEntity(codebaseName), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}