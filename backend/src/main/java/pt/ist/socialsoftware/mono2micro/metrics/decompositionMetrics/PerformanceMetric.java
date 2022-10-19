package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.AccessesInfo.ACCESSES_INFO;

public class PerformanceMetric extends DecompositionMetric { // the average of the number of hops between clusters for all traces
    public static final String PERFORMANCE = "Performance";

    @Override
    public String getType() {
        return PERFORMANCE;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        AccessesInfo accessesInfo = (AccessesInfo) decomposition.getRepresentationInformationByType(ACCESSES_INFO);
        double performance = 0;

        for (Functionality functionality : accessesInfo.getFunctionalities().values()) {
            Double performanceMetric = (Double) functionality.getMetric(PERFORMANCE);
            performance += performanceMetric;
        }

        int graphFunctionalitiesAmount = accessesInfo.getFunctionalities().size();

        return BigDecimal.valueOf(performance / graphFunctionalitiesAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}