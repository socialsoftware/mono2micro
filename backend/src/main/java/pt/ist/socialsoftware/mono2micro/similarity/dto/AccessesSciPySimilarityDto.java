package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public class AccessesSciPySimilarityDto extends SimilarityDto {
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;
    private String profile;
    private String linkageType;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    @Override
    public String getType() {
        return AccessesSciPyStrategy.ACCESSES_SCIPY;
    }

    public AccessesSciPySimilarityDto() {this.type = AccessesSciPyStrategy.ACCESSES_SCIPY;}

    public AccessesSciPySimilarityDto(AccessesSciPySimilarity similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = AccessesSciPyStrategy.ACCESSES_SCIPY;
        this.accessMetricWeight = similarity.getAccessMetricWeight();
        this.writeMetricWeight = similarity.getWriteMetricWeight();
        this.readMetricWeight = similarity.getReadMetricWeight();
        this.sequenceMetricWeight = similarity.getSequenceMetricWeight();
        this.profile = similarity.getProfile();
        this.linkageType = similarity.getLinkageType();
        this.tracesMaxLimit = similarity.getTracesMaxLimit();
        this.traceType = similarity.getTraceType();
    }

    public AccessesSciPySimilarityDto(RecommendAccessesSciPy recommend, Constants.TraceType traceType, String linkageType, String decompositionName) {
        this.type = AccessesSciPyStrategy.ACCESSES_SCIPY;
        this.strategyName = recommend.getStrategy().getName();
        String[] weights = decompositionName.split(",");
        this.accessMetricWeight = Float.parseFloat(weights[1]);
        this.writeMetricWeight = Float.parseFloat(weights[2]);
        this.readMetricWeight = Float.parseFloat(weights[3]);
        this.sequenceMetricWeight = Float.parseFloat(weights[4]);
        this.profile = recommend.getProfile();
        this.linkageType = linkageType;
        this.tracesMaxLimit = recommend.getTracesMaxLimit();
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
