package pt.ist.socialsoftware.mono2micro.comparisonTool.domain;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public abstract class Analysis {
    public abstract String getType();
    public abstract void analyse(Decomposition decomposition1, Decomposition decomposition2) throws Exception;
}
