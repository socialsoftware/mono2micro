package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.List;

public abstract class Clustering {
    public abstract Decomposition generateDecomposition(Similarity similarity, DecompositionRequest request) throws Exception;
    public abstract String getType();

    public abstract List<String> getAlgorithmSupportedStrategyTypes();
}
