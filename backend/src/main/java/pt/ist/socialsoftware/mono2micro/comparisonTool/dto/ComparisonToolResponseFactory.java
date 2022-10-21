package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class ComparisonToolResponseFactory {
    public static ComparisonToolResponse getComparisonToolResponse(Decomposition decomposition1, Decomposition decomposition2) throws Exception {
        switch (decomposition1.getType() + decomposition2.getType()) { // Compare two decompositions based on their type
            default:
                return new DefaultComparisonToolResponse(decomposition1, decomposition2);
        }
    }
}
