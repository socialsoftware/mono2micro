package pt.ist.socialsoftware.mono2micro.domain.metrics;

import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
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
            case Strategy.StrategyType.ACCESSES_SCIPY:
                this.value = calculateMetricAccessesSciPy((AccessesSciPyDecomposition) decomposition);
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    private float calculateMetricAccessesSciPy(AccessesSciPyDecomposition decomposition) {
        System.out.println("Calculating decomposition performance...");

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
                        accessesSciPyDecomposition.getEntityIDToClusterID(),
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