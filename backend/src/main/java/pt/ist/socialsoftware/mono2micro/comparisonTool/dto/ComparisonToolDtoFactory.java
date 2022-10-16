package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class ComparisonToolDtoFactory {
    public static ComparisonToolDto getComparisonToolDto(Decomposition decomposition1, Decomposition decomposition2) {
        switch (decomposition1.getType() + decomposition2.getType()) { // Compare two decompositions based on their type
            default:
                return new DefaultComparisonToolDto();
        }
    }
}
