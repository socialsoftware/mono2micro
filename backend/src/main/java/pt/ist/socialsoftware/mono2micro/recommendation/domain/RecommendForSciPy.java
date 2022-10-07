package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.interfaces.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendForSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document("recommendation")
public class RecommendForSciPy extends Recommendation implements SimilarityMatrices {
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String linkageType;
    private List<Weights> weightsList;
    private Set<String> similarityMatricesNames;

    @Transient
    private byte[] representationBytes;

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(SIMILARITY_MATRICES);
    }};

    public RecommendForSciPy() {}

    public RecommendForSciPy(RecommendForSciPyDto dto) {
        this.type = dto.getType();
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.isCompleted = dto.isCompleted();
        this.weightsList = dto.getWeightsList();
        this.similarityMatricesNames = new HashSet<>();
    }

    public List<String> getImplementations() {
        return implementationTypes;
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

    public Set<String> getSimilarityMatricesNames() {
        return similarityMatricesNames;
    }

    public void setSimilarityMatricesNames(Set<String> similarityMatricesNames) {
        this.similarityMatricesNames = similarityMatricesNames;
    }

    @Override
    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public byte[] getRepresentationBytes() {
        return representationBytes;
    }

    public void setRepresentationBytes(byte[] representationBytes) {
        this.representationBytes = representationBytes;
    }

    @Override
    public boolean equalsDto(RecommendationDto dto) {
        if (!(dto instanceof RecommendForSciPyDto))
            return false;

        RecommendForSciPyDto recommendForSciPyDto = (RecommendForSciPyDto) dto;

        return recommendForSciPyDto.getProfile().equals(this.profile) &&
                (recommendForSciPyDto.getLinkageType().equals(this.linkageType)) &&
                (recommendForSciPyDto.getTraceType() == this.traceType) &&
                (recommendForSciPyDto.getTracesMaxLimit() == this.tracesMaxLimit);
    }
}
