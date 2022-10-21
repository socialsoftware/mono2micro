package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;

public abstract class ComparisonToolResponse {
    protected DecompositionDto decomposition1;
    protected DecompositionDto decomposition2;

    public abstract String getType();

    public DecompositionDto getDecomposition2() {
        return decomposition2;
    }

    public void setDecomposition2(DecompositionDto decomposition2) {
        this.decomposition2 = decomposition2;
    }

    public DecompositionDto getDecomposition1() {
        return decomposition1;
    }

    public void setDecomposition1(DecompositionDto decomposition1) {
        this.decomposition1 = decomposition1;
    }
}