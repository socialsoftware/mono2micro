package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

@Document("strategy")
public class AccessesSciPyStrategy extends Strategy {
    public static final String ACCESSES_SCIPY = "ACCESSES_SCIPY";
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

    public AccessesSciPyStrategy(RecommendAccessesSciPyStrategy recommendStrategy, Constants.TraceType traceType, String linkageType, String decompositionName) {
        String[] weights = decompositionName.split(",");
        this.accessMetricWeight = Float.parseFloat(weights[0]);
        this.writeMetricWeight = Float.parseFloat(weights[1]);
        this.readMetricWeight = Float.parseFloat(weights[2]);
        this.sequenceMetricWeight = Float.parseFloat(weights[3]);

        this.setCodebase(recommendStrategy.getCodebase());
        this.profile = recommendStrategy.getProfile();
        this.tracesMaxLimit = recommendStrategy.getTracesMaxLimit();
        this.traceType = traceType;
        this.linkageType = linkageType;
    }

    public AccessesSciPyStrategy(AccessesSciPyStrategyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.accessMetricWeight = dto.getAccessMetricWeight();
        this.writeMetricWeight = dto.getWriteMetricWeight();
        this.readMetricWeight = dto.getReadMetricWeight();
        this.sequenceMetricWeight = dto.getSequenceMetricWeight();
        this.linkageType = dto.getLinkageType();
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
    public boolean equals(Object obj) {
        if (!(obj instanceof AccessesSciPyStrategy))
            return false;

        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) obj;
        return strategy.getCodebase().getName().equals(this.getCodebase().getName()) &&
                strategy.getProfile().equals(this.profile) &&
                (strategy.getTracesMaxLimit() == this.tracesMaxLimit) &&
                (strategy.getTraceType() == this.traceType) &&
                (strategy.getAccessMetricWeight() == this.accessMetricWeight) &&
                (strategy.getWriteMetricWeight() == this.writeMetricWeight) &&
                (strategy.getReadMetricWeight() == this.readMetricWeight) &&
                (strategy.getSequenceMetricWeight() == this.sequenceMetricWeight) &&
                (strategy.getLinkageType().equals(this.linkageType));
    }
    //TODO check if this is still needed

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