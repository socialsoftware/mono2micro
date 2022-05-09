package pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;

public class ClusteringAlgorithmFactory {
    private static ClusteringAlgorithmFactory factory = null;

    public static ClusteringAlgorithmFactory getFactory() {
        if (factory == null)
            factory = new ClusteringAlgorithmFactory();
        return factory;
    }

    public ClusteringAlgorithm getClusteringAlgorithm(String strategyType) {
        switch (strategyType) {
            case ACCESSES_SCIPY:
            case RECOMMENDATION_ACCESSES_SCIPY: // Uses the same generator since it generates recommendations of the same type
                return new SciPyClusteringAlgorithm();
            default:
                throw new RuntimeException("The type \"" + strategyType + "\" is not a valid strategy.");
        }
    }
}
