package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.MoJoFM;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;

import java.io.IOException;

public class DefaultComparisonToolResponse extends ComparisonToolResponse {
    public DefaultComparisonToolResponse(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        this.decomposition1 = DecompositionDtoFactory.getDecompositionDto(decomposition1);
        this.decomposition2 = DecompositionDtoFactory.getDecompositionDto(decomposition2);

        this.addResults(new MoJoFM().getAnalysis(decomposition1, decomposition2));
    }
}
