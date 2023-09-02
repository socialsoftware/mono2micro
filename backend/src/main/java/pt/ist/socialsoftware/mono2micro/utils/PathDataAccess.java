package pt.ist.socialsoftware.mono2micro.utils;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;

public class PathDataAccess {
    
    protected Access access;
    protected float probability = 1.0f;

    public PathDataAccess(Access access) {
        this.access = access;
    }

    public PathDataAccess(Access access, float probability) {
        this.access = access;
        this.probability = probability;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

}
