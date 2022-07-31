package pt.ist.socialsoftware.mono2micro.source.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.CodebaseController;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDto;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDtoFactory;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;

import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class SourceController {

    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    @Autowired
    SourceService sourceService;

    @RequestMapping(value = "/codebase/{codebaseName}/source/{sourceType}/getCodebaseSource", method = RequestMethod.GET)
    public ResponseEntity<SourceDto> getCodebaseSource(
            @PathVariable String codebaseName,
            @PathVariable String sourceType
    ) {
        logger.debug("getCodebaseSource");

        try {
            return new ResponseEntity<>(
                    SourceDtoFactory.getFactory().getSourceDto(sourceService.getCodebaseSource(codebaseName, sourceType)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/source/{sourceName}/delete", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteSource(@PathVariable String sourceName) {
        logger.debug("deleteCodebase");

        try {
            sourceService.deleteSource(sourceName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/source/{sourceName}/getSource", method = RequestMethod.GET)
    public ResponseEntity<SourceDto> getSource(@PathVariable String sourceName) {
        logger.debug("getSource");

        try {
            return new ResponseEntity<>(SourceDtoFactory.getFactory().getSourceDto(sourceService.getSource(sourceName)), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}