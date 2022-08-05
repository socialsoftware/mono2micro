package pt.ist.socialsoftware.mono2micro.dendrogram.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.AccessesSciPyDendrogramDto;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.AccessesSciPyDendrogramService;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSciPyDendrogramController {

    private static final Logger logger = LoggerFactory.getLogger(Dendrogram.class);

    @Autowired
    AccessesSciPyDendrogramService dendrogramService;

    @RequestMapping(value = "/dendrogram/createAccessesSciPyDendrogram", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createDendrogram(
            @RequestBody AccessesSciPyDendrogramDto dendrogramDto
    ) {
        logger.debug("Create Accesses SciPy Dendrogram");

        try {
            dendrogramService.createDendrogram(dendrogramDto);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    // Specific to accesses similarity generator with SciPy clustering algorithm
    @RequestMapping(value = "/dendrogram/{dendrogramName}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getDendrogramImage(
            @PathVariable String dendrogramName
    ) {
        logger.debug("getDendrogramImage");

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(dendrogramService.getDendrogramImage(dendrogramName));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
