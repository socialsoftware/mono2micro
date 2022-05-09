package pt.ist.socialsoftware.mono2micro.domain.metrics;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

public class CohesionMetric extends Metric<Float> {
    public String getType() {
        return MetricType.COHESION;
    }

    public void calculateMetric(Decomposition decomposition) {
        switch (decomposition.getStrategyType()) {
            case Strategy.StrategyType.ACCESSES_SCIPY:
                this.value = calculateMetricAccessesSciPy((AccessesSciPyDecomposition) decomposition);
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    private float calculateMetricAccessesSciPy(AccessesSciPyDecomposition decomposition) {
        Map<Short, Set<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(
                decomposition.getEntityIDToClusterID(),
                decomposition.getClusters(),
                decomposition.getFunctionalities().values()
        );

        float totalCohesion = 0;

        for (Cluster cluster : decomposition.getClusters().values()) {
            Set<Functionality> FunctionalitiesThatAccessThisCluster = clustersFunctionalities.get(cluster.getID());

            float clusterCohesion = 0;

            for (Functionality functionality : FunctionalitiesThatAccessThisCluster) {
                float numberEntitiesTouched = 0;

                Set<Short> functionalityEntities = functionality.getEntities().keySet();

                for (short entityID : functionalityEntities)
                    if (cluster.containsEntity(entityID))
                        numberEntitiesTouched++;

                clusterCohesion += numberEntitiesTouched / cluster.getEntities().size();
            }

            clusterCohesion /= FunctionalitiesThatAccessThisCluster.size();
            clusterCohesion = BigDecimal.valueOf(clusterCohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
            cluster.setCohesion(clusterCohesion);
            totalCohesion += clusterCohesion;
        }

        int graphClustersAmount = decomposition.getClusters().size();

        return BigDecimal.valueOf(totalCohesion / graphClustersAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {}
}