package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.io.IOException;
import java.util.*;

public class ZeroWeights extends Weights {
    public static final String ZERO_WEIGHTS = "ZERO_WEIGHTS";

    public ZeroWeights() {}

    @Override
    public String getType() {
        return ZERO_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 0;
    }

    @Override
    public float[] getWeights() {
        return new float[]{};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<>();
    }

    @Override
    public void setWeightsFromArray(float[] weightsArray) {

    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof ZeroWeights);
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException {
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException {
    }

}
