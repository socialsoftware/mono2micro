package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PerformanceMetric extends DecompositionMetric { // the average of the number of hops between clusters for all traces
    public static final String PERFORMANCE = "Performance";

    public String getType() {
        return PERFORMANCE;
    }

    public Double calculateMetric(Decomposition decomposition) {
        AccessesDecomposition d = (AccessesDecomposition) decomposition;
        double performance = 0;

        for (Functionality functionality : d.getFunctionalities().values()) {
            Double performanceMetric = (Double) functionality.getMetric(PERFORMANCE);
            performance += performanceMetric;
        }

        int graphFunctionalitiesAmount = d.getFunctionalities().size();

        return BigDecimal.valueOf(performance / graphFunctionalitiesAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}