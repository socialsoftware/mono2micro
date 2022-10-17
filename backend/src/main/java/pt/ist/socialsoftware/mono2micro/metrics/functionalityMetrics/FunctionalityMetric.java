package pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;

public abstract class FunctionalityMetric extends Metric {
    public abstract Object calculateMetric(AccessesDecomposition decomposition, Functionality functionality) throws Exception;
}
