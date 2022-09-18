package pt.ist.socialsoftware.mono2micro.representation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.CodebaseController;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDto;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDtoFactory;
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;

@RestController
@RequestMapping(value = "/mono2micro")
public class RepresentationController {

    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    @Autowired
    RepresentationService representationService;

    @RequestMapping(value = "/codebase/{codebaseName}/representation/{representationType}/getCodebaseRepresentation", method = RequestMethod.GET)
    public ResponseEntity<RepresentationDto> getCodebaseRepresentation(
            @PathVariable String codebaseName,
            @PathVariable String representationType
    ) {
        logger.debug("getCodebaseRepresentation");

        try {
            return new ResponseEntity<>(
                    RepresentationDtoFactory.getFactory().getRepresentationDto(representationService.getCodebaseRepresentation(codebaseName, representationType)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/representation/{representationName}/delete", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteRepresentation(@PathVariable String representationName) {
        logger.debug("deleteCodebase");

        try {
            representationService.deleteSingleRepresentation(representationName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/representation/{representationName}/getRepresentation", method = RequestMethod.GET)
    public ResponseEntity<RepresentationDto> getRepresentation(@PathVariable String representationName) {
        logger.debug("getRepresentation");

        try {
            return new ResponseEntity<>(RepresentationDtoFactory.getFactory().getRepresentationDto(representationService.getRepresentation(representationName)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}