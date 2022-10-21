package pt.ist.socialsoftware.mono2micro.comparisonTool.domain;

import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.results.Results;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public abstract class ComparisonAlgorithms {
    public abstract Results getAnalysis(Decomposition decomposition1, Decomposition decomposition2) throws Exception;
}
