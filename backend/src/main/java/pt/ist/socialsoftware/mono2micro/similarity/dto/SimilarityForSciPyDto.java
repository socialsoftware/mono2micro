package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;

public class SimilarityForSciPyDto extends SimilarityDto {
    private List<Weights> weightsList;
    private String linkageType;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String profile;

    public SimilarityForSciPyDto() {}

    public SimilarityForSciPyDto(SimilarityForSciPy similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = AccessesSciPyStrategy.ACCESSES_SCIPY;
        this.weightsList = similarity.getWeightsList();
        this.profile = similarity.getProfile();
        this.linkageType = similarity.getLinkageType();
        this.tracesMaxLimit = similarity.getTracesMaxLimit();
        this.traceType = similarity.getTraceType();
    }

    public SimilarityForSciPyDto(RecommendForSciPy recommend, List<Weights> weightsList) {
        this.type = AccessesSciPyStrategy.ACCESSES_SCIPY;
        this.strategyName = recommend.getStrategy().getName();
        this.weightsList = weightsList;
        this.profile = recommend.getProfile();
        this.linkageType = recommend.getLinkageType();
        this.tracesMaxLimit = recommend.getTracesMaxLimit();
        this.traceType = recommend.getTraceType();
    }

    public List<Weights> getWeightsList() {
    return weightsList;
}

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
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
}
