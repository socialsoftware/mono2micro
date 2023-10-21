package pt.ist.socialsoftware.mono2micro.utils;

import java.util.Map;

import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;

public class FunctionalityInfo {
    TraceDto longestPath = null;
    TraceDto mostProbablePath = null;
    TraceDto mostDifferentAccessesPath = null;
    Map<String, Float> e1e2PairCount = null;
    Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities = null;

    public void setLongestPath(TraceDto longestPath) {
        this.longestPath = longestPath;
    }

    public TraceDto getLongestPath() {
        return longestPath;
    }

    public void setMostProbablePath(TraceDto mostProbablePath) {
        this.mostProbablePath = mostProbablePath;
    }

    public TraceDto getMostProbablePath() {
        return mostProbablePath;
    }

    public void setMostDifferentAccessesPath(TraceDto mostDifferentAccessesPath) {
        this.mostDifferentAccessesPath = mostDifferentAccessesPath;
    }

    public TraceDto getMostDifferentAccessesPath() {
        return mostDifferentAccessesPath;
    }

    public void setE1e2PairCount(Map<String, Float> e1e2PairCount) {
        this.e1e2PairCount = e1e2PairCount;
    }

    public Map<String, Float> getE1e2PairCount() {
        return e1e2PairCount;
    }

    public void setEntityFunctionalities(Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities) {
        this.entityFunctionalities = entityFunctionalities;
    }

    public Map<Short, Map<Pair<String, Byte>, Float>> getEntityFunctionalities() {
        return entityFunctionalities;
    }

}
