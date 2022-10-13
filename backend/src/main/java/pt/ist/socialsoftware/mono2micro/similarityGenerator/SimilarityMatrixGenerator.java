package pt.ist.socialsoftware.mono2micro.similarityGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.similarityGenerator.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityGenerator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;

import java.io.ByteArrayInputStream;
import java.util.*;

public class SimilarityMatrixGenerator {
    private static final int INTERVAL = 100;
    private static final int STEP = 10;
    private final GridFsService gridFsService;

    public SimilarityMatrixGenerator() {
        this.gridFsService = ContextManager.get().getBean(GridFsService.class);
    }

    public void createSimilarityMatrixFromWeights(SimilarityMatrix similarity) throws Exception {
        Set<Short> elements = similarity.fillElements(gridFsService);

        float[][][] rawMatrix = getEmptyRawMatrix(elements.size(), similarity.getTotalNumberOfWeights());

        int fillFromIndex = 0;
        for (Weights weights : similarity.getWeightsList()) {
            weights.fillMatrix(gridFsService, (Similarity) similarity, rawMatrix, elements, fillFromIndex);
            fillFromIndex += weights.getNumberOfWeights();
        }

        JSONObject matrixJSON = getSimilarityMatrixAsJSON(elements, rawMatrix, similarity.getWeightsAsArray());
        similarity.setSimilarityMatrixName(similarity.getName() + "_similarityMatrix");
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), similarity.getSimilarityMatrixName());
    }

    public void createSimilarityMatrices(SimilarityMatrices recommendation) throws Exception {
        Set<Short> elements = recommendation.fillElements(gridFsService);

        float[][][] rawMatrix = getEmptyRawMatrix(elements.size(), recommendation.getTotalNumberOfWeights());

        int fillFromIndex = 0;
        for (Weights weights : recommendation.getWeightsList()) {
            weights.fillMatrix(gridFsService, (Recommendation) recommendation, rawMatrix, elements, fillFromIndex);
            fillFromIndex += weights.getNumberOfWeights();
        }

        int[] weights = new int[recommendation.getTotalNumberOfWeights()];
        weights[0] = INTERVAL;
        int[] remainders = new int[recommendation.getTotalNumberOfWeights()];
        remainders[0] = INTERVAL;

        getMatrixCombinations(recommendation, elements, rawMatrix, weights, remainders, 0);
    }

    public JSONObject getSimilarityMatrixAsJSON(
            Set<Short> elements,
            float[][][] matrix,
            float[] weights
    ) throws JSONException {
        JSONObject matrixData = new JSONObject();
        JSONArray matrixJSON = new JSONArray();

        int i = 0;
        for (short e1ID : elements) {
            JSONArray matrixRow = new JSONArray();
            int j = 0;

            for (short e2ID : elements) {
                if (e1ID == e2ID) {
                    matrixRow.put(1);
                    j++;
                    continue;
                }

                float metric = 0;
                for (int k = 0; k < weights.length; k++)
                    metric += matrix[i][j][k] * weights[k] / 100;

                matrixRow.put(metric);
                j++;
            }
            matrixJSON.put(matrixRow);
            i++;
        }

        matrixData.put("elements", elements);
        matrixData.put("matrix", matrixJSON);
        return matrixData;
    }

    public float[][][] getEmptyRawMatrix(int numberOfEntities, int numberOfWeights) {
        return new float[numberOfEntities][numberOfEntities][numberOfWeights];
    }

    // Creates matrices based on combinations of weights ([100, 0, 0], [90, 10, 0], [90, 0, 10], [80, 20, 0], [80, 10, 10], ...)
    public void getMatrixCombinations(
            SimilarityMatrices recommendation,
            Set<Short> elements,
            float[][][] rawMatrix,
            int[] weights,
            int[] remainders, int i
    )
            throws Exception
    {
        if (i + 1 == remainders.length) {
            createAndWriteSimilarityMatrix(recommendation, elements, rawMatrix, weights);
            return;
        }
        else {
            remainders[i + 1] = remainders[i] - weights[i];
            weights[i + 1] = remainders[i + 1];
            getMatrixCombinations(recommendation, elements, rawMatrix, weights, remainders, i+1);
        }

        weights[i] = weights[i] - STEP;
        if (weights[i] >= 0)
            getMatrixCombinations(recommendation, elements, rawMatrix, weights, remainders, i);
    }

    private void createAndWriteSimilarityMatrix(
            SimilarityMatrices recommendation,
            Set<Short> elements,
            float[][][] rawMatrix,
            int[] weights
    ) throws Exception {
        float[] weightsAsFloats = new float[weights.length];
        for (int i = 0; i < weights.length; i++)
            weightsAsFloats[i] = weights[i];

        JSONObject matrixJSON = getSimilarityMatrixAsJSON(elements, rawMatrix, weightsAsFloats);

        StringBuilder similarityMatrixName = new StringBuilder(recommendation.getName());
        for (float weight : weights)
            similarityMatrixName.append(",").append(getWeightAsString(weight));

        recommendation.addSimilarityMatrixName(similarityMatrixName.toString());
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), similarityMatrixName.toString());
    }

    public String getWeightAsString(float weight) {
        return Float.toString(weight).replaceAll("\\.?0*$", "");
    }
}