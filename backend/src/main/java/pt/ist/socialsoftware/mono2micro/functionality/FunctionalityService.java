package pt.ist.socialsoftware.mono2micro.functionality;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.FileManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionService.AccessesSciPyMetricService;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import javax.naming.NameAlreadyBoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    AccessesSciPyMetricService metricService;

    @Autowired
    GridFsService gridFsService;

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
            Functionality functionality = new Functionality(decomposition.getName(), functionalityName);

            // Get traces according to trace type
            List<TraceDto> traceDtos = iter.getTracesByType(traceType);
            functionality.setTraces(traceDtos);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionGraph = functionality.createLocalTransactionGraph(
                    decomposition.getEntityIDToClusterName()
            );

            localTransactionsGraphs.put(functionality.getName(), localTransactionGraph);

            findClusterDependencies(decomposition, localTransactionGraph);

            newFunctionalities.add(functionality);
            decomposition.addFunctionality(functionality);
        }

        System.out.println("Calculating functionality metrics...");

        for (Functionality functionality: newFunctionalities) {
            functionality.defineFunctionalityType();
            metricService.calculateMetrics(decomposition, functionality);

            // Functionality Redesigns
            if (calculateRedesigns) {
                createFunctionalityRedesign(
                        decomposition,
                        functionality,
                        Constants.DEFAULT_REDESIGN_NAME,
                        true,
                        localTransactionsGraphs.get(functionality.getName()));
            }
        }
        functionalityRepository.saveAll(newFunctionalities);
    }

    public FunctionalityRedesign createFunctionalityRedesign(
            AccessesSciPyDecomposition decomposition,
            Functionality functionality,
            String name,
            boolean usedForMetrics,
            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
    ) throws IOException {
        FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);
        if (usedForMetrics)
            functionality.setFunctionalityRedesignNameUsedForMetrics(name);

        LocalTransaction graphRootLT = new LocalTransaction(0, "-1");

        graphRootLT.setName(functionality.getName());

        Iterator<LocalTransaction> iterator = new BreadthFirstIterator<>(
                localTransactionsGraph,
                graphRootLT
        );

        while (iterator.hasNext()) {
            LocalTransaction lt = iterator.next();
            lt.setRemoteInvocations(new ArrayList<>());

            List<LocalTransaction> graphChildrenLTs = successorListOf(
                    localTransactionsGraph,
                    lt
            );

            for (LocalTransaction childLT : graphChildrenLTs) {
                lt.addRemoteInvocations(childLT.getId());
                childLT.setName(childLT.getId() + ": " + childLT.getClusterName());
            }

            functionalityRedesign.getRedesign().add(lt);
            if(lt.getId() != 0){
                for(AccessDto accessDto : lt.getClusterAccesses()){
                    if(functionality.getEntitiesPerCluster().containsKey(lt.getClusterName())){
                        functionality.getEntitiesPerCluster().get(lt.getClusterName()).add(accessDto.getEntityID());
                    } else {
                        Set<Short> entities = new HashSet<>();
                        entities.add(accessDto.getEntityID());
                        functionality.getEntitiesPerCluster().put(lt.getClusterName(), entities);
                    }
                }
            }
        }

        metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);
        saveFunctionalityRedesign(functionality, functionalityRedesign);
        functionality.addFunctionalityRedesign(functionalityRedesign.getName(), functionality.getId() + functionalityRedesign.getName());
        return functionalityRedesign;
    }

    public void saveFunctionalityRedesign(Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        gridFsService.saveFile(
                FileManager.getInstance().getFunctionalityRedesignAsJSON(functionalityRedesign),
                functionality.getId() + functionalityRedesign.getName()
        );
    }

    public void updateFunctionalityRedesign(Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        gridFsService.replaceFile(
                FileManager.getInstance().getFunctionalityRedesignAsJSON(functionalityRedesign),
                functionality.getId() + functionalityRedesign.getName()
        );
    }

    public FunctionalityRedesign getFunctionalityRedesign(Functionality functionality, String redesignName) throws IOException {
        return FileManager.getInstance().getFunctionalityRedesign(gridFsService.getFile(functionality.getFunctionalityRedesignFileName(redesignName)));
    }

    public FunctionalityRedesign getFunctionalityRedesignUsedForMetrics(Functionality functionality) throws IOException {
        return FileManager.getInstance().getFunctionalityRedesign(
                gridFsService.getFile(functionality.getFunctionalityRedesignFileName(functionality.getFunctionalityRedesignNameUsedForMetrics()))
        );
    }

    public List<FunctionalityRedesign> getFunctionalityRedesigns(Functionality functionality) {
        return functionality.getFunctionalityRedesigns().values().stream().map(fileName -> {
            try {
                return FileManager.getInstance().getFunctionalityRedesign(gridFsService.getFile(fileName));
            } catch (IOException e) { throw new RuntimeException(e); }
        }).collect(Collectors.toList());
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

        if (!functionality.containsFunctionalityRedesignName(Constants.DEFAULT_REDESIGN_NAME)) {
            FunctionalityRedesign functionalityRedesign = createFunctionalityRedesign(
                    decomposition,
                    functionality,
                    Constants.DEFAULT_REDESIGN_NAME,
                    true,
                    functionality.createLocalTransactionGraphFromScratch(
                            sourceService.getSourceFileAsInputStream(source.getName()),
                            strategy.getTracesMaxLimit(),
                            strategy.getTraceType(),
                            decomposition.getEntityIDToClusterName())
            );
            metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);
            functionalityRepository.save(functionality);
        }
        return functionality;
    }

    public Functionality addCompensating(String decompositionName, String functionalityName, String redesignName, HashMap<String, Object> data) throws Exception {
        int fromID = (Integer) data.get("fromID");
        String clusterName = (String) data.get("cluster");
        ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.addCompensating(clusterName, accesses, fromID);
        metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality sequenceChange(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        String localTransactionID = data.get("localTransactionID");
        String newCaller = data.get("newCaller");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.sequenceChange(localTransactionID, newCaller);
        metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality dcgi(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        String fromClusterName = data.get("fromCluster");
        String toClusterName = data.get("toCluster");
        String localTransactions = data.get("localTransactions");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.dcgi(fromClusterName, toClusterName, localTransactions);
        metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
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
            if(functionality.containsFunctionalityRedesignName(newRedesignName.get()))
                throw new NameAlreadyBoundException();

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.definePivotTransaction(Integer.parseInt(transactionID));
        metricService.calculateMetrics(decomposition, functionality, functionalityRedesign);

        if(newRedesignName.isPresent()) {
            gridFsService.deleteFile(functionality.getFunctionalityRedesigns().remove(redesignName));
            functionalityRedesign.setName(newRedesignName.get());
            saveFunctionalityRedesign(functionality, functionalityRedesign);
            functionality.addFunctionalityRedesign(functionalityRedesign.getName(), functionality.getId() + functionalityRedesign.getName());
            functionality.setFunctionalityRedesignNameUsedForMetrics(functionalityRedesign.getName());

            Source source = strategy.getCodebase().getSourceByType(ACCESSES);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                    .createLocalTransactionGraphFromScratch(
                            sourceService.getSourceFileAsInputStream(source.getName()),
                            strategy.getTracesMaxLimit(),
                            strategy.getTraceType(),
                            decomposition.getEntityIDToClusterName());

            createFunctionalityRedesign(
                    decomposition,
                    functionality,
                    Constants.DEFAULT_REDESIGN_NAME,
                    false,
                    functionalityLocalTransactionsGraph
            );
        }
        else updateFunctionalityRedesign(functionality, functionalityRedesign);

        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality changeLTName(String decompositionName, String functionalityName, String redesignName, String transactionID, String newName)
            throws IOException
    {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.changeLTName(transactionID, newName);
        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality deleteRedesign(String decompositionName, String functionalityName, String redesignName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.removeFunctionalityRedesign(redesignName);
        gridFsService.deleteFile(functionality.getFunctionalityRedesignFileName(redesignName));
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality useForMetrics(String decompositionName, String functionalityName, String redesignName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.setFunctionalityRedesignNameUsedForMetrics(redesignName);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public void deleteFunctionality(Functionality functionality) {
        functionality.getFunctionalityRedesigns().values().forEach(fileName -> gridFsService.deleteFile(fileName));
        functionalityRepository.delete(functionality);
    }

    public void deleteFunctionalities(Iterable<Functionality> functionalities) {
        for (Functionality functionality: functionalities)
            functionality.getFunctionalityRedesigns().values().forEach(fileName -> gridFsService.deleteFile(fileName));
        functionalityRepository.deleteAll(functionalities);
    }
}