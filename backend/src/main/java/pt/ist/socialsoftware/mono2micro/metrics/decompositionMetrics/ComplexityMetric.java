package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;

public class ComplexityMetric extends DecompositionMetric {
    public static final String COMPLEXITY = "Complexity";

    @Override
    public String getType() {
        return COMPLEXITY;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);
        return calculateMetric(decomposition, clustersFunctionalities);
    }

    public static Double calculateMetric(Decomposition decomposition, Map<String, List<Functionality>> clustersFunctionalities) {
        AccessesInfo accessesInfo = (AccessesInfo) decomposition.getRepresentationInformationByType(ACCESSES_INFO);
        double complexity;

        // Set cluster complexity
        for (Cluster c : decomposition.getClusters().values()) {
            Partition cluster = (Partition) c;
            List<Functionality> functionalitiesThatAccessThisCluster = clustersFunctionalities.get(cluster.getName());

            complexity = 0;

            for (Functionality functionality : functionalitiesThatAccessThisCluster) {
                Object complexityMetric = functionality.getMetric(COMPLEXITY);
                complexity += (Double) complexityMetric;
            }

            if (!functionalitiesThatAccessThisCluster.isEmpty())
                complexity /= functionalitiesThatAccessThisCluster.size();
            complexity = BigDecimal.valueOf(complexity).setScale(3, RoundingMode.HALF_UP).doubleValue();

            cluster.addMetric(COMPLEXITY, complexity);
        }

        // Return overall complexity
        complexity = 0;

        for (Functionality functionality : accessesInfo.getFunctionalities().values()) {
            Double complexityMetric = (Double) functionality.getMetric(COMPLEXITY);
            complexity += complexityMetric;
        }

        return BigDecimal.valueOf(complexity / accessesInfo.getFunctionalities().size())
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
