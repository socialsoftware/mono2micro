package pt.ist.socialsoftware.mono2micro.metrics.functionalityRedesignMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.MetricCalculator;

public abstract class FunctionalityRedesignMetricCalculator extends MetricCalculator {
    public abstract Object calculateMetric(
            Decomposition decomposition,
            AccessesInformation info,
            Functionality functionality,
            FunctionalityRedesign functionalityRedesign) throws Exception;
}
