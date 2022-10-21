package pt.ist.socialsoftware.mono2micro.comparisonTool.dto;

import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.results.Results;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;

import java.util.ArrayList;
import java.util.List;

public abstract class ComparisonToolResponse {
    protected DecompositionDto decomposition1;
    protected DecompositionDto decomposition2;

    protected List<Results> resultsList = new ArrayList<>();

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

    public List<Results> getResultsList() {
        return resultsList;
    }

    public void setResultsList(List<Results> resultsList) {
        this.resultsList = resultsList;
    }

    public void addResults(Results results) {
        this.resultsList.add(results);
    }
}