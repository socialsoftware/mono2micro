package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/mono2micro")
public class AccessesSciPyDecompositionController {
    private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    @Autowired
    AccessesSciPyDecompositionService decompositionService;

    @RequestMapping(value = "/strategy/{strategyName}/createAccessesSciPyDecomposition", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createDecomposition(
            @PathVariable String strategyName,
            @RequestParam String cutType,
            @RequestParam float cutValue
    ) {
        logger.debug("createDecomposition");

        try {
            decompositionService.createDecomposition(strategyName, cutType, cutValue);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/strategy/{strategyName}/createAccessesSciPyExpertDecomposition", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createExpertDecomposition(
            @PathVariable String strategyName,
            @RequestParam String expertName,
            @RequestParam Optional<MultipartFile> expertFile
    ) {
        logger.debug("createExpertDecomposition");

        try {
            decompositionService.createExpertDecomposition(strategyName, expertName, expertFile);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
