package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyAccessesAndRepositoryDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class SimilarityScipyAccessesAndRepository extends SimilarityScipy {
    public static final String SIMILARITY_SCIPY_ACCESSES_REPOSITORY = "SIMILARITY_SCIPY_ACCESSES_REPOSITORY";
    private static final int INTERVAL = 100;
    private static final int STEP = 10;

    // Used during Similarity Generation
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    public SimilarityScipyAccessesAndRepository() {}

    public SimilarityScipyAccessesAndRepository(Strategy strategy, String name, SimilarityScipyAccessesAndRepositoryDto dto) {
        super(strategy, name, dto.getLinkageType(), dto.getWeightsList());
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
    }

    public SimilarityScipyAccessesAndRepository(RecommendMatrixSciPy recommendation) {
        super(recommendation.getStrategy(), recommendation.getName(), recommendation.getLinkageType(), recommendation.getWeightsList());
        this.profile = recommendation.getProfile();
        this.tracesMaxLimit = recommendation.getTracesMaxLimit();
        this.traceType = recommendation.getTraceType();
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_ACCESSES_REPOSITORY;
    }

    @Override
    public Map<Short, String> getIDToEntityName() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityScipyAccessesAndRepositoryDto))
            return false;

        SimilarityScipyAccessesAndRepositoryDto similarityDto = (SimilarityScipyAccessesAndRepositoryDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
                similarityDto.getProfile().equals(this.profile) &&
                similarityDto.getTracesMaxLimit() == this.tracesMaxLimit &&
                similarityDto.getTraceType() == this.traceType &&
                similarityDto.getLinkageType().equals(this.linkageType) &&
                equalWeights(similarityDto.getWeightsList());
    }

    private boolean equalWeights(List<Weights> weightsList) {
        for (int i=0; i < weightsList.size(); i++) {
            if (!weightsList.get(i).equals(getWeightsList().get(i)) ) {
                return false;
            }
        }

        return true;
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

    private int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
    }

    private Weights getWeightsByType(String type) {
        return getWeightsList().stream().filter(weight -> weight.getType().equals(type)).findFirst().orElse(null);
    }

    private float[] getWeightsAsArray() {
        float[] allWeightsAsArray = new float[getTotalNumberOfWeights()];
        int i = 0;

        for (Weights weights : getWeightsList()) {
            float[] weightsAsArray = weights.getWeights();
            for (float weight : weightsAsArray) allWeightsAsArray[i++] = weight;
        }
        return allWeightsAsArray;
    }

    @Override
    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {
        Set<Short> elements = fillElements(gridFsService, similarity);
        float[][][] rawMatrix = getEmptyRawMatrix(elements.size(), getTotalNumberOfWeights());

        int fillFromIndex = 0;
        for (Weights weights : getWeightsList()) {
            weights.fillMatrix(gridFsService, similarity, rawMatrix, elements, fillFromIndex);
            fillFromIndex += weights.getNumberOfWeights();
        }

        JSONObject matrixJSON = getSimilarityMatrixAsJSON(elements, rawMatrix, getWeightsAsArray());
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());
    }

    public Set<Short> fillElements(GridFsService gridFsService, Similarity similarity) throws IOException {
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = new ObjectMapper().readValue(
                gridFsService.getFileAsString(idToEntityRepresentation.getName()),
                new TypeReference<Map<Short, String>>() {}
        );
        return new TreeSet<>(idToEntity.keySet());
    }

    public float[][][] getEmptyRawMatrix(int numberOfEntities, int numberOfWeights) {
        return new float[numberOfEntities][numberOfEntities][numberOfWeights];
    }

    public JSONObject getSimilarityMatrixAsJSON(Set<Short> elements, float[][][] matrix, float[] weights) throws JSONException {
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
        matrixData.put("labels", elements);
        matrixData.put("matrix", matrixJSON);
        matrixData.put("clusterPrimitiveType", "Entity");
        return matrixData;
    }

    @Override
    public Set<String> generateMultipleMatrices(GridFsService gridFsService, Recommendation recommendation, Set<Short> elements, int totalNumberOfWeights) throws Exception {
        float[][][] rawMatrix = getEmptyRawMatrix(elements.size(), totalNumberOfWeights);

        int fillFromIndex = 0;
        for (Weights weights : getWeightsList()) {
            weights.fillMatrix(gridFsService, recommendation, rawMatrix, elements, fillFromIndex);
            fillFromIndex += weights.getNumberOfWeights();
        }

        int[] weights = new int[totalNumberOfWeights];
        weights[0] = INTERVAL;
        int[] remainders = new int[totalNumberOfWeights];
        remainders[0] = INTERVAL;

        Set<String> similarityMatrices = new HashSet<>();
        getMatrixCombinations(similarityMatrices, elements, rawMatrix, weights, remainders, 0);
        return similarityMatrices;
    }

    // Creates matrices based on combinations of weights ([100, 0, 0], [90, 10, 0], [90, 0, 10], [80, 20, 0], [80, 10, 10], ...)
    public void getMatrixCombinations(
            Set<String> similarityMatrices,
            Set<Short> elements,
            float[][][] rawMatrix,
            int[] weights,
            int[] remainders, int i
    )
            throws Exception
    {
        if (i + 1 == remainders.length) {
            createAndWriteSimilarityMatrix(similarityMatrices, elements, rawMatrix, weights);
            return;
        }
        else {
            remainders[i + 1] = remainders[i] - weights[i];
            weights[i + 1] = remainders[i + 1];
            getMatrixCombinations(similarityMatrices, elements, rawMatrix, weights, remainders, i+1);
        }

        weights[i] = weights[i] - STEP;
        if (weights[i] >= 0)
            getMatrixCombinations(similarityMatrices, elements, rawMatrix, weights, remainders, i);
    }

    private void createAndWriteSimilarityMatrix(
            Set<String> similarityMatrices,
            Set<Short> elements,
            float[][][] rawMatrix,
            int[] weights
    ) throws Exception {
        float[] weightsAsFloats = new float[weights.length];
        for (int i = 0; i < weights.length; i++)
            weightsAsFloats[i] = weights[i];

        JSONObject matrixJSON = getSimilarityMatrixAsJSON(elements, rawMatrix, weightsAsFloats);

        StringBuilder similarityMatrixName = new StringBuilder(getName());
        for (float weight : weights)
            similarityMatrixName.append(",").append(getWeightAsString(weight));

        similarityMatrices.add(similarityMatrixName.toString());
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), similarityMatrixName.toString());
    }

    public String getWeightAsString(float weight) {
        return Float.toString(weight).replaceAll("\\.?0*$", "");
    }
}
