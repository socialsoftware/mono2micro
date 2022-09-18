package pt.ist.socialsoftware.mono2micro.similarity.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.service.AccessesSciPySimilarityService;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSciPySimilarityController {

    private static final Logger logger = LoggerFactory.getLogger(Similarity.class);

    @Autowired
    AccessesSciPySimilarityService similarityService;


    // Specific to accesses similarity generator with SciPy clustering algorithm
    @RequestMapping(value = "/similarity/{similarityName}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getDendrogramImage(
            @PathVariable String similarityName
    ) {
        logger.debug("getDendrogramImage");

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(similarityService.getDendrogramImage(similarityName));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
