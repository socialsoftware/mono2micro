package pt.ist.socialsoftware.mono2micro.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.socialsoftware.mono2micro.controller.accessesSciPy.ClusterController;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.history.service.AccessesSciPyHistoryService;

import java.util.Map;

@RestController
@RequestMapping(value = "/mono2micro/history/{decompositionName}")
public class AccessesSciPyHistoryController {
    @Autowired
    AccessesSciPyHistoryService accessesSciPyHistoryService;

    @Autowired
    AccessesSciPyDecompositionRepository decompositionRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @RequestMapping(value = "/undoOperation", method = RequestMethod.GET)
    public ResponseEntity<Map<Short, Cluster>> undoOperation(
            @PathVariable String decompositionName
    ) {
        logger.debug("undoOperation");

        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

            accessesSciPyHistoryService.undoOperation(decomposition);

            return new ResponseEntity<>(decomposition.getClusters(), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
