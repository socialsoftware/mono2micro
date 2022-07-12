package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.service.AccessesSciPyHistoryService;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

@RestController
@RequestMapping(value = "/mono2micro/decomposition/{decompositionName}")
public class ClusterController {

	@Autowired
	AccessesSciPyHistoryService accessesSciPyHistoryService;

	@Autowired
	AccessesSciPyDecompositionService decompositionService;

	@Autowired
	AccessesSciPyDecompositionRepository decompositionRepository;

	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

}