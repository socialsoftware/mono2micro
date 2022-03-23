package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.source.AccessesSource;
import pt.ist.socialsoftware.mono2micro.domain.source.Source;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.HashSet;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.*;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class SourceController {

    private static Logger logger = LoggerFactory.getLogger(SourceController.class);

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    @RequestMapping(value = "/source/{sourceType}/getSource", method = RequestMethod.GET)
    public ResponseEntity<Source> getSource(@PathVariable String codebaseName, @PathVariable String sourceType) {
        logger.debug("getSource");

        try {
            return new ResponseEntity<>(codebaseManager.getCodebaseSource(codebaseName, sourceType), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/source/{sourceType}/addProfile", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> addProfile(
            @PathVariable String codebaseName,
            @PathVariable String sourceType,
            @RequestParam String profile
    ) {
        logger.debug("addProfile");

        try {
            switch (sourceType) {
                case ACCESSES:
                    AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(codebaseName, sourceType);
                    source.addProfile(profile, new HashSet<>());
                    codebaseManager.writeSource(codebaseName, source.getType(), source);
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/source/{sourceType}/moveControllers", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> moveControllers(
            @PathVariable String codebaseName,
            @PathVariable String sourceType,
            @RequestBody String[] controllers,
            @RequestParam String targetProfile
    ) {
        logger.debug("moveControllers");

        try {
            switch (sourceType) {
                case ACCESSES:
                    AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(codebaseName, sourceType);
                    source.moveControllers(controllers, targetProfile);
                    codebaseManager.writeSource(codebaseName, source.getType(), source);
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/source/{sourceType}/deleteProfile", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteProfile(
            @PathVariable String codebaseName,
            @PathVariable String sourceType,
            @RequestParam String profile
    ) {
        logger.debug("deleteProfile");

        try {
            switch (sourceType) {
                case ACCESSES:
                    AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(codebaseName, sourceType);
                    source.deleteProfile(profile);
                    codebaseManager.writeSource(codebaseName, source.getType(), source);
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/source/{sourceType}/getInputFile", method = RequestMethod.GET)
    public ResponseEntity<String> getInputFile(
            @PathVariable String codebaseName,
            @PathVariable String sourceType
    ) {
        logger.debug("getInputFile");

        try {
            return new ResponseEntity<>(codebaseManager.getInputFile(codebaseName, sourceType), HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
