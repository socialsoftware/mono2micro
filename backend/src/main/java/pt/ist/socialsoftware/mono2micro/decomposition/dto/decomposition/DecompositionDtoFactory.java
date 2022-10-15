package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

public class DecompositionDtoFactory {
    private static DecompositionDtoFactory factory = null;

    public static DecompositionDtoFactory getFactory() {
        if (factory == null)
            factory = new DecompositionDtoFactory();
        return factory;
    }

    public static DecompositionDto getDecompositionDto(Decomposition decomposition) {
        if (decomposition == null)
            return null;
        switch (decomposition.getType()) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyDecompositionDto((AccessesSciPyDecomposition) decomposition);
            case REPOSITORY_SCIPY:
                return new RepositorySciPyDecompositionDto((RepositorySciPyDecomposition) decomposition);
            case ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyDecompositionDto((AccAndRepoSciPyDecomposition) decomposition);
            default:
                throw new RuntimeException("The type \"" + decomposition.getType() + "\" is not a valid strategy type for the decomposition.");
        }
    }

    public List<DecompositionDto> getDecompositionDtos(List<Decomposition> decompositions) {
        List<DecompositionDto> decompositionDtos = new ArrayList<>();
        for (Decomposition decomposition : decompositions)
            decompositionDtos.add(getDecompositionDto(decomposition));
        return decompositionDtos;
    }
}