package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

public class CouplingMetric extends DecompositionMetric {
    public static final String COUPLING = "Coupling";

    public String getType() {
        return COUPLING;
    }

    public Double calculateMetric(Decomposition decomposition) {
        int graphClustersAmount = decomposition.getClusters().size();
        double coupling = 0;

        for (Cluster c1 : decomposition.getClusters().values()) {
            SciPyCluster cluster1 = (SciPyCluster) c1;
            double clusterCoupling = 0;
            Map<String, Set<Short>> couplingDependencies = cluster1.getCouplingDependencies();

            for (String cluster2 : couplingDependencies.keySet())
                clusterCoupling += (double) couplingDependencies.get(cluster2).size() / decomposition.getCluster(cluster2).getElements().size();

            clusterCoupling = graphClustersAmount == 1 ? 0 : clusterCoupling / (graphClustersAmount - 1);
            clusterCoupling = BigDecimal.valueOf(clusterCoupling)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            cluster1.addMetric(COUPLING, clusterCoupling);

            coupling += clusterCoupling;
        }

        return BigDecimal.valueOf(coupling / graphClustersAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
