package pt.ist.socialsoftware.mono2micro.metrics.metricService;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;

import java.util.Set;

@Service
public class InconsistencyComplexityMetricService {
    public Integer calculateMetric(AccessesDecomposition decomposition, Functionality functionality) {
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