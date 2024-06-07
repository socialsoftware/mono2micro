package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyStructure;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyStructure.SIMILARITY_SCIPY_STRUCTURE;

public class SimilarityScipyStructureDto extends SimilarityDto {
    private List<Weights> weightsList;
    private String linkageType;
    private String profile;
    private Constants.TraceType traceType;

    public SimilarityScipyStructureDto() { this.type = SIMILARITY_SCIPY_STRUCTURE; }

    public SimilarityScipyStructureDto(SimilarityScipyStructure similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.weightsList = similarity.getWeightsList();
        this.profile = similarity.getProfile();
        this.linkageType = similarity.getLinkageType();
        this.traceType = similarity.getTraceType();

    }

    public SimilarityScipyStructureDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.type = SIMILARITY_SCIPY_STRUCTURE;
        this.weightsList = weightsList;
        this.profile = recommend.getProfile();
        this.traceType = recommend.getTraceType();
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.strategyName + " "
                    + "params" + "(" + this.profile + "," + this.linkageType + "," + this.traceType + ") "
                    + this.weightsList.stream()
                    .map(weights -> weights.getName())
                    .collect(Collectors.joining(", "));
        }

        return this.name;
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

}
