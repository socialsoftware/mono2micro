package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

public class RecommendAccessesSciPyDto extends RecommendationDto {
    private String profile;
    private Set<String> linkageTypes;
    private int tracesMaxLimit;
    private Set<Constants.TraceType> traceTypes;

    public RecommendAccessesSciPyDto() {this.setType(ACCESSES_SCIPY);}

    public RecommendAccessesSciPyDto(RecommendAccessesSciPy recommendation) {
        this.setStrategyName(recommendation.getStrategy().getName());
        this.name = recommendation.getName();
        this.setType(ACCESSES_SCIPY);
        this.profile = recommendation.getProfile();
        this.linkageTypes = recommendation.getLinkageTypes();
        this.tracesMaxLimit = recommendation.getTracesMaxLimit();
        this.isCompleted = recommendation.isCompleted();
        this.traceTypes = recommendation.getTraceTypes();
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

    public Set<Constants.TraceType> getTraceTypes() {
        return traceTypes;
    }

    public void setTraceTypes(Set<Constants.TraceType> traceTypes) {
        this.traceTypes = traceTypes;
    }
}