package pt.ist.socialsoftware.mono2micro.metrics;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.util.Map;
import java.util.Set;

public class FunctionalityComplexityMetric extends Metric<Integer> {
    public String getType() {
        return MetricType.FUNCTIONALITY_COMPLEXITY;
    }

    // Decomposition Metric
    public void calculateMetric(Decomposition decomposition) {}

    // Functionality Metric
    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    // Functionality Redesign Metric
    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {
        calculateFunctionalityComplexity((AccessesSciPyDecomposition) decomposition, functionality, functionalityRedesign);
    }

    private void calculateFunctionalityComplexity(
            AccessesSciPyDecomposition decomposition,
            Functionality functionality,
            FunctionalityRedesign functionalityRedesign
    ){
        if(functionality.getType() != FunctionalityType.SAGA)
            return;

        Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
                decomposition.getEntityIDToClusterID(),
                decomposition.getClusters(),
                decomposition.getFunctionalities().values());

        this.value = 0;

        for (int i = 0; i < functionalityRedesign.getRedesign().size(); i++) {
            LocalTransaction lt = functionalityRedesign.getRedesign().get(i);

            if(lt.getId() != 0){
                for(AccessDto accessDto : lt.getClusterAccesses()) {
                    short entity = accessDto.getEntityID();
                    byte mode = accessDto.getMode();

                    // Functionality complexity cost of write
                    if(mode >= 2 && lt.getType() == LocalTransactionTypes.COMPENSATABLE) // 2 -> W, 3 -> RW
                        this.value++;

                    // Functionality complexity cost of read
                    if (mode != 2) { // 2 -> W - we want all the reads
                        for (Functionality otherFunctionality : decomposition.getFunctionalities().values()) {
                            if (!otherFunctionality.getName().equals(functionality.getName()) &&
                                    otherFunctionality.containsEntity(entity) &&
                                    functionalitiesClusters.get(otherFunctionality.getName()).size() > 1) {

                                if(otherFunctionality.getFunctionalityRedesigns().size() == 1 &&
                                        otherFunctionality.getEntities().get(entity) >= 2)
                                    this.value++;
                                else if(otherFunctionality.getFunctionalityRedesigns().size() > 1 &&
                                        otherFunctionality.frUsedForMetrics().semanticLockEntities().contains(entity))
                                    this.value++;
                            }
                        }
                    }
                }
            }
        }
    }
}
