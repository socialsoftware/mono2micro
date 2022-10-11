package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

public class DecompositionFactory {
    public static Decomposition getDecomposition(String type) {
        switch (type) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition();
            case REPOSITORY_SCIPY:
                return new RepositorySciPyDecomposition();
            case ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyDecomposition();
            default:
                throw new RuntimeException("The type \"" + type + "\" is not a valid type for the decomposition.");
        }
    }

    // Use this if some properties need to be set before the clustering algorithm
    public static Decomposition getDecomposition(DecompositionRequest request) {
        switch (request.getDecompositionType()) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition(request);
            case REPOSITORY_SCIPY:
                return new RepositorySciPyDecomposition(request);
            case ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyDecomposition(request);
            default:
                throw new RuntimeException("The type \"" + request.getDecompositionType() + "\" is not a valid type for the decomposition.");
        }
    }
}
