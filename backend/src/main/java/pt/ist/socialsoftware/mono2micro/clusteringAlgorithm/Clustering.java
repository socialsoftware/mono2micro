package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;

public abstract class Clustering {
    public abstract void generateClusters(Decomposition decomposition, DecompositionRequest request) throws Exception;
    public abstract String getType();
}
