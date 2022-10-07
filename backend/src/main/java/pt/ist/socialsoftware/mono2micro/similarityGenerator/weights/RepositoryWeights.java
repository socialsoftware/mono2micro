package pt.ist.socialsoftware.mono2micro.similarityGenerator.weights;

import java.util.ArrayList;
import java.util.List;

public class RepositoryWeights extends Weights {
    public static final String REPOSITORY_WEIGHTS = "REPOSITORY_WEIGHTS";
    private float authorMetricWeight;
    private float commitMetricWeight;

    public RepositoryWeights() {}

    public RepositoryWeights(float authorMetricWeight, float commitMetricWeight) {
        this.authorMetricWeight = authorMetricWeight;
        this.commitMetricWeight = commitMetricWeight;
    }

    @Override
    public String getType() {
        return REPOSITORY_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 2;
    }

    @Override
    public float[] getWeights() {
        return new float[]{authorMetricWeight, commitMetricWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<String>() {{
            add("authorMetricWeight");
            add("commitMetricWeight");
        }};
    }

    public void setWeightsFromArray(float[] weightsArray) {
        this.authorMetricWeight = weightsArray[0];
        this.commitMetricWeight = weightsArray[1];
    }

    public float getAuthorMetricWeight() {
        return authorMetricWeight;
    }

    public void setAuthorMetricWeight(float authorMetricWeight) {
        this.authorMetricWeight = authorMetricWeight;
    }

    public float getCommitMetricWeight() {
        return commitMetricWeight;
    }

    public void setCommitMetricWeight(float commitMetricWeight) {
        this.commitMetricWeight = commitMetricWeight;
    }



    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RepositoryWeights))
            return false;
        RepositoryWeights repositoryWeights = (RepositoryWeights) object;
        return this.authorMetricWeight == repositoryWeights.getAuthorMetricWeight() &&
                this.commitMetricWeight == repositoryWeights.getCommitMetricWeight();
    }
}
