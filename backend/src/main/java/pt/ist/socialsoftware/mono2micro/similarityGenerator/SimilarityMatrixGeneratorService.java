package pt.ist.socialsoftware.mono2micro.similarityGenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.interfaces.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.generator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.utils.AccessesUtils;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.utils.RepositoryUtils;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.AccessesWeights.ACCESSES_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.RepositoryWeights.REPOSITORY_WEIGHTS;

@Service
public class SimilarityMatrixGeneratorService {
    private static final int INTERVAL = 100;
    private static final int STEP = 10;

    @Autowired
    RepresentationService representationService;

    @Autowired
    GridFsService gridFsService;

    //####################################################################################################
    //                                  NEW ADDITIONS IN THE FOLLOWING METHODS
    //####################################################################################################

    public void createSimilarityMatrixFromWeights(SimilarityMatrix similarity) throws Exception {
        Set<Short> elements = new TreeSet<>();  // NEEDS TO BE FILLED
        float[][][] rawMatrix;                  // NEEDS TO BE FILLED

        List<String> representations = similarity.getStrategy().getRepresentationTypes();
        if (representations.contains(ACCESSES)) {
            AccessesRepresentation accesses = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
            AccessesSimilarity s = (AccessesSimilarity) similarity;
            fillElementsFromAccesses(accesses, elements, s.getProfile(), s.getTraceType(), s.getTracesMaxLimit());
        }
        // ADD OTHER WAYS TO FILL ELEMENTS HERE, ACCORDING TO AVAILABLE WEIGHTS

        rawMatrix = getEmptyRawMatrix(elements.size(), similarity.getTotalNumberOfWeights());

        // FILL MATRIX
        int fillFromIndex = 0;
        for (String weightType : similarity.getAllWeightsTypes()) {
            Weights weights = similarity.getWeightsByType(weightType);
            switch (weightType) {
                case ACCESSES_WEIGHTS:
                    AccessesSimilarity s = (AccessesSimilarity) similarity;
                    AccessesRepresentation accesses = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
                    fillMatrixFromAccessesWeights(accesses, s.getProfile(), s.getTraceType(), s.getTracesMaxLimit(), rawMatrix, fillFromIndex);
                    break;
                case REPOSITORY_WEIGHTS:
                    AuthorRepresentation authors = (AuthorRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(AUTHOR);
                    CommitRepresentation commits = (CommitRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(COMMIT);
                    fillMatrixFromRepositoryWeights(authors, commits, rawMatrix, elements, fillFromIndex);
                    break;
                // ADD OTHER WAYS TO FILL THE MATRIX HERE
            }
            fillFromIndex += weights.getNumberOfWeights();
        }
        JSONObject matrixJSON = getSimilarityMatrixAsJSON(elements, rawMatrix, similarity.getWeightsAsArray());
        similarity.setSimilarityMatrixName(similarity.getName() + "_similarityMatrix");
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), similarity.getSimilarityMatrixName());
    }

    public void createSimilarityMatrices(SimilarityMatrices recommendation) throws Exception {
        Set<Short> elements = new TreeSet<>();  // NEEDS TO BE FILLED

        List<String> representations = recommendation.getStrategy().getRepresentationTypes();
        if (representations.contains(ACCESSES)) {
            AccessesRepresentation accesses = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
            RecommendForSciPy s = (RecommendForSciPy) recommendation;
            fillElementsFromAccesses(accesses, elements, s.getProfile(), s.getTraceType(), s.getTracesMaxLimit());
        }
        // ADD OTHER WAYS TO FILL ELEMENTS HERE, ACCORDING TO AVAILABLE WEIGHTS

        float[][][] rawMatrix = getEmptyRawMatrix(elements.size(), recommendation.getTotalNumberOfWeights());

        int[] weights = new int[recommendation.getTotalNumberOfWeights()];
        weights[0] = INTERVAL;
        int[] remainders = new int[recommendation.getTotalNumberOfWeights()];
        remainders[0] = INTERVAL;

        getMatrixCombinations(recommendation, elements, rawMatrix, weights, remainders, 0);
    }

    //####################################################################################################
    //                                  NEW ADDITIONS END HERE
    //####################################################################################################

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

    //================================================================================================
    //                                      Recommendation
    //================================================================================================

    // Creates matrices based on combinations of weights ([100, 0, 0], [90, 10, 0], [90, 0, 10], [80, 20, 0], [80, 10, 10], ...)
    public void getMatrixCombinations(SimilarityMatrices recommendation, Set<Short> elements, float[][][] rawMatrix, int[] weights, int[] remainders, int i) throws Exception {
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

    public static String getWeightAsString(float weight) {
        return Float.toString(weight).replaceAll("\\.?0*$", "");
    }

    // TODO THIS METHOD SHOULD BE REFACTORED TO ONLY OBTAIN ELEMENTS
    public void fillElementsFromAccesses(AccessesRepresentation accesses, Set<Short> elements, String profile, Constants.TraceType traceType, int tracesMaxLimit)
            throws IOException, JSONException
    {
        Map<String, Integer> e1e2PairCount = new HashMap<>();
        Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>
        AccessesUtils.fillDataStructures(
                elements,
                e1e2PairCount,
                entityFunctionalities,
                new FunctionalityTracesIterator(representationService.getRepresentationFileAsInputStream(accesses.getName()), tracesMaxLimit),
                accesses.getProfile(profile),
                traceType);
    }

    //================================================================================================
    //                                          ACCESSES_WEIGHTS
    //================================================================================================

    public void fillMatrixFromAccessesWeights(AccessesRepresentation accesses, String profile, Constants.TraceType traceType, int tracesMaxLimit, float[][][] rawMatrix, int fillFromIndex) throws IOException, JSONException {
        AccessesUtils.fillRawMatrixFromAccesses(
                rawMatrix,
                fillFromIndex,
                representationService.getRepresentationFileAsInputStream(accesses.getName()),
                accesses.getProfile(profile),
                traceType,
                tracesMaxLimit);
    }

    //================================================================================================
    //                                          REPOSITORY_WEIGHTS
    //================================================================================================

    public void fillMatrixFromRepositoryWeights(AuthorRepresentation authors, CommitRepresentation commits, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException {
        RepositoryUtils.fillRawMatrix(
                rawMatrix,
                elements,
                fillFromIndex,
                new ObjectMapper().readValue(representationService.getRepresentationFileAsString(authors.getName()), new TypeReference<Map<Short, ArrayList<String>>>() {}),
                new ObjectMapper().readValue(representationService.getRepresentationFileAsString(commits.getName()), new TypeReference<Map<String, Map<String, Integer>>>() {}));
    }
}