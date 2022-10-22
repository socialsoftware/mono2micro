package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.*;

public class DecompositionFactory {
    public static Decomposition getDecomposition(String type) {
        switch (type) {
            case ACCESSES_DECOMPOSITION:
            case REPOSITORY_DECOMPOSITION:
            case ACC_AND_REPO_DECOMPOSITION:
                return new ClustersDecomposition(type);
            default:
                throw new RuntimeException("The type \"" + type + "\" is not a valid type for the decomposition.");
        }
    }

    // Use this if some properties need to be set before the clustering algorithm
    public static Decomposition getDecomposition(DecompositionRequest request) {
        switch (request.getDecompositionType()) {
            case ACCESSES_DECOMPOSITION:
            case REPOSITORY_DECOMPOSITION:
            case ACC_AND_REPO_DECOMPOSITION:
                return new ClustersDecomposition(request);
            default:
                throw new RuntimeException("The type \"" + request.getDecompositionType() + "\" is not a valid type for the decomposition.");
        }
    }
}
