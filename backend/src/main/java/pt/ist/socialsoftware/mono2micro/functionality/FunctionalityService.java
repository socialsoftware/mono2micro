package pt.ist.socialsoftware.mono2micro.functionality;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import javax.naming.NameAlreadyBoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;

@Service
public class FunctionalityService {
    @Autowired
    AccessesSciPyDecompositionRepository decompositionRepository;

    @Autowired
    SourceService sourceService;

    @Autowired
    FunctionalityRepository functionalityRepository;

    public void setupFunctionalities(
            AccessesSciPyDecomposition decomposition,
            InputStream inputFilePath,
            Set<String> profileFunctionalities,
            int tracesMaxLimit,
            Constants.TraceType traceType,
            boolean calculateRedesigns
    ) throws Exception {
        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(inputFilePath, tracesMaxLimit);
        Map<String, DirectedAcyclicGraph<LocalTransaction, DefaultEdge>> localTransactionsGraphs = new HashMap<>();
        List<Functionality> newFunctionalities = new ArrayList<>();

        Iterator<String> availableFunctionalities = iter.getFunctionalitiesNames();
        while (availableFunctionalities.hasNext()) {
            String functionalityName = availableFunctionalities.next();
            if (!profileFunctionalities.contains(functionalityName) || decomposition.functionalityExists(functionalityName))
                continue;

            iter.getFunctionalityWithName(functionalityName);
            Functionality functionality = new Functionality(functionalityName);

            // Get traces according to trace type
            List<TraceDto> traceDtos = iter.getTracesByType(traceType);
            functionality.setTraces(traceDtos);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionGraph = functionality.createLocalTransactionGraph(decomposition.getEntityIDToClusterName());

            localTransactionsGraphs.put(functionality.getName(), localTransactionGraph);

            findClusterDependencies(decomposition, localTransactionGraph);

            newFunctionalities.add(functionality);
            decomposition.addFunctionality(functionality);
        }

        System.out.println("Calculating functionality metrics...");

        for (Functionality functionality: newFunctionalities) {
            functionality.defineFunctionalityType();
            functionality.calculateMetrics(decomposition);

            // Functionality Redesigns
            if (calculateRedesigns) {
                FunctionalityRedesign functionalityRedesign = functionality.createFunctionalityRedesign(
                        Constants.DEFAULT_REDESIGN_NAME,
                        true,
                        localTransactionsGraphs.get(functionality.getName()));

                functionalityRedesign.calculateMetrics(decomposition, functionality);
            }
        }
        functionalityRepository.saveAll(newFunctionalities);
    }

    public void findClusterDependencies(AccessesSciPyDecomposition decomposition, DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
        Set<LocalTransaction> allLocalTransactions = localTransactionsGraph.vertexSet();

        for (LocalTransaction lt : allLocalTransactions) {
            // ClusterDependencies
            String clusterName = lt.getClusterName();
            if (!clusterName.equals("-1")) { // not root node
                Cluster fromCluster = decomposition.getCluster(clusterName);

                List<LocalTransaction> nextLocalTransactions = successorListOf(localTransactionsGraph, lt);

                for (LocalTransaction nextLt : nextLocalTransactions)
                    fromCluster.addCouplingDependencies(nextLt.getClusterName(), nextLt.getFirstAccessedEntityIDs());
            }
        }
    }

    public Functionality getOrCreateRedesign(String decompositionName, String functionalityName) throws IOException, JSONException {
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
                            decomposition.getEntityIDToClusterName())
            );
        }
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality addCompensating(String decompositionName, String functionalityName, String redesignName, HashMap<String, Object> data) throws Exception {
        int fromID = (Integer) data.get("fromID");
        String clusterName = (String) data.get("cluster");
        ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.addCompensating(clusterName, accesses, fromID);
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality sequenceChange(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        String localTransactionID = data.get("localTransactionID");
        String newCaller = data.get("newCaller");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.sequenceChange(localTransactionID, newCaller);
        functionalityRedesign.calculateMetrics(decomposition, functionality);

        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality dcgi(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        String fromClusterName = data.get("fromCluster");
        String toClusterName = data.get("toCluster");
        String localTransactions = data.get("localTransactions");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.dcgi(fromClusterName, toClusterName, localTransactions);
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality pivotTransaction(String decompositionName, String functionalityName, String redesignName, String transactionID, Optional<String> newRedesignName)
            throws Exception
    {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        if(newRedesignName.isPresent())
            if(!functionality.checkNameValidity(newRedesignName.get()))
                throw new NameAlreadyBoundException();

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
                            decomposition.getEntityIDToClusterName());

            functionality.createFunctionalityRedesign(
                    Constants.DEFAULT_REDESIGN_NAME,
                    false,
                    functionalityLocalTransactionsGraph
            );
        }

        functionalityRedesign = functionality.getFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME);
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality changeLTName(String decompositionName, String functionalityName, String redesignName, String transactionID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.getFunctionalityRedesign(redesignName).changeLTName(transactionID, newName);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public void deleteRedesign(String decompositionName, String functionalityName, String redesignName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.deleteRedesign(redesignName);
        functionalityRepository.save(functionality);
    }

    public Functionality useForMetrics(String decompositionName, String functionalityName, String redesignName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.changeFRUsedForMetrics(redesignName);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public void deleteFunctionality(Functionality functionality) {
        functionalityRepository.delete(functionality);
    }

    public void deleteFunctionalities(Iterable<Functionality> functionalities) {
        functionalityRepository.deleteAll(functionalities);
    }
}
