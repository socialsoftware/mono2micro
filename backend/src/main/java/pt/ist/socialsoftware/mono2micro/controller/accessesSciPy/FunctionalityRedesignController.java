package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import java.util.*;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Functionality;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;

@RestController
@RequestMapping(value = "/mono2micro/decomposition/{decompositionName}")
public class FunctionalityRedesignController {

    @Autowired
    AccessesSciPyDecompositionRepository decompositionRepository;

    @Autowired
    SourceService sourceService;

    private static final Logger logger = LoggerFactory.getLogger(FunctionalityRedesignController.class);

}
