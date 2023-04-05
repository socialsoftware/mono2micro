package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FunctionalityVectorizationSequenceOfAccessesWeights extends Weights {
    public static final String FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS = "FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS";
    private float readMetricWeight;
    private float writeMetricWeight;

    public FunctionalityVectorizationSequenceOfAccessesWeights() {}

    public FunctionalityVectorizationSequenceOfAccessesWeights(float readMetricWeight, float writeMetricWeight) {
        this.readMetricWeight = readMetricWeight;
        this.writeMetricWeight = writeMetricWeight;
    }

    @Override
    public String getType() {
        return FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 2;
    }

    @Override
    public float[] getWeights() {
        return new float[]{readMetricWeight, writeMetricWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<>(Arrays.asList("readMetricWeight", "writeMetricWeight"));
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder("ws(");
        result.append("Re")
                .append(Math.round(getWeights()[0]))
                .append(",")
                .append("Wr")
                .append(Math.round(getWeights()[1]))
                .append(")");
        return result.toString();
    }

    @Override
    public void setWeightsFromArray(float[] weightsArray) {
        this.readMetricWeight = weightsArray[0];
        this.writeMetricWeight = weightsArray[1];
    }

    public float getReadMetricWeight() {
        return readMetricWeight;
    }

    public void setReadMetricWeight(float readMetricWeight) {
        this.readMetricWeight = readMetricWeight;
    }

    public float getWriteMetricWeight() {
        return writeMetricWeight;
    }

    public void setWriteMetricWeight(float writeMetricWeight) {
        this.writeMetricWeight = writeMetricWeight;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FunctionalityVectorizationSequenceOfAccessesWeights))
            return false;
        FunctionalityVectorizationSequenceOfAccessesWeights callGraphWeights = (FunctionalityVectorizationSequenceOfAccessesWeights) object;
        return this.readMetricWeight == callGraphWeights.getReadMetricWeight() &&
                this.writeMetricWeight == callGraphWeights.getWriteMetricWeight();
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        throw new NotImplementedException("Not used");
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        throw new NotImplementedException("Not used");
    }

}
