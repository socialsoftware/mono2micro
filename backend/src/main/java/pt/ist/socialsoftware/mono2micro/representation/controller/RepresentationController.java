package pt.ist.socialsoftware.mono2micro.representation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.CodebaseController;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDto;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDtoFactory;
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;

import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class RepresentationController {

    private static final Logger logger = LoggerFactory.getLogger(RepresentationController.class);

    @Autowired
    RepresentationService representationService;

    @GetMapping(value = "/codebase/{codebaseName}/representation/{representationType}/getCodebaseRepresentation")
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


    @DeleteMapping(value = "/representation/{representationName}/delete")
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

    @GetMapping(value = "/representation/{representationName}/getRepresentation")
    public ResponseEntity<RepresentationDto> getRepresentation(@PathVariable String representationName) {
        logger.debug("getRepresentation");

        try {
            return new ResponseEntity<>(RepresentationDtoFactory.getFactory().getRepresentationDto(representationService.getRepresentation(representationName)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/codebase/{codebaseName}/addRepresentations/{representationInfoType}")
    public ResponseEntity<HttpStatus> createStrategy(
            @PathVariable String codebaseName,
            @PathVariable String representationInfoType,
            @Nullable @RequestParam List<String> representationTypes,
            @Nullable @RequestParam List<Object> representations
    ){
        logger.debug("createStrategy");

        try {
            representationService.addRepresentations(codebaseName, representationInfoType, representationTypes, representations);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}