package pt.ist.socialsoftware.mono2micro.metrics.metricService;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

@Service
public class PerformanceMetricService { // the average of the number of hops between clusters for all traces
    public Double calculateMetric(AccessesSciPyDecomposition decomposition) {
        double performance = 0;

        for (Functionality functionality : decomposition.getFunctionalities().values()) {
            Double performanceMetric = (Double) functionality.getMetric(MetricType.PERFORMANCE);
            performance += performanceMetric;
        }

        int graphFunctionalitiesAmount = decomposition.getFunctionalities().size();

        return BigDecimal.valueOf(performance / graphFunctionalitiesAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double calculateMetric(Decomposition decomposition, Functionality functionality) {
        AccessesSciPyDecomposition accessesSciPyDecomposition = (AccessesSciPyDecomposition) decomposition;

        double functionalityPerformance = 0;
        for (TraceDto t : functionality.getTraces()) {
            List<ReducedTraceElementDto> traceElements = t.getElements();

            if (traceElements.size() > 0) {
                Utils.GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = Utils.getLocalTransactionsSequenceAndCalculateTracePerformance(
                        1,
                        null,
                        traceElements,
                        accessesSciPyDecomposition.getEntityIDToClusterName(),
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