package pt.ist.socialsoftware.mono2micro.utils;

import java.util.List;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;

public class PathData {
    private List<Access> mostProbablePath;
    private Float mostProbablePathProbability;

    private List<Access> mostDifferentAccessesPath;
    private List<Access> mostDifferentAccesses;

    private List<Access> longestPath;

    public PathData(List<Access> mostProbablePath, Float mostProbablePathProbability, List<Access> mostDifferentAccessesPath, List<Access> mostDifferentAccesses, List<Access> longestPath) {
        this.mostProbablePath = mostProbablePath;
        this.mostProbablePathProbability = mostProbablePathProbability;

        this.mostDifferentAccessesPath = mostDifferentAccessesPath;
        this.mostDifferentAccesses = mostDifferentAccesses;

        this.longestPath = longestPath;

    }

    public List<Access> getMostProbablePath() {
        return mostProbablePath;
    }

    public void setMostProbablePath(List<Access> mostProbablePath) {
        this.mostProbablePath = mostProbablePath;
    }

    public Float getMostProbablePathProbability() {
        return mostProbablePathProbability;
    }

    public void setMostProbablePathProbability(Float mostProbablePathProbability) {
        this.mostProbablePathProbability = mostProbablePathProbability;
    }

    public List<Access> getMostDifferentAccessesPath() {
        return mostDifferentAccessesPath;
    }

    public void setMostDifferentAccessesPath(List<Access> mostDifferentAccessesPath) {
        this.mostDifferentAccessesPath = mostDifferentAccessesPath;
    }

    public List<Access> getMostDifferentAccesses() {
        return mostDifferentAccesses;
    }

    public void setMostDifferentAccesses(List<Access> mostDifferentAccesses) {
        this.mostDifferentAccesses = mostDifferentAccesses;
    }

    public List<Access> getLongestPath() {
        return longestPath;
    }

    public void setLongestPath(List<Access> longestPath) {
        this.longestPath = longestPath;
    }
}
