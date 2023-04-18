package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

public class CouplingMetricCalculator extends DecompositionMetricCalculator {
    public static final String COUPLING = "Coupling";

    @Override
    public String getType() {
        return COUPLING;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        int graphClustersAmount = decomposition.getClusters().size();
        double coupling = 0;

        for (Cluster cluster1 : decomposition.getClusters().values()) {
            Partition partition1 = (Partition) cluster1;
            double clusterCoupling = 0;
            Map<String, Set<Short>> couplingDependencies = partition1.getCouplingDependencies();

            for (Map.Entry<String, Set<Short>> entry : couplingDependencies.entrySet())
                clusterCoupling += (double) entry.getValue().size() / decomposition.getCluster(entry.getKey()).getElements().size();

            clusterCoupling = graphClustersAmount == 1 ? 0 : clusterCoupling / (graphClustersAmount - 1);
            clusterCoupling = BigDecimal.valueOf(clusterCoupling)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            partition1.addMetric(COUPLING, clusterCoupling);

            coupling += clusterCoupling;
        }

        return BigDecimal.valueOf(coupling / graphClustersAmount)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
