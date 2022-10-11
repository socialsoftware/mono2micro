package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import java.io.InputStream;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

public interface AccessesDecomposition {
    String ACCESSES_DECOMPOSITION = "ACCESSES_DECOMPOSITION";
    boolean isOutdated();
    void setOutdated(boolean outdated);
    String getName();
    String getType();
    Strategy getStrategy();
    Similarity getSimilarity();
    Map<String, Cluster> getClusters();
    Map<String, Functionality> getFunctionalities();
    void setFunctionalities(Map<String, Functionality> functionalities);
    Map<Short, String> getEntityIDToClusterName();
    Map<String, Object> getMetrics();

    void setMetrics(Map<String, Object> metrics);

    void addCluster(Cluster cluster);
    Cluster removeCluster(String clusterName);

    Cluster getCluster(String clusterName);

    boolean clusterNameExists(String clusterName);

    default Functionality getFunctionality(String functionalityName) {
        Functionality c = getFunctionalities().get(functionalityName.replaceAll("\\.", "_"));

        if (c == null) throw new Error("Functionality with name: " + functionalityName + " not found");

        return c;
    }

    default boolean functionalityExists(String functionalityName) {
        return getFunctionalities().containsKey(functionalityName.replaceAll("\\.", "_"));
    }

    default void addFunctionality(Functionality functionality) {
        getFunctionalities().put(functionality.getName().replaceAll("\\.", "_"), functionality);
    }

    default void transferCouplingDependencies(Set<Short> entities, String currentClusterName, String newClusterName) {
        for (Cluster cluster : getClusters().values())
            ((SciPyCluster) cluster).transferCouplingDependencies(entities, currentClusterName, newClusterName);
    }


    default void setupFunctionalities(
            GridFsService gridFsService,
            FunctionalityRepository functionalityRepository,
            InputStream inputFilePath,
            String profile,
            int tracesMaxLimit,
            Constants.TraceType traceType
    ) throws Exception {
        AccessesRepresentation accesses = (AccessesRepresentation) getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        Set<String> profileFunctionalities = accesses.getProfile(profile);

        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(inputFilePath, tracesMaxLimit);
        Map<String, DirectedAcyclicGraph<LocalTransaction, DefaultEdge>> localTransactionsGraphs = new HashMap<>();
        List<Functionality> newFunctionalities = new ArrayList<>();

        Iterator<String> availableFunctionalities = iter.getFunctionalitiesNames();
        while (availableFunctionalities.hasNext()) {
            String functionalityName = availableFunctionalities.next();
            if (!profileFunctionalities.contains(functionalityName) || this.functionalityExists(functionalityName))
                continue;

            iter.getFunctionalityWithName(functionalityName);
            Functionality functionality = new Functionality(getName(), functionalityName);

            // Get traces according to trace type
            List<TraceDto> traceDtos = iter.getTracesByType(traceType);
            functionality.setTraces(traceDtos);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionGraph = functionality.createLocalTransactionGraph(
                    getEntityIDToClusterName()
            );

            localTransactionsGraphs.put(functionality.getName(), localTransactionGraph);

            findClusterDependencies(localTransactionGraph);

            newFunctionalities.add(functionality);
            this.addFunctionality(functionality);
        }

        System.out.println("Calculating functionality metrics...");

        for (Functionality functionality: newFunctionalities) {
            functionality.defineFunctionalityType();
            functionality.calculateMetrics(this);

            // Functionality Redesigns
            if (gridFsService != null) {
                FunctionalityService.createFunctionalityRedesign(
                        gridFsService,
                        this,
                        functionality,
                        Constants.DEFAULT_REDESIGN_NAME,
                        true,
                        localTransactionsGraphs.get(functionality.getName()));
            }
        }
        if (functionalityRepository != null)
            functionalityRepository.saveAll(newFunctionalities);
    }

    default void findClusterDependencies(DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
        Set<LocalTransaction> allLocalTransactions = localTransactionsGraph.vertexSet();

        for (LocalTransaction lt : allLocalTransactions) {
            // ClusterDependencies
            String clusterName = lt.getClusterName();
            if (!clusterName.equals("-1")) { // not root node
                SciPyCluster fromCluster = (SciPyCluster) getCluster(clusterName);

                List<LocalTransaction> nextLocalTransactions = successorListOf(localTransactionsGraph, lt);

                for (LocalTransaction nextLt : nextLocalTransactions)
                    fromCluster.addCouplingDependencies(nextLt.getClusterName(), nextLt.getFirstAccessedEntityIDs());
            }
        }
    }
}
