package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyWeights.SIMILARITY_SCIPY_WEIGHTS;

public class SimilarityMatrixSciPyDto extends SimilarityDto {
    private List<Weights> weightsList;
    private String linkageType;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String profile;

    public SimilarityMatrixSciPyDto() { this.type = SIMILARITY_SCIPY_WEIGHTS; }

    public SimilarityMatrixSciPyDto(SimilarityScipyWeights similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.weightsList = ((SimilarityMatrixWeights) similarity.getSimilarityMatrix()).getWeightsList();
        this.profile = similarity.getProfile();
        this.linkageType = similarity.getLinkageType();
        this.tracesMaxLimit = similarity.getTracesMaxLimit();
        this.traceType = similarity.getTraceType();
    }

    public SimilarityMatrixSciPyDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.type = SIMILARITY_SCIPY_WEIGHTS;
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
