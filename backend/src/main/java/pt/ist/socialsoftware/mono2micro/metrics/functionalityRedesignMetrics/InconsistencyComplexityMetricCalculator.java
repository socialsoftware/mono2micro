package pt.ist.socialsoftware.mono2micro.metrics.functionalityRedesignMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;

import java.util.Set;

public class InconsistencyComplexityMetricCalculator extends FunctionalityRedesignMetricCalculator {
    public static final String INCONSISTENCY_COMPLEXITY = "Inconsistency Complexity";

    @Override
    public String getType() {
        return INCONSISTENCY_COMPLEXITY;
    }

    @Override
    public Integer calculateMetric(Decomposition decomposition, AccessesInformation accessesInformation, Functionality functionality, FunctionalityRedesign functionalityRedesign) {
        int value = 0;

        if(functionality.getType() != FunctionalityType.QUERY)
            return value;

        Set<Short> entitiesRead = functionality.entitiesTouchedInAGivenMode((byte) 1);

        for (Functionality otherFunctionality : accessesInformation.getFunctionalities().values()) {
            if (!otherFunctionality.getName().equals(functionality.getName()) &&
                    otherFunctionality.getType() == FunctionalityType.SAGA){

                Set<Short> entitiesWritten = otherFunctionality.entitiesTouchedInAGivenMode((byte) 2);
                entitiesWritten.retainAll(entitiesRead);
                Set<String> clustersInCommon = otherFunctionality.clustersOfGivenEntities(entitiesWritten);

                if(clustersInCommon.size() > 1)
                    value += clustersInCommon.size();
            }
        }
        return value;
    }
}