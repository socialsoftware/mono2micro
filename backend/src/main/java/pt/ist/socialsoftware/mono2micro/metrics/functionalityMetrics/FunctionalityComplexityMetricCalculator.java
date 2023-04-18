package pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.util.*;

public class FunctionalityComplexityMetricCalculator extends FunctionalityMetricCalculator {
    public static final String COMPLEXITY = "Complexity";

    @Override
    public String getType() {
        return COMPLEXITY;
    }

    @Override
    public Double calculateMetric(AccessesInformation accessesInformation, Decomposition decomposition, Functionality functionality) {
        double value;

        // Since metric calculation is always done during the creation of the functionalities, we can use createLocalTransactionGraph,
        // otherwise, if traces == null, use createLocalTransactionGraphFromScratch
        DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph = functionality.createLocalTransactionGraph(decomposition.getEntityIDToClusterName());

        Map<String, Set<Cluster>> functionalityClusters = Utils.getFunctionalitiesClusters(
                decomposition.getEntityIDToClusterName(),
                decomposition.getClusters(),
                accessesInformation.getFunctionalities().values());

        Set<LocalTransaction> allLocalTransactions = localTransactionsGraph.vertexSet();

        if (functionalityClusters.get(functionality.getName()).size() == 1) {
            value = 0F;
        } else {
            // < entity + mode, List<functionalityName>> functionalitiesThatTouchSameEntities for a given mode
            Map<String, List<String>> cache = new HashMap<>();

            double functionalityComplexity = 0;

            for (LocalTransaction lt : allLocalTransactions) {
                // ClusterDependencies
                String clusterName = lt.getClusterName();
                if (!clusterName.equals("-1")) { // not root node

                    Set<String> functionalitiesThatTouchSameEntities = new HashSet<>();
                    Set<AccessDto> clusterAccesses = lt.getClusterAccesses();

                    for (AccessDto a : clusterAccesses) {
                        short entityID = a.getEntityID();
                        byte mode = a.getMode();

                        String key = String.join("-", String.valueOf(entityID), String.valueOf(mode));
                        List<String> functionalitiesThatTouchThisEntityAndMode = cache.get(key);

                        if (functionalitiesThatTouchThisEntityAndMode == null) {
                            functionalitiesThatTouchThisEntityAndMode = costOfAccess(
                                    functionality.getName(),
                                    entityID,
                                    mode,
                                    accessesInformation.getFunctionalities().values(),
                                    functionalityClusters
                            );

                            cache.put(key, functionalitiesThatTouchThisEntityAndMode);
                        }

                        functionalitiesThatTouchSameEntities.addAll(functionalitiesThatTouchThisEntityAndMode);
                    }

                    functionalityComplexity += functionalitiesThatTouchSameEntities.size();
                }
            }
            value = functionalityComplexity;
        }

        return value;
    }

    private static List<String> costOfAccess(
            String functionalityName,
            short entityID,
            byte mode,
            Collection<Functionality> functionalities,
            Map<String, Set<Cluster>> functionalityClusters
    ) {
        List<String> functionalitiesThatTouchThisEntityAndMode = new ArrayList<>();

        for (Functionality otherFunctionality : functionalities) {
            String otherFunctionalityName = otherFunctionality.getName();

            if (!otherFunctionalityName.equals(functionalityName) && functionalityClusters.containsKey(otherFunctionalityName)) {
                Byte savedMode = otherFunctionality.getEntities().get(entityID);

                if (
                        savedMode != null &&
                                savedMode != mode &&
                                functionalityClusters.get(otherFunctionalityName).size() > 1
                ) {
                    functionalitiesThatTouchThisEntityAndMode.add(otherFunctionalityName);
                }
            }
        }

        return functionalitiesThatTouchThisEntityAndMode;
    }
}
