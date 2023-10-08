package pt.ist.socialsoftware.mono2micro.utils;

import java.util.List;
import java.util.stream.Collectors;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;

public class PathData {
    private List<PathDataAccess> mostProbablePath;
    private Float mostProbablePathProbability;

    private List<PathDataAccess> mostDifferentAccessesPath;
    private List<PathDataAccess> mostDifferentAccesses;

    private List<PathDataAccess> longestPath;
    private int longestPathSize;

    public PathData(List<Access> mostProbablePath, Float mostProbablePathProbability, List<Access> mostDifferentAccessesPath, List<Access> mostDifferentAccesses, List<Access> longestPath, int longestPathSize){
        this.mostProbablePath = mostProbablePath.stream().map(a -> new PathDataAccess(a)).collect(Collectors.toList());
        this.mostProbablePathProbability = mostProbablePathProbability;

        this.mostDifferentAccessesPath = mostDifferentAccessesPath.stream().map(a -> new PathDataAccess(a)).collect(Collectors.toList());
        this.mostDifferentAccesses = mostDifferentAccesses.stream().map(a -> new PathDataAccess(a)).collect(Collectors.toList());

        this.longestPath = longestPath.stream().map(a -> new PathDataAccess(a)).collect(Collectors.toList());
        this.longestPathSize = longestPathSize;
    }

    public PathData(List<PathDataAccess> longestPath, int longestPathSize, List<PathDataAccess> mostProbablePath, Float mostProbablePathProbability, List<PathDataAccess> mostDifferentAccessesPath, List<PathDataAccess> mostDifferentAccesses) {
        this.mostProbablePath = mostProbablePath;
        this.mostProbablePathProbability = mostProbablePathProbability;

        this.mostDifferentAccessesPath = mostDifferentAccessesPath;
        this.mostDifferentAccesses = mostDifferentAccesses;

        this.longestPath = longestPath;
        this.longestPathSize = longestPathSize;
    }

    public List<PathDataAccess> getMostProbablePath() {
        return mostProbablePath;
    }

    public void setMostProbablePath(List<PathDataAccess> mostProbablePath) {
        this.mostProbablePath = mostProbablePath;
    }

    public Float getMostProbablePathProbability() {
        return mostProbablePathProbability;
    }

    public void setMostProbablePathProbability(Float mostProbablePathProbability) {
        this.mostProbablePathProbability = mostProbablePathProbability;
    }

    public List<PathDataAccess> getMostDifferentAccessesPath() {
        return mostDifferentAccessesPath;
    }

    public void setMostDifferentAccessesPath(List<PathDataAccess> mostDifferentAccessesPath) {
        this.mostDifferentAccessesPath = mostDifferentAccessesPath;
    }

    public List<PathDataAccess> getMostDifferentAccesses() {
        return mostDifferentAccesses;
    }

    public void setMostDifferentAccesses(List<PathDataAccess> mostDifferentAccesses) {
        this.mostDifferentAccesses = mostDifferentAccesses;
    }

    public List<PathDataAccess> getLongestPath() {
        return longestPath;
    }

    public void setLongestPath(List<PathDataAccess> longestPath) {
        this.longestPath = longestPath;
    }

    public int getLongestPathSize() {
        return longestPathSize;
    }

    public void setLongestPathSize(int longestPathSize) {
        this.longestPathSize = longestPathSize;
    }
}
