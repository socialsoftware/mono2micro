package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.*;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.*;

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
            case ACCESSES_DECOMPOSITION:
                return new AccessesDecompositionDto((PartitionsDecomposition) decomposition);
            case REPOSITORY_DECOMPOSITION:
                return new RepositoryDecompositionDto((PartitionsDecomposition) decomposition);
            case ACC_AND_REPO_DECOMPOSITION:
                return new AccAndRepoDecompositionDto((PartitionsDecomposition) decomposition);
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