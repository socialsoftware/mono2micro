package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy.RECOMMEND_ACCESSES_SCIPY;

public class RecommendAccessesSciPyDto {
    private String profile;
    private Set<String> linkageTypes;
    private int tracesMaxLimit;
    private boolean isCompleted;
    private Set<Constants.TraceType> traceTypes;

    public RecommendAccessesSciPyDto() {this.setType(RECOMMEND_ACCESSES_SCIPY);}

    public RecommendAccessesSciPyDto(RecommendAccessesSciPy strategy) {
        this.setStrategyName(strategy.getCodebase().getName());
        this.setName(strategy.getName());
        this.setType(RECOMMEND_ACCESSES_SCIPY);
        this.profile = strategy.getProfile();
        this.linkageTypes = strategy.getLinkageTypes();
        this.tracesMaxLimit = strategy.getTracesMaxLimit();
        this.isCompleted = strategy.isCompleted();
        this.traceTypes = strategy.getTraceTypes();
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Set<String> getLinkageTypes() {
        return linkageTypes;
    }

    public void setLinkageTypes(Set<String> linkageTypes) {
        this.linkageTypes = linkageTypes;
    }

    public int getTracesMaxLimit() {
        return tracesMaxLimit;
    }

    public void setTracesMaxLimit(int tracesMaxLimit) {
        this.tracesMaxLimit = tracesMaxLimit;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Set<Constants.TraceType> getTraceTypes() {
        return traceTypes;
    }

    public void setTraceTypes(Set<Constants.TraceType> traceTypes) {
        this.traceTypes = traceTypes;
    }
}