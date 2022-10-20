package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoDecomposition.ACC_AND_REPO_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;

public class DecompositionFactory {
    public static Decomposition getDecomposition(String type) {
        switch (type) {
            case ACCESSES_DECOMPOSITION:
                return new AccessesDecomposition();
            case REPOSITORY_DECOMPOSITION:
                return new RepositoryDecomposition();
            case ACC_AND_REPO_DECOMPOSITION:
                return new AccAndRepoDecomposition();
            default:
                throw new RuntimeException("The type \"" + type + "\" is not a valid type for the decomposition.");
        }
    }

    // Use this if some properties need to be set before the clustering algorithm
    public static Decomposition getDecomposition(DecompositionRequest request) {
        switch (request.getDecompositionType()) {
            case ACCESSES_DECOMPOSITION:
                return new AccessesDecomposition(request);
            case REPOSITORY_DECOMPOSITION:
                return new RepositoryDecomposition(request);
            case ACC_AND_REPO_DECOMPOSITION:
                return new AccAndRepoDecomposition(request);
            default:
                throw new RuntimeException("The type \"" + request.getDecompositionType() + "\" is not a valid type for the decomposition.");
        }
    }
}
