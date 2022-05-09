package pt.ist.socialsoftware.mono2micro.domain.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.RECOMMENDATION_ACCESSES_SCIPY;

public class RecommendAccessesSciPyStrategy extends Strategy {
    // Used in Similarity Generator
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private boolean isCompleted; // true when all the decompositions are calculated

    // Used in Clustering Algorithm
    private String linkageType;

    @JsonIgnore
    private int numberOfEntities;

    @Override
    public String getType() {
        return RECOMMENDATION_ACCESSES_SCIPY;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getTracesMaxLimit() {
        return tracesMaxLimit;
    }

    public void setTracesMaxLimit(int tracesMaxLimit) {
        this.tracesMaxLimit = tracesMaxLimit;
    }

    public Constants.TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(Constants.TraceType traceType) {
        this.traceType = traceType;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public int getNumberOfEntities() {
        return numberOfEntities;
    }

    public void setNumberOfEntities(int numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecommendAccessesSciPyStrategy))
            return false;

        RecommendAccessesSciPyStrategy strategy = (RecommendAccessesSciPyStrategy) obj;

        return strategy.getProfile().equals(this.profile) &&
                (strategy.getTracesMaxLimit() == this.tracesMaxLimit) &&
                (strategy.getTraceType() == this.traceType) &&
                (strategy.getLinkageType().equals(this.linkageType));
    }
}
