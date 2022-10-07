package pt.ist.socialsoftware.mono2micro.similarity.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.SciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.generator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityForSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.ArrayList;
import java.util.List;

@Document("similarity")
public class SimilarityForSciPy extends Similarity implements AccessesSimilarity, Dendrogram, SciPy, SimilarityMatrix {
    // Used during Similarity Generation
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    private List<Weights> weightsList;

    // Used in Clustering Algorithm
    private String linkageType;
    private String similarityMatrixName;

    // Image created in the Python services
    private String dendrogramName;
    private String copheneticDistanceName;

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_SIMILARITY);
        add(DENDROGRAM);
        add(SCIPY);
        add(SIMILARITY_MATRIX);
    }};

    public SimilarityForSciPy() {}

    public SimilarityForSciPy(SimilarityForSciPyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.linkageType = dto.getLinkageType();
        this.weightsList = dto.getWeightsList();
        setDecompositions(new ArrayList<>());
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

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }
    @Override
    public List<String> getImplementations() {
        return implementationTypes;
    }
    @Override
    public String getLinkageType() {
        return linkageType;
    }
    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }
    @Override
    public String getSimilarityMatrixName() {
        return similarityMatrixName;
    }
    @Override
    public void setSimilarityMatrixName(String similarityMatrixName) {
        this.similarityMatrixName = similarityMatrixName;
    }
    public String getDendrogramName() {
        return dendrogramName;
    }
    public void setDendrogramName(String dendrogramName) {
        this.dendrogramName = dendrogramName;
    }
    @Override
    public String getCopheneticDistanceName() {
        return copheneticDistanceName;
    }
    public void setCopheneticDistanceName(String copheneticDistanceName) {
        this.copheneticDistanceName = copheneticDistanceName;
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityForSciPyDto))
            return false;

        SimilarityForSciPyDto similarityDto = (SimilarityForSciPyDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
                similarityDto.getType().equals(this.getType()) &&
                similarityDto.getProfile().equals(this.profile) &&
                (similarityDto.getTracesMaxLimit() == this.tracesMaxLimit) &&
                (similarityDto.getTraceType() == this.traceType) &&
                (similarityDto.getLinkageType().equals(this.linkageType)) &&
                hasSameWeights(similarityDto.getWeightsList());
    }
}
