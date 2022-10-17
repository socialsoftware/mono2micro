package pt.ist.socialsoftware.mono2micro.metrics.functionalityRedesignMetrics;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;

import java.util.Set;

public class InconsistencyComplexityMetric extends  FunctionalityRedesignMetric {
    public static final String INCONSISTENCY_COMPLEXITY = "Inconsistency Complexity";

    public String getType() {
        return INCONSISTENCY_COMPLEXITY;
    }

    public Integer calculateMetric(AccessesDecomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {
        int value = 0;

        if(functionality.getType() != FunctionalityType.QUERY)
            return value;

        Set<Short> entitiesRead = functionality.entitiesTouchedInAGivenMode((byte) 1);

        for (Functionality otherFunctionality : decomposition.getFunctionalities().values()) {
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