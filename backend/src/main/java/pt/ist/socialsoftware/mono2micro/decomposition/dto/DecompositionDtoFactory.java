package pt.ist.socialsoftware.mono2micro.decomposition.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

import java.util.ArrayList;
import java.util.List;

public class DecompositionDtoFactory {
    private static DecompositionDtoFactory factory = null;

    public static DecompositionDtoFactory getFactory() {
        if (factory == null)
            factory = new DecompositionDtoFactory();
        return factory;
    }

    public DecompositionDto getDecompositionDto(Decomposition decomposition) {
        if (decomposition == null)
            return null;
        switch (decomposition.getStrategyType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                return new AccessesSciPyDecompositionDto((AccessesSciPyDecomposition) decomposition);
            default:
                throw new RuntimeException("The type \"" + decomposition.getStrategyType() + "\" is not a valid strategy type for the decomposition.");
        }
    }

    public List<DecompositionDto> getDecompositionDtos(List<Decomposition> decompositions) {
        List<DecompositionDto> decompositionDtos = new ArrayList<>();
        for (Decomposition decomposition : decompositions)
            decompositionDtos.add(getDecompositionDto(decomposition));
        return decompositionDtos;
    }
}