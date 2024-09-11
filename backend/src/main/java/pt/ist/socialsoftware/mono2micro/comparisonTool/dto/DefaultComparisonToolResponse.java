package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.MoJoFM;
import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.Purity;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;

public class DefaultComparisonToolResponse extends ComparisonToolResponse {
    public DefaultComparisonToolResponse(Decomposition decomposition1, Decomposition decomposition2) throws Exception {
        this.decomposition1 = DecompositionDtoFactory.getDecompositionDto(decomposition1);
        this.decomposition2 = DecompositionDtoFactory.getDecompositionDto(decomposition2);

        this.addAnalysis(new MoJoFM(decomposition1, decomposition2));
        this.addAnalysis(new Purity(decomposition1, decomposition2));
    }
}
