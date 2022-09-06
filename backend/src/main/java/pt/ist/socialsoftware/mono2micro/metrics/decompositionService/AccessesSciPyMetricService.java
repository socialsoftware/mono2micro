package pt.ist.socialsoftware.mono2micro.metrics.decompositionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.metricService.*;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessesSciPyMetricService {
    @Autowired
    CohesionMetricService cohesionMetricService;
    @Autowired
    ComplexityMetricService complexityMetricService;
    @Autowired
    CouplingMetricService couplingMetricService;
    @Autowired
    FunctionalityComplexityMetricService functionalityComplexityMetricService;
    @Autowired
    InconsistencyComplexityMetricService inconsistencyComplexityMetricService;
    @Autowired
    PerformanceMetricService performanceMetricService;
    @Autowired
    SystemComplexityMetricService systemComplexityMetricService;

    public void calculateMetrics(AccessesSciPyDecomposition decomposition) {
        Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);

        decomposition.setMetrics(new HashMap<>());
        decomposition.addMetric(MetricType.COMPLEXITY, complexityMetricService.calculateMetric(decomposition, clustersFunctionalities));
        decomposition.addMetric(MetricType.PERFORMANCE, performanceMetricService.calculateMetric(decomposition));
        decomposition.addMetric(MetricType.COHESION, cohesionMetricService.calculateMetric(decomposition, clustersFunctionalities));
        decomposition.addMetric(MetricType.COUPLING, couplingMetricService.calculateMetric(decomposition));
    }

    public void calculateMetrics(AccessesSciPyDecomposition decomposition, Functionality functionality) {
        functionality.setMetrics(new HashMap<>());
        functionality.addMetric(MetricType.COMPLEXITY, complexityMetricService.calculateMetric(decomposition, functionality));
        functionality.addMetric(MetricType.PERFORMANCE, performanceMetricService.calculateMetric(decomposition, functionality));
    }

    public void calculateMetrics(AccessesSciPyDecomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        functionalityRedesign.setMetrics(new HashMap<>());
        if (functionality.getType() == FunctionalityType.SAGA) {
            functionalityRedesign.addMetric(MetricType.SYSTEM_COMPLEXITY, systemComplexityMetricService.calculateMetric(decomposition, functionality, functionalityRedesign));
            functionalityRedesign.addMetric(MetricType.FUNCTIONALITY_COMPLEXITY, functionalityComplexityMetricService.calculateMetric(decomposition, functionality, functionalityRedesign));
        }
        else functionalityRedesign.addMetric(MetricType.INCONSISTENCY_COMPLEXITY, inconsistencyComplexityMetricService.calculateMetric(decomposition, functionality));
    }
}