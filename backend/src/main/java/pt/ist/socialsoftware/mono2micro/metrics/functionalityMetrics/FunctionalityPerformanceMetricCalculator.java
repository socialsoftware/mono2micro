package pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionalityPerformanceMetricCalculator extends FunctionalityMetricCalculator {
    public static final String PERFORMANCE = "Performance";

    @Override
    public String getType() {
        return PERFORMANCE;
    }

    @Override
    public Double calculateMetric(AccessesInformation accessesInformation, Decomposition decomposition, Functionality functionality) {
        double functionalityPerformance = 0;
        Map<Short, String> entityIDToClusterName = decomposition.getEntityIDToClusterName();
        for (TraceDto t : functionality.getTraces()) {
            List<ReducedTraceElementDto> traceElements = t.getElements();

            if (traceElements.size() > 0) {
                Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
                        1,
                        null,
                        traceElements,
                        entityIDToClusterName,
                        new HashMap<>(),
                        0,
                        traceElements.size()
                );

                functionalityPerformance += result.performance;
            }
        }
        functionalityPerformance /= functionality.getTraces().size();

        return BigDecimal.valueOf(functionalityPerformance).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
