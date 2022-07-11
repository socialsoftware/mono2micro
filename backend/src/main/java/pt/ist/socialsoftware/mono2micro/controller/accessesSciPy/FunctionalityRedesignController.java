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
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.domain.*;
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

    @RequestMapping(value = "/functionality/{functionalityName}/getOrCreateRedesign", method = RequestMethod.GET)
    public ResponseEntity<Functionality> getOrCreateRedesign(
            @PathVariable String decompositionName,
            @PathVariable String functionalityName
    ) {

        logger.debug("getOrCreateRedesign");

        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();

            Functionality functionality = decomposition.getFunctionality(functionalityName);

            Source source = strategy.getCodebase().getSourceByType(ACCESSES);

            if(functionality.getFunctionalityRedesigns()
                    .stream()
                    .noneMatch(e -> e.getName().equals(Constants.DEFAULT_REDESIGN_NAME))){
                functionality.createFunctionalityRedesign(
                        Constants.DEFAULT_REDESIGN_NAME,
                        true,
                        functionality.createLocalTransactionGraphFromScratch(
                                        sourceService.getSourceFileAsInputStream(source.getName()),
                                        strategy.getTracesMaxLimit(),
                                        strategy.getTraceType(),
                                        decomposition.getEntityIDToClusterID())
                );
            };
            decompositionRepository.save(decomposition);
            return new ResponseEntity<>(functionality, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/functionality/{functionalityName}/redesign/{redesignName}/addCompensating", method = RequestMethod.POST)
    public ResponseEntity<Functionality> addCompensating(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName,
        @RequestBody HashMap<String, Object> data
    ) {
        logger.debug("addCompensating");

        try {
            int fromID = (Integer) data.get("fromID");
            Short clusterID = ((Integer) data.get("cluster")).shortValue();
            ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");


            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);

            FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
            functionalityRedesign.addCompensating(clusterID, accesses, fromID);
            // TODO certificar se a proxima linha e' necessaria
            functionalityRedesign.calculateMetrics(decomposition, functionality);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/functionality/{functionalityName}/redesign/{redesignName}/sequenceChange", method = RequestMethod.POST)
    public ResponseEntity<Functionality> sequenceChange(@PathVariable String decompositionName,
                                                        @PathVariable String functionalityName,
                                                        @PathVariable String redesignName,
                                                        @RequestBody HashMap<String, String> data) {
        logger.debug("sequenceChange");
        try {
            String localTransactionID = data.get("localTransactionID");
            String newCaller = data.get("newCaller");

            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);

            FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
            functionalityRedesign.sequenceChange(localTransactionID, newCaller);
            functionalityRedesign.calculateMetrics(decomposition, functionality);

            decompositionRepository.save(decomposition);
            return new ResponseEntity<>(functionality, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/functionality/{functionalityName}/redesign/{redesignName}/dcgi", method = RequestMethod.POST)
    public ResponseEntity<Functionality> dcgi(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName,
        @RequestBody HashMap<String, String> data
    ) {
        logger.debug("dcgi");
        try {
            Short fromClusterID = Short.parseShort(data.get("fromCluster"));
            Short toClusterID = Short.parseShort(data.get("toCluster"));
            String localTransactions = data.get("localTransactions");

            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);

            FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
            functionalityRedesign.dcgi(fromClusterID, toClusterID, localTransactions);
            functionalityRedesign.calculateMetrics(decomposition, functionality);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/functionality/{functionalityName}/redesign/{redesignName}/pivotTransaction", method = RequestMethod.POST)
    public ResponseEntity<Object> pivotTransaction(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName,
        @RequestParam String transactionID,
        @RequestParam Optional<String> newRedesignName
    ) {
        logger.debug("pivotTransaction");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
            Functionality functionality = decomposition.getFunctionality(functionalityName);

            if(newRedesignName.isPresent())
                if(!functionality.checkNameValidity(newRedesignName.get()))
                    return new ResponseEntity<>("Name is already selected",HttpStatus.FORBIDDEN);

            FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
            functionalityRedesign.definePivotTransaction(Integer.parseInt(transactionID));
            functionalityRedesign.calculateMetrics(decomposition, functionality);

            if(newRedesignName.isPresent()) {
                functionality.changeFunctionalityRedesignName(redesignName, newRedesignName.get());

                Source source = strategy.getCodebase().getSourceByType(ACCESSES);

                DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                        .createLocalTransactionGraphFromScratch(
                                sourceService.getSourceFileAsInputStream(source.getName()),
                                strategy.getTracesMaxLimit(),
                                strategy.getTraceType(),
                                decomposition.getEntityIDToClusterID());

                functionality.createFunctionalityRedesign(
                    Constants.DEFAULT_REDESIGN_NAME,
                    false,
                    functionalityLocalTransactionsGraph
                );
            }

            functionalityRedesign = functionality.getFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME);
            functionalityRedesign.calculateMetrics(decomposition, functionality);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="/functionality/{functionalityName}/redesign/{redesignName}/changeLTName", method = RequestMethod.POST)
    public ResponseEntity<Functionality> changeLTName(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName,
        @RequestParam String transactionID,
        @RequestParam String newName
    ){
        logger.debug("changeLTName");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);
            functionality.getFunctionalityRedesign(redesignName).changeLTName(transactionID, newName);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/functionality/{functionalityName}/redesign/{redesignName}/deleteRedesign", method = RequestMethod.DELETE)
    public ResponseEntity<Functionality> deleteRedesign(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName
    ) {
        logger.debug("deleteRedesign");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);
            functionality.deleteRedesign(redesignName);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/functionality/{functionalityName}/redesign/{redesignName}/useForMetrics", method = RequestMethod.POST)
    public ResponseEntity<Functionality> useForMetrics(
        @PathVariable String decompositionName,
        @PathVariable String functionalityName,
        @PathVariable String redesignName
    ) {
        logger.debug("useForMetrics");
        try {
            AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
            Functionality functionality = decomposition.getFunctionality(functionalityName);
            functionality.changeFRUsedForMetrics(redesignName);
            decompositionRepository.save(decomposition);

            return new ResponseEntity<>(functionality, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
