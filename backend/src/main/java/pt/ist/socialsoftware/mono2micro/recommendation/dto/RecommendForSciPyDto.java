package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;

public class RecommendForSciPyDto extends RecommendationDto {
    private String profile;
    private String linkageType;
    private Constants.TraceType traceType;
    private int tracesMaxLimit;
    private List<Weights> weightsList;

    public RecommendForSciPyDto() {}

    public RecommendForSciPyDto(RecommendForSciPy recommendation) {
        this.setStrategyName(recommendation.getStrategy().getName());
        this.name = recommendation.getName();
        this.type = recommendation.getType();
        this.profile = recommendation.getProfile();
        this.linkageType = recommendation.getLinkageType();
        this.tracesMaxLimit = recommendation.getTracesMaxLimit();
        this.isCompleted = recommendation.isCompleted();
        this.traceType = recommendation.getTraceType();
        this.weightsList = recommendation.getWeightsList();
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public Constants.TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(Constants.TraceType traceType) {
        this.traceType = traceType;
    }

    public int getTracesMaxLimit() {
        return tracesMaxLimit;
    }

    public void setTracesMaxLimit(int tracesMaxLimit) {
        this.tracesMaxLimit = tracesMaxLimit;
    }

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }
}