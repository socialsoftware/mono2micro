package pt.ist.socialsoftware.mono2micro.metrics;

import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;

import java.util.Set;

public class InconsistencyComplexityMetric extends Metric<Integer> {
    public String getType() {
        return MetricType.INCONSISTENCY_COMPLEXITY;
    }

    // Decomposition Metric
    public void calculateMetric(Decomposition decomposition) {}

    // Functionality Metric
    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    // Functionality Redesign Metric
    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {
        calculateInconsistencyComplexity((AccessesSciPyDecomposition) decomposition, functionality, functionalityRedesign);
    }

    private void calculateInconsistencyComplexity(AccessesSciPyDecomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {
        if(functionality.getType() != FunctionalityType.QUERY)
            return;

        this.value = 0;

        Set<Short> entitiesRead = functionality.entitiesTouchedInAGivenMode((byte) 1);

        for (Functionality otherFunctionality : decomposition.getFunctionalities().values()) {
            if (!otherFunctionality.getName().equals(functionality.getName()) &&
                    otherFunctionality.getType() == FunctionalityType.SAGA){

                Set<Short> entitiesWritten = otherFunctionality.entitiesTouchedInAGivenMode((byte) 2);
                entitiesWritten.retainAll(entitiesRead);
                Set<String> clustersInCommon = otherFunctionality.clustersOfGivenEntities(entitiesWritten);

                if(clustersInCommon.size() > 1)
                    this.value += clustersInCommon.size();
            }
        }
    }
}