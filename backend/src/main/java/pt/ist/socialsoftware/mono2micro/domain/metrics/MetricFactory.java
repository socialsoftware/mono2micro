package pt.ist.socialsoftware.mono2micro.domain.metrics;

import static pt.ist.socialsoftware.mono2micro.domain.metrics.Metric.MetricType.*;

public class MetricFactory {
    private static MetricFactory factory = null;

    public static MetricFactory getFactory() {
        if (factory == null)
            factory = new MetricFactory();
        return factory;
    }

    public Metric getMetric(String metricType) {
        switch (metricType) {
            case COHESION:
                return new CohesionMetric();
            case COMPLEXITY:
                return new ComplexityMetric();
            case COUPLING:
                return new CouplingMetric();
            case PERFORMANCE:
                return new PerformanceMetric();
            case SILHOUETTE_SCORE:
                return new SilhouetteScoreMetric();
            case SYSTEM_COMPLEXITY:
                return new SystemComplexityMetric();
            case FUNCTIONALITY_COMPLEXITY:
                return new FunctionalityComplexityMetric();
            case INCONSISTENCY_COMPLEXITY:
                return new InconsistencyComplexityMetric();
            default:
                throw new RuntimeException("The type \"" + metricType + "\" is not a valid metric.");
        }
    }
}
