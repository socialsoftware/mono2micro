package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

public class AccessesSciPyStrategyDto extends StrategyDto {
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;
    private String profile;
    private String linkageType;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    public AccessesSciPyStrategyDto() {this.setType(ACCESSES_SCIPY);}

    public AccessesSciPyStrategyDto(AccessesSciPyStrategy strategy) {
        this.setName(strategy.getName());
        this.setType(ACCESSES_SCIPY);
        this.accessMetricWeight = strategy.getAccessMetricWeight();
        this.writeMetricWeight = strategy.getWriteMetricWeight();
        this.readMetricWeight = strategy.getReadMetricWeight();
        this.sequenceMetricWeight = strategy.getSequenceMetricWeight();
        this.profile = strategy.getProfile();
        this.linkageType = strategy.getLinkageType();
        this.tracesMaxLimit = strategy.getTracesMaxLimit();
        this.traceType = strategy.getTraceType();
    }

    public AccessesSciPyStrategyDto(RecommendAccessesSciPyStrategy strategy, Constants.TraceType traceType, String linkageType, String decompositionName) {
        this.setType(ACCESSES_SCIPY);
        this.setCodebaseName(strategy.getCodebase().getName());
        String[] weights = decompositionName.split(",");
        this.accessMetricWeight = Float.parseFloat(weights[1]);
        this.writeMetricWeight = Float.parseFloat(weights[2]);
        this.readMetricWeight = Float.parseFloat(weights[3]);
        this.sequenceMetricWeight = Float.parseFloat(weights[4]);
        this.profile = strategy.getProfile();
        this.linkageType = linkageType;
        this.tracesMaxLimit = strategy.getTracesMaxLimit();
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
}
