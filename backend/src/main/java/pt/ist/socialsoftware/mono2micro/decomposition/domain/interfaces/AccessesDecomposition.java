package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.Map;
import java.util.Set;

public interface AccessesDecomposition {
    String ACCESSES_DECOMPOSITION = "ACCESSES_DECOMPOSITION";
    boolean isOutdated();
    void setOutdated(boolean outdated);
    String getName();
    String getStrategyType();
    Strategy getStrategy();
    Similarity getSimilarity();
    Map<String, Cluster> getClusters();
    Map<String, Functionality> getFunctionalities();
    void setFunctionalities(Map<String, Functionality> functionalities);
    Map<Short, String> getEntityIDToClusterName();
    Map<String, Object> getMetrics();

    void setMetrics(Map<String, Object> metrics);
    void addMetric(String metricType, Object metricValue);

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
}
