package pt.ist.socialsoftware.mono2micro.similarityGenerator.weights;

import java.util.ArrayList;
import java.util.List;

public class AccessesWeights extends Weights {
    public static final String ACCESSES_WEIGHTS = "ACCESSES_WEIGHTS";
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;

    public AccessesWeights() {}

    public AccessesWeights(float accessMetricWeight, float writeMetricWeight, float readMetricWeight, float sequenceMetricWeight) {
        this.accessMetricWeight = accessMetricWeight;
        this.writeMetricWeight = writeMetricWeight;
        this.readMetricWeight = readMetricWeight;
        this.sequenceMetricWeight = sequenceMetricWeight;
    }

    @Override
    public String getType() {
        return ACCESSES_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 4;
    }

    @Override
    public float[] getWeights() {
        return new float[]{accessMetricWeight, writeMetricWeight, readMetricWeight, sequenceMetricWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<String>() {{
            add("accessMetricWeight");
            add("writeMetricWeight");
            add("readMetricWeight");
            add("sequenceMetricWeight");
        }};
    }

    public void setWeightsFromArray(float[] weightsArray) {
        this.accessMetricWeight = weightsArray[0];
        this.writeMetricWeight = weightsArray[1];
        this.readMetricWeight = weightsArray[2];
        this.sequenceMetricWeight = weightsArray[3];
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccessesWeights))
            return false;
        AccessesWeights accessesWeights = (AccessesWeights) object;
        return this.accessMetricWeight == accessesWeights.getAccessMetricWeight() &&
                this.writeMetricWeight == accessesWeights.getWriteMetricWeight() &&
                this.readMetricWeight == accessesWeights.getReadMetricWeight() &&
                this.sequenceMetricWeight == accessesWeights.getSequenceMetricWeight();
    }
}
