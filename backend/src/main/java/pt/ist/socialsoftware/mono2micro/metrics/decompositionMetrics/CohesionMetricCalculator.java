package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CohesionMetricCalculator extends DecompositionMetricCalculator {
    public static final String COHESION = "Cohesion";

    @Override
    public String getType() {
        return COHESION;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) { // It might be extended to adapt for other kinds of decompositions without functionalities
        Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);
        return calculateMetric(decomposition, clustersFunctionalities);
    }

    public Double calculateMetric(Decomposition decomposition, Map<String, List<Functionality>> clustersFunctionalities) {
        double totalCohesion = 0;

        for (Cluster cluster : decomposition.getClusters().values()) {
            List<Functionality> functionalitiesThatAccessThisCluster = clustersFunctionalities.get(cluster.getName());

            double clusterCohesion = 0;

            for (Functionality functionality : functionalitiesThatAccessThisCluster) {
                double numberEntitiesTouched = 0;

                Set<Short> functionalityEntities = functionality.getEntities().keySet();

                for (short entityID : functionalityEntities)
                    if (cluster.containsElement(entityID))
                        numberEntitiesTouched++;

                clusterCohesion += numberEntitiesTouched / cluster.getElements().size();
            }

            if (!functionalitiesThatAccessThisCluster.isEmpty())
                clusterCohesion /= functionalitiesThatAccessThisCluster.size();
            clusterCohesion = BigDecimal.valueOf(clusterCohesion).setScale(3, RoundingMode.HALF_UP).doubleValue();
            cluster.addMetric(COHESION, clusterCohesion);
            totalCohesion += clusterCohesion;
        }

        int graphClustersAmount = decomposition.getClusters().size();

        return BigDecimal.valueOf(totalCohesion / graphClustersAmount)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}