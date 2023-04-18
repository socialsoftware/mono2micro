package pt.ist.socialsoftware.mono2micro.metrics.functionalityMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.metrics.MetricCalculator;

public abstract class FunctionalityMetricCalculator extends MetricCalculator {
    public abstract Object calculateMetric(AccessesInformation accessesInformation, Decomposition decomposition, Functionality functionality) throws Exception;
}
