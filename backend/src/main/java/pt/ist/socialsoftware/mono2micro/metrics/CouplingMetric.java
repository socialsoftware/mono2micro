package pt.ist.socialsoftware.mono2micro.metrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

public class CouplingMetric extends Metric<Float> {
    public String getType() {
        return MetricType.COUPLING;
    }

    public void calculateMetric(Decomposition decomposition) {
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                this.value = calculateMetricAccessesSciPy((AccessesSciPyDecomposition) decomposition);
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    private float calculateMetricAccessesSciPy(AccessesSciPyDecomposition decomposition) {

        int graphClustersAmount = decomposition.getClusters().size();
        float coupling = 0;

        for (Cluster cluster1 : decomposition.getClusters().values()) {
            float clusterCoupling = 0;
            Map<String, Set<Short>> couplingDependencies = cluster1.getCouplingDependencies();

            for (String cluster2 : couplingDependencies.keySet())
                clusterCoupling += (float) couplingDependencies.get(cluster2).size() / decomposition.getCluster(cluster2).getEntities().size();

            clusterCoupling = graphClustersAmount == 1 ? 0 : clusterCoupling / (graphClustersAmount - 1);
            clusterCoupling = BigDecimal.valueOf(clusterCoupling)
                    .setScale(2, RoundingMode.HALF_UP)
                    .floatValue();

            cluster1.setCoupling(clusterCoupling);

            coupling += clusterCoupling;
        }

        return BigDecimal.valueOf(coupling / graphClustersAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {}
}
