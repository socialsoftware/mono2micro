package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ComplexityMetric extends DecompositionMetric {
    public static final String COMPLEXITY = "Complexity";

    public String getType() {
        return COMPLEXITY;
    }

    public Double calculateMetric(Decomposition decomposition) {
        Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities((AccessesDecomposition) decomposition);
        return calculateMetric((AccessesDecomposition) decomposition, clustersFunctionalities);
    }

    public static Double calculateMetric(AccessesDecomposition decomposition, Map<String, List<Functionality>> clustersFunctionalities) {
        double complexity;

        // Set cluster complexity
        for (Cluster c : decomposition.getClusters().values()) {
            SciPyCluster cluster = (SciPyCluster) c;
            List<Functionality> functionalitiesThatAccessThisCluster = clustersFunctionalities.get(cluster.getName());

            complexity = 0;

            for (Functionality functionality : functionalitiesThatAccessThisCluster) {
                Object complexityMetric = functionality.getMetric(COMPLEXITY);
                complexity += (Double) complexityMetric;
            }

            complexity /= functionalitiesThatAccessThisCluster.size();
            complexity = BigDecimal.valueOf(complexity).setScale(2, RoundingMode.HALF_UP).doubleValue();

            cluster.addMetric(COMPLEXITY, complexity);
        }

        // Return overall complexity
        complexity = 0;

        for (Functionality functionality : decomposition.getFunctionalities().values()) {
            Double complexityMetric = (Double) functionality.getMetric(COMPLEXITY);
            complexity += complexityMetric;
        }

        return BigDecimal.valueOf(complexity / decomposition.getFunctionalities().size())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
