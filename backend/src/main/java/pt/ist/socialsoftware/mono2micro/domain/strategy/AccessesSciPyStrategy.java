package pt.ist.socialsoftware.mono2micro.domain.strategy;

import pt.ist.socialsoftware.mono2micro.utils.Constants;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.ACCESSES_SCIPY;

public class AccessesSciPyStrategy extends Strategy {
    // Used in Similarity Generator
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;

    // Used in Clustering Algorithm
    private String linkageType;

    public AccessesSciPyStrategy() {}

    public AccessesSciPyStrategy(RecommendAccessesSciPyStrategy recommendStrategy, Constants.TraceType traceType, String linkageType, String decompositionName) {
        String[] weights = decompositionName.split(",");
        this.accessMetricWeight = Float.parseFloat(weights[0]);
        this.writeMetricWeight = Float.parseFloat(weights[1]);
        this.readMetricWeight = Float.parseFloat(weights[2]);
        this.sequenceMetricWeight = Float.parseFloat(weights[3]);

        this.setCodebaseName(recommendStrategy.getCodebaseName());
        this.profile = recommendStrategy.getProfile();
        this.tracesMaxLimit = recommendStrategy.getTracesMaxLimit();
        this.traceType = traceType;
        this.linkageType = linkageType;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccessesSciPyStrategy))
            return false;

        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) obj;
        return strategy.getCodebaseName().equals(this.getCodebaseName()) &&
                strategy.getProfile().equals(this.profile) &&
                (strategy.getTracesMaxLimit() == this.tracesMaxLimit) &&
                (strategy.getTraceType() == this.traceType) &&
                (strategy.getAccessMetricWeight() == this.accessMetricWeight) &&
                (strategy.getWriteMetricWeight() == this.writeMetricWeight) &&
                (strategy.getReadMetricWeight() == this.readMetricWeight) &&
                (strategy.getSequenceMetricWeight() == this.sequenceMetricWeight) &&
                (strategy.getLinkageType().equals(this.linkageType));
    }
}