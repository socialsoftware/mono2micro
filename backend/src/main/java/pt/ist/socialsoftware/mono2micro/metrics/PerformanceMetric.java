package pt.ist.socialsoftware.mono2micro.metrics;

import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

public class PerformanceMetric extends Metric<Float> { // the average of the number of hops between clusters for all traces
    public String getType() {
        return MetricType.PERFORMANCE;
    }

    public void calculateMetric(Decomposition decomposition) {
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                this.value = calculateMetricAccessesSciPy((AccessesSciPyDecomposition) decomposition);
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    private float calculateMetricAccessesSciPy(AccessesSciPyDecomposition decomposition) {
        float performance = 0;

        for (Functionality functionality : decomposition.getFunctionalities().values()) {
            PerformanceMetric performanceMetric = (PerformanceMetric) functionality.searchMetricByType(MetricType.PERFORMANCE);
            performance += performanceMetric.getValue();
        }

        int graphFunctionalitiesAmount = decomposition.getFunctionalities().size();

        return BigDecimal.valueOf(performance / graphFunctionalitiesAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality) {
        AccessesSciPyDecomposition accessesSciPyDecomposition = (AccessesSciPyDecomposition) decomposition;

        float functionalityPerformance = 0;
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

        this.value = BigDecimal.valueOf(functionalityPerformance).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {}
}