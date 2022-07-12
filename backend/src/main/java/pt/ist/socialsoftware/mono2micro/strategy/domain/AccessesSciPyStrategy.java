package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.ArrayList;

@Document("strategy")
public class AccessesSciPyStrategy extends Strategy {
    public static final String ACCESSES_SCIPY = "Accesses SciPy";
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;

    // Used in Clustering Algorithm
    private String linkageType;
    private String similarityMatrixName;

    // Image created in the Python services
    private String imageName;

    private String copheneticDistanceName;

    public AccessesSciPyStrategy() {}

    public AccessesSciPyStrategy(AccessesSciPyStrategyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.accessMetricWeight = dto.getAccessMetricWeight();
        this.writeMetricWeight = dto.getWriteMetricWeight();
        this.readMetricWeight = dto.getReadMetricWeight();
        this.sequenceMetricWeight = dto.getSequenceMetricWeight();
        this.linkageType = dto.getLinkageType();
        setDecompositions(new ArrayList<>());
    }

    @Override
    public String getType() {
        return ACCESSES_SCIPY;
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

    public float getAccessMetricWeight() {
        return accessMetricWeight;
    }

    public void setAccessMetricWeight(float accessMetricWeight) {
        this.accessMetricWeight = accessMetricWeight;
    }

    public float getWriteMetricWeight() {
        return writeMetricWeight;
    }

    public void setWriteMetricWeight(float writeMetricWeight) {
        this.writeMetricWeight = writeMetricWeight;
    }

    public float getReadMetricWeight() {
        return readMetricWeight;
    }

    public void setReadMetricWeight(float readMetricWeight) {
        this.readMetricWeight = readMetricWeight;
    }

    public float getSequenceMetricWeight() {
        return sequenceMetricWeight;
    }

    public void setSequenceMetricWeight(float sequenceMetricWeight) {
        this.sequenceMetricWeight = sequenceMetricWeight;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public String getSimilarityMatrixName() {
        return similarityMatrixName;
    }

    public void setSimilarityMatrixName(String similarityMatrixName) {
        this.similarityMatrixName = similarityMatrixName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCopheneticDistanceName() {
        return copheneticDistanceName;
    }

    public void setCopheneticDistanceName(String copheneticDistanceName) {
        this.copheneticDistanceName = copheneticDistanceName;
    }

    @Override
    public boolean equalsDto(StrategyDto dto) {
        if (!(dto instanceof AccessesSciPyStrategyDto))
            return false;

        AccessesSciPyStrategyDto strategyDto = (AccessesSciPyStrategyDto) dto;
        return strategyDto.getCodebaseName().equals(this.getCodebase().getName()) &&
                strategyDto.getProfile().equals(this.profile) &&
                (strategyDto.getTracesMaxLimit() == this.tracesMaxLimit) &&
                (strategyDto.getTraceType() == this.traceType) &&
                (strategyDto.getAccessMetricWeight() == this.accessMetricWeight) &&
                (strategyDto.getWriteMetricWeight() == this.writeMetricWeight) &&
                (strategyDto.getReadMetricWeight() == this.readMetricWeight) &&
                (strategyDto.getSequenceMetricWeight() == this.sequenceMetricWeight) &&
                (strategyDto.getLinkageType().equals(this.linkageType));
    }
}