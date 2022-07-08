package pt.ist.socialsoftware.mono2micro.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CohesionMetric.class, name = Metric.MetricType.COHESION),
        @JsonSubTypes.Type(value = ComplexityMetric.class, name = Metric.MetricType.COMPLEXITY),
        @JsonSubTypes.Type(value = CouplingMetric.class, name = Metric.MetricType.COUPLING),
        @JsonSubTypes.Type(value = PerformanceMetric.class, name = Metric.MetricType.PERFORMANCE),
        @JsonSubTypes.Type(value = SystemComplexityMetric.class, name = Metric.MetricType.SYSTEM_COMPLEXITY),
        @JsonSubTypes.Type(value = FunctionalityComplexityMetric.class, name = Metric.MetricType.FUNCTIONALITY_COMPLEXITY),
        @JsonSubTypes.Type(value = InconsistencyComplexityMetric.class, name = Metric.MetricType.INCONSISTENCY_COMPLEXITY)
})
public abstract class Metric<Type> {
    protected Type value;

    public abstract void calculateMetric(Decomposition decomposition) throws Exception;

    // Some metrics might not have metrics for functionalities
    public abstract void calculateMetric(Decomposition decomposition, Functionality functionality) throws Exception;

    // Some metrics might not have metrics for functionalities redesigns
    public abstract void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) throws Exception;

    @JsonIgnore
    public abstract String getType();

    public Type getValue() {
        return value;
    }

    public void setValue(Type value) {
        this.value = value;
    }

    public static class MetricType {
        // Used by decompositions and functionalities
        public static final String COMPLEXITY = "Complexity";
        public static final String PERFORMANCE = "Performance";
        // Used by decompositions
        public static final String COHESION = "Cohesion";
        public static final String COUPLING = "Coupling";
        // Used by functionality redesigns
        public static final String SYSTEM_COMPLEXITY = "System Complexity";
        public static final String FUNCTIONALITY_COMPLEXITY = "Functionality Complexity";
        public static final String INCONSISTENCY_COMPLEXITY = "Inconsistency Complexity";
    }
}