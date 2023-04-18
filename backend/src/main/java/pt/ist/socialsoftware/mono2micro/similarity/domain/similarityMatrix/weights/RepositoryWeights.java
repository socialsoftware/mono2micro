package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;

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
        return new ArrayList<>(Arrays.asList("authorMetricWeight", "commitMetricWeight"));
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder("ws(");
        result.append("Au")
                .append(Math.round(getWeights()[0]))
                .append(",")
                .append("Co")
                .append(Math.round(getWeights()[1]))
                .append(")");
        return result.toString();
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

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException {
        fillRawMatrix(rawMatrix, elements, fillFromIndex,
                new ObjectMapper().readValue(gridFsService.getFileAsString(similarity.getStrategy().getCodebase().getRepresentationByFileType(AUTHOR).getName()), new TypeReference<Map<Short, ArrayList<String>>>() {}),
                new ObjectMapper().readValue(gridFsService.getFileAsString(similarity.getStrategy().getCodebase().getRepresentationByFileType(COMMIT).getName()), new TypeReference<Map<String, Map<String, Integer>>>() {}));
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException {
        fillRawMatrix(rawMatrix, elements, fillFromIndex,
                new ObjectMapper().readValue(gridFsService.getFileAsString(recommendation.getStrategy().getCodebase().getRepresentationByFileType(AUTHOR).getName()), new TypeReference<Map<Short, ArrayList<String>>>() {}),
                new ObjectMapper().readValue(gridFsService.getFileAsString(recommendation.getStrategy().getCodebase().getRepresentationByFileType(COMMIT).getName()), new TypeReference<Map<String, Map<String, Integer>>>() {}));
    }

    public void fillRawMatrix(
            float[][][] rawMatrix,
            Set<Short> entities,
            int fillFromIndex,
            Map<Short, ArrayList<String>> authorChanges,
            Map<String, Map<String, Integer>> commitChanges
    ) {
        int i = 0;
        for (short e1ID : entities) {
            int j = 0;

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    for (int k = fillFromIndex; k < fillFromIndex + 2; k++)
                        rawMatrix[i][j][k] = 1;
                    j++;
                    continue;
                }

                float[] metrics = calculateSimilarityMatrixCommitMetrics(e1ID, e2ID, commitChanges, authorChanges);

                for (int k = fillFromIndex, l = 0; k < fillFromIndex + 2; k++, l++)
                    rawMatrix[i][j][k] = metrics[l];
                j++;
            }
            i++;
        }
    }

    public float[] calculateSimilarityMatrixCommitMetrics(
            short e1ID, short e2ID,
            Map<String, Map<String, Integer>> commitChanges,
            Map<Short, ArrayList<String>> authorChanges
    ) {

        float commitMetricValue = 0;
        if (commitChanges.containsKey(String.valueOf(e1ID)))
            if (commitChanges.get(String.valueOf(e1ID)).containsKey(String.valueOf(e2ID)))
                commitMetricValue = (float) commitChanges.get(String.valueOf(e1ID)).get(String.valueOf(e2ID)) /
                        commitChanges.get(String.valueOf(e1ID)).get("total_commits");

        float authorMetricValue;
        try {
            authorMetricValue = (float) authorChanges.get(e1ID).stream().filter(authorChanges.get(e2ID)::contains).count() / (long) authorChanges.get(e1ID).size();
        } catch (NullPointerException e) {
            authorMetricValue = 0;
        }
        return new float[] { authorMetricValue, commitMetricValue };
    }
}
