package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;

public abstract class DecompositionMetric extends Metric {
    public abstract Object calculateMetric(Decomposition decomposition);
}
