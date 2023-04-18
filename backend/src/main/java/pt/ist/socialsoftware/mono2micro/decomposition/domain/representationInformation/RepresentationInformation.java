package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.DecompositionMetricCalculator;

import java.util.List;
import java.util.Set;

public abstract class RepresentationInformation {
    String decompositionName;
    public abstract String getType();

    public abstract void deleteProperties();

    public abstract void setup(Decomposition decomposition) throws Exception;

    public abstract void update(Decomposition decomposition) throws Exception;
    public abstract void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) throws Exception;

    public abstract List<DecompositionMetricCalculator> getDecompositionMetrics();

    public abstract List<String> getParameters();

    public void renameClusterInFunctionalities(String clusterName, String newName) {}

    public void removeFunctionalitiesWithEntityIDs(Decomposition decomposition, Set<Short> elements) {}

    public abstract String getEdgeWeights(Decomposition decomposition) throws Exception;
    public abstract String getSearchItems(Decomposition decomposition) throws Exception;

    public String getDecompositionName() {
        return decompositionName;
    }

    public void setDecompositionName(String decompositionName) {
        this.decompositionName = decompositionName;
    }
}
