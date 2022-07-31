package pt.ist.socialsoftware.mono2micro.dendrogram.dto;

import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public class AccessesSciPyDendrogramDto extends DendrogramDto {
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

    public AccessesSciPyDendrogramDto() {this.setType(AccessesSciPyStrategy.ACCESSES_SCIPY);}

    public AccessesSciPyDendrogramDto(AccessesSciPyDendrogram dendrogram) {
        this.setStrategyName(dendrogram.getStrategy().getName());
        this.setName(dendrogram.getName());
        this.setType(AccessesSciPyStrategy.ACCESSES_SCIPY);
        this.accessMetricWeight = dendrogram.getAccessMetricWeight();
        this.writeMetricWeight = dendrogram.getWriteMetricWeight();
        this.readMetricWeight = dendrogram.getReadMetricWeight();
        this.sequenceMetricWeight = dendrogram.getSequenceMetricWeight();
        this.profile = dendrogram.getProfile();
        this.linkageType = dendrogram.getLinkageType();
        this.tracesMaxLimit = dendrogram.getTracesMaxLimit();
        this.traceType = dendrogram.getTraceType();
    }

    public AccessesSciPyDendrogramDto(RecommendAccessesSciPy recommend, Constants.TraceType traceType, String linkageType, String decompositionName) {
        this.setType(AccessesSciPyStrategy.ACCESSES_SCIPY);
        this.setStrategyName(recommend.getStrategy().getName());
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
