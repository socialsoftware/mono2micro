package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.metrics.MetricCalculator;

public abstract class DecompositionMetricCalculator extends MetricCalculator {
    public abstract Object calculateMetric(Decomposition decomposition);
}
