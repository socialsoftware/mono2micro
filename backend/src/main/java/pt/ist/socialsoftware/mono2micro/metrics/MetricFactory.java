package pt.ist.socialsoftware.mono2micro.metrics;

public class MetricFactory {
    private static MetricFactory factory = null;

    public static MetricFactory getFactory() {
        if (factory == null)
            factory = new MetricFactory();
        return factory;
    }

    public Metric getMetric(String metricType) {
        switch (metricType) {
            case Metric.MetricType.COHESION:
                return new CohesionMetric();
            case Metric.MetricType.COMPLEXITY:
                return new ComplexityMetric();
            case Metric.MetricType.COUPLING:
                return new CouplingMetric();
            case Metric.MetricType.PERFORMANCE:
                return new PerformanceMetric();
            case Metric.MetricType.SYSTEM_COMPLEXITY:
                return new SystemComplexityMetric();
            case Metric.MetricType.FUNCTIONALITY_COMPLEXITY:
                return new FunctionalityComplexityMetric();
            case Metric.MetricType.INCONSISTENCY_COMPLEXITY:
                return new InconsistencyComplexityMetric();
            default:
                throw new RuntimeException("The type \"" + metricType + "\" is not a valid metric.");
        }
    }
}
