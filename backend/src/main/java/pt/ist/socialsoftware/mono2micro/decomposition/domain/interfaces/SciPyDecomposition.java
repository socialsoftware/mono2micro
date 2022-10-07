package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.Map;

public interface SciPyDecomposition {
    String SCIPY_DECOMPOSITION = "SCIPY_DECOMPOSITION";
    boolean containsImplementation(String implementation);
    String getStrategyType();
    void setStrategy(Strategy strategy);
    void setSimilarity(Similarity similarity);
    Similarity getSimilarity();
    String getName();
    void setName(String name);
    Map<String, Object> getMetrics();
    boolean isExpert();
    void setExpert(boolean expert);
    Map<String, Cluster> getClusters();
    void addCluster(Cluster cluster);
    int maxClusterSize();
    Map<Short, String> getEntityIDToClusterName();
    void setSilhouetteScore(double silhouetteScore);
}
