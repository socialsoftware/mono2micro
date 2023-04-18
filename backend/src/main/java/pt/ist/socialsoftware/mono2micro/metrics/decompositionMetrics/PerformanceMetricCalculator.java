package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.ACCESSES_TYPE;

public class PerformanceMetricCalculator extends DecompositionMetricCalculator { // the average of the number of hops between clusters for all traces
    public static final String PERFORMANCE = "Performance";

    @Override
    public String getType() {
        return PERFORMANCE;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        double performance = 0;

        for (Functionality functionality : accessesInformation.getFunctionalities().values()) {
            Double performanceMetric = (Double) functionality.getMetric(PERFORMANCE);
            performance += performanceMetric;
        }

        int graphFunctionalitiesAmount = accessesInformation.getFunctionalities().size();

        return BigDecimal.valueOf(performance / graphFunctionalitiesAmount)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}