package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.*;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.PartitionsDecomposition.PARTITIONS_DECOMPOSITION;

public class DecompositionDtoFactory {
    public static DecompositionDto getDecompositionDto(Decomposition decomposition) {
        if (decomposition == null)
            return null;
        switch (decomposition.getType()) {
            case PARTITIONS_DECOMPOSITION:
                return new PartitionsDecompositionDto((PartitionsDecomposition) decomposition);
            default:
                throw new RuntimeException("The type \"" + decomposition.getType() + "\" is not a valid strategy type for the decomposition.");
        }
    }

    public static List<DecompositionDto> getDecompositionDtos(List<Decomposition> decompositions) {
        List<DecompositionDto> decompositionDtos = new ArrayList<>();
        for (Decomposition decomposition : decompositions)
            decompositionDtos.add(getDecompositionDto(decomposition));
        return decompositionDtos;
    }
}