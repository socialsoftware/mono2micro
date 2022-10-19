package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.Set;

public abstract class RepresentationInformation {
    String decompositionName;
    public abstract String getType();

    public abstract void deleteProperties();

    public abstract void update(Decomposition decomposition) throws Exception;

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
