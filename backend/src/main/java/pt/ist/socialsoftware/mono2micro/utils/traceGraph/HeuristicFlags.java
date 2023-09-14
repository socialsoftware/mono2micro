package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

public class HeuristicFlags {
    public boolean hasBreak = false;
    public boolean hasCall = false;
    public boolean goingToLoopHead = false;
    public boolean hasLoop = false;
    public boolean hasStore = false;
    public boolean hasReturn = false;
    public boolean zeroComparison = false;
    public boolean postDominant = false;
    public boolean objectComparison = false;
    public boolean laterChangedCVariable = false;
}
