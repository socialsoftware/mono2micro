package pt.ist.socialsoftware.mono2micro.metrics.decompositionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.metricService.*;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@Service
public class MetricService {
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
    @Autowired
    TSRMetricService tsrMetricService;

    public void calculateMetrics(Decomposition d) {
        if (d.containsImplementation(ACCESSES_DECOMPOSITION)) {
            AccessesDecomposition decomposition = (AccessesDecomposition) d;
            Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);

            decomposition.addMetric(MetricType.COMPLEXITY, complexityMetricService.calculateMetric(decomposition, clustersFunctionalities));
            decomposition.addMetric(MetricType.PERFORMANCE, performanceMetricService.calculateMetric(decomposition));
            decomposition.addMetric(MetricType.COHESION, cohesionMetricService.calculateMetric(decomposition, clustersFunctionalities));
            decomposition.addMetric(MetricType.COUPLING, couplingMetricService.calculateMetric(decomposition));
        }
        if (d.containsImplementation(REPOSITORY_DECOMPOSITION)) {
            RepositoryDecomposition decomposition = (RepositoryDecomposition) d;
            decomposition.addMetric(MetricType.TSR, tsrMetricService.calculateMetric(decomposition));
        }
        // ADD NEW ENTRIES HERE
    }

    // USED BY FUNCTIONALITIES
    public void calculateMetrics(AccessesDecomposition decomposition, Functionality functionality) {
        functionality.addMetric(MetricType.COMPLEXITY, complexityMetricService.calculateMetric(decomposition, functionality));
        functionality.addMetric(MetricType.PERFORMANCE, performanceMetricService.calculateMetric(decomposition, functionality));
    }

    // USED BY FUNCTIONALITY REDESIGNS
    public void calculateMetrics(AccessesDecomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        if (functionality.getType() == FunctionalityType.SAGA) {
            functionalityRedesign.addMetric(MetricType.SYSTEM_COMPLEXITY, systemComplexityMetricService.calculateMetric(decomposition, functionality, functionalityRedesign));
            functionalityRedesign.addMetric(MetricType.FUNCTIONALITY_COMPLEXITY, functionalityComplexityMetricService.calculateMetric(decomposition, functionality, functionalityRedesign));
        }
        else functionalityRedesign.addMetric(MetricType.INCONSISTENCY_COMPLEXITY, inconsistencyComplexityMetricService.calculateMetric(decomposition, functionality));
    }
}
