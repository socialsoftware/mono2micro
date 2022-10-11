package pt.ist.socialsoftware.mono2micro.metrics.functionalityRedesignMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;

public abstract class FunctionalityRedesignMetric extends Metric {
    public abstract Object calculateMetric(
            AccessesDecomposition decomposition,
            Functionality functionality,
            FunctionalityRedesign functionalityRedesign) throws Exception;
}
