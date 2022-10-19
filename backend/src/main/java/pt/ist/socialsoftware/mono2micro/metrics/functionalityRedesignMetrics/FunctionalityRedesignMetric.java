package pt.ist.socialsoftware.mono2micro.metrics.functionalityRedesignMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;

public abstract class FunctionalityRedesignMetric extends Metric {
    public abstract Object calculateMetric(
            Decomposition decomposition,
            AccessesInfo info,
            Functionality functionality,
            FunctionalityRedesign functionalityRedesign) throws Exception;
}
