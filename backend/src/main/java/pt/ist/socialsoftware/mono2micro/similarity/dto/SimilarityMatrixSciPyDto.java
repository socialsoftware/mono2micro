package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;

public class SimilarityMatrixSciPyDto extends SimilarityDto {
    private List<Weights> weightsList;
    private String linkageType;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String profile;

    public SimilarityMatrixSciPyDto() {}

    public SimilarityMatrixSciPyDto(SimilarityMatrixSciPy similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.decompositionType = similarity.getStrategy().getDecompositionType();
        this.weightsList = similarity.getSimilarityMatrix().getWeightsList();
        this.profile = similarity.getProfile();
        this.linkageType = similarity.getLinkageType();
        this.tracesMaxLimit = similarity.getTracesMaxLimit();
        this.traceType = similarity.getTraceType();
    }

    public SimilarityMatrixSciPyDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.decompositionType = recommend.getStrategy().getDecompositionType();
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
