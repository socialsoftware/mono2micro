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
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationSequenceOfAccessesWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyAccessesAndRepositoryDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationSequenceOfAccessesWeights.FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS;

public class SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES = "SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES";
    private static final int INTERVAL = 100;
    private static final int STEP = 10;

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses() {}

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(Strategy strategy, String name, SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto dto) {
        super(strategy, name, dto.getLinkageType(), dto.getWeightsList());
    }

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(RecommendMatrixSciPy recommendation) {
        super(recommendation.getStrategy(), recommendation.getName(), recommendation.getLinkageType(), recommendation.getWeightsList());
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityScipyAccessesAndRepositoryDto))
            return false;

        SimilarityScipyAccessesAndRepositoryDto similarityDto = (SimilarityScipyAccessesAndRepositoryDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
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

    @Override
    public String getProfile() {
        return "Generic";
    }
    @Override
    public int getTracesMaxLimit() {
        return 0;
    }
    @Override
    public Constants.TraceType getTraceType() {
        return Constants.TraceType.ALL;
    }

    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {
        JSONObject codeEmbeddings = getCodeEmbeddings(similarity.getStrategy());
        JSONObject functionalityTraces = getFunctionalityTraces(similarity.getStrategy());
        Map<String, Short> entityToId = getEntitiesNamesToIds(similarity.getStrategy());

        HashMap<String, Object> matrix = new HashMap<>();
        List<HashMap> entitiesVectors = this.computeEntitiesVectors(codeEmbeddings, entityToId);
        this.computeSequenceOfAccessesFunctionalityVectors(matrix, functionalityTraces, entitiesVectors);

        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        AccessesRepresentation accessesInfo = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);
        matrix.put("translationFileName", idToEntity.getName());
        matrix.put("accessesFileName", accessesInfo.getName());

        JSONObject matrixJSON = new JSONObject(matrix);

        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());
    }

    private void computeSequenceOfAccessesFunctionalityVectors(
            HashMap<String, Object> matrix,
            JSONObject functionalityTraces,
            List<HashMap> entitiesVectors
    ) throws JSONException {
        List<List<Double>> functionalityVectors = new ArrayList<>();
        List<String> featuresNames = new ArrayList<>();

        FunctionalityVectorizationSequenceOfAccessesWeights weights = (FunctionalityVectorizationSequenceOfAccessesWeights)
                getWeightsByType(FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS);

        Iterator<String> keys = functionalityTraces.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (functionalityTraces.get(key) instanceof JSONObject) {
                JSONObject traces = (JSONObject) functionalityTraces.get(key);
                JSONArray trace = traces.getJSONArray("t");
                JSONObject trace_obj  = trace.getJSONObject(0);
                JSONArray accesses = trace_obj.getJSONArray("a");

                float count = 0;
                ArrayList<Double> vector = new ArrayList<Double>();
                for (int idx = 0; idx < 384; idx++) {
                    vector.add(0.0);
                }

                for (int i = 0; i < accesses.length(); i++) {
                    JSONArray access = accesses.getJSONArray(i);
                    String accessType = access.getString(0);
                    Integer entityId = access.getInt(1);
                    float weight = 0;

                    if (accessType.equals("R")) {
                        weight = weights.getReadMetricWeight();
                    } else {
                        weight = weights.getWriteMetricWeight();
                    }
                    for (HashMap entity : entitiesVectors) {
                        short translationID = (short) entity.get("translationID");
                        if (translationID == entityId) {
                            ArrayList<Double> code_vector = (ArrayList<Double>) entity.get("codeVector");
                            for (int idx = 0; idx < 384; idx++) {
                                vector.set(idx, vector.get(idx) + weight * code_vector.get(idx));
                            }

                            count += weight;
                            break;
                        }
                    }
                }
                if (count > 0) {
                    vectorDivision(vector, count);
                    functionalityVectors.add(vector);
                    featuresNames.add(key);
                }
            }
        }

        matrix.put("elements", featuresNames);
        matrix.put("labels", featuresNames);
        matrix.put("matrix", functionalityVectors);
        matrix.put("clusterPrimitiveType", "Functionality");
    }

    private List<HashMap> computeEntitiesVectors(JSONObject codeEmbeddings, Map<String, Short> entityToId) throws JSONException {
        List<HashMap> entitiesVectors = new ArrayList<>();
        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String classType = cls.getString("type");
                String className = cls.getString("name");

                if (classType.equals("Entity") && !className.endsWith("_Base")) {

                    Acumulator acumulator = new Acumulator();
                    getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        JSONArray code_vector = method.getJSONArray("codeVector");
                        acumulator.addVector(code_vector);
                    }

                    HashMap<String, Object> entityEmbeddings = new HashMap<String, Object>();
                    entityEmbeddings.put("package", pack.getString("name"));
                    entityEmbeddings.put("name", className);
                    entityEmbeddings.put("translationID", entityToId.get(className));
                    entityEmbeddings.put("codeVector", acumulator.getMeanVector());
                    entitiesVectors.add(entityEmbeddings);

                }
            }
        }
        return entitiesVectors;
    }

    private Acumulator getAscendedClassesMethodsCodeVectors(
            JSONArray packages,
            Acumulator acumulator,
            String qualifiedName
    )
            throws JSONException
    {
        if (qualifiedName.isEmpty()) return acumulator;

        String[] splittedStr = qualifiedName.split("[.]");
        StringBuilder packageName = new StringBuilder(splittedStr[0]);
        for (int i = 1; i < splittedStr.length-1; i++) {
            packageName.append(".").append(splittedStr[i]);
        }
        String className = splittedStr[splittedStr.length - 1];

        JSONObject cls = getClassMethodsCodeVectors(acumulator, packages, packageName.toString(), className);
        if (cls == null) return acumulator;

        return getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));
    }

    public JSONObject getClassMethodsCodeVectors(
            Acumulator acumulator,
            JSONArray packages,
            String packageName,
            String className
    )
            throws JSONException
    {
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);

            if (pack.getString("name").equals(packageName)) {
                JSONArray classes = pack.optJSONArray("classes");

                for (int j = 0; j < classes.length(); j++) {
                    JSONObject cls = classes.getJSONObject(j);

                    if (cls.getString("name").equals(className)) {
                        JSONArray methods = cls.getJSONArray("methods");

                        for (int k = 0; k < methods.length(); k++) {
                            JSONObject method = methods.getJSONObject(k);
                            JSONArray codeVector = method.getJSONArray("codeVector");
                            acumulator.addVector(codeVector);
                        }

                        return cls;
                    }
                }
            }
        }
        return null;
    }

    public void vectorDivision(ArrayList<Double> vector, float count) {
        for (int i = 0; i < vector.size(); i++) {
            vector.set(i, vector.get(i) / count);
        }
    }

    private Weights getWeightsByType(String type) {
        return getWeightsList().stream().filter(weight -> weight.getType().equals(type)).findFirst().orElse(null);
    }

    private Map<String, Short> getEntitiesNamesToIds(Strategy strategy) throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        EntityToIDRepresentation entityToId = (EntityToIDRepresentation) strategy.getCodebase().getRepresentationByFileType(ENTITY_TO_ID);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(entityToId.getName()),
                new TypeReference<Map<String, Short>>() {}
        );
    }

    private Map<String, Short> getTranslationIdToEntity(Strategy strategy) throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) strategy.getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(idToEntity.getName()),
                new TypeReference<Map<Short, String>>() {}
        );
    }

    private JSONObject getFunctionalityTraces(Strategy strategy) throws IOException, JSONException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        AccessesRepresentation accessesRepresentation = (AccessesRepresentation) strategy.getCodebase().getRepresentationByFileType(ACCESSES);
        return new JSONObject(
                gridFsService.getFileAsString(accessesRepresentation.getName())
        );
    }

    @Override
    public Set<String> generateMultipleMatrices(
            GridFsService gridFsService,
            Recommendation recommendation,
            Set<Short> elements,
            int totalNumberOfWeights
    ) throws Exception {
        JSONObject codeEmbeddings = getCodeEmbeddings(recommendation.getStrategy());
        JSONObject functionalityTraces = getFunctionalityTraces(recommendation.getStrategy());
        Map<String, Short> entityToId = getEntitiesNamesToIds(recommendation.getStrategy());
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        AccessesRepresentation accessesInfo = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);

        int[] weights = new int[totalNumberOfWeights];
        weights[0] = INTERVAL;
        int[] remainders = new int[totalNumberOfWeights];
        remainders[0] = INTERVAL;

        Set<String> similarityMatrices = new HashSet<>();
        getMatrixCombinations(similarityMatrices, codeEmbeddings, functionalityTraces, idToEntity.getName(), accessesInfo.getName(), entityToId, weights, remainders, 0);
        return similarityMatrices;
    }

    private void getMatrixCombinations(
            Set<String> similarityMatrices,
            JSONObject codeEmbeddings,
            JSONObject functionalityTraces,
            String translationFileName,
            String accessesFileName,
            Map<String, Short> entityToId,
            int[] weights,
            int[] remainders,
            int i
    ) throws Exception {
        if (i + 1 == remainders.length) {
            createAndWriteSimilarityMatrix(similarityMatrices, codeEmbeddings, functionalityTraces, weights, translationFileName, accessesFileName, entityToId);
            return;
        }
        else {
            remainders[i + 1] = remainders[i] - weights[i];
            weights[i + 1] = remainders[i + 1];
            getMatrixCombinations(similarityMatrices, codeEmbeddings, functionalityTraces, translationFileName, accessesFileName, entityToId, weights, remainders, i+1);
        }

        weights[i] = weights[i] - STEP;
        if (weights[i] >= 0)
            getMatrixCombinations(similarityMatrices, codeEmbeddings, functionalityTraces, translationFileName, accessesFileName, entityToId, weights, remainders, i);
    }

    private void createAndWriteSimilarityMatrix(
            Set<String> similarityMatrices,
            JSONObject codeEmbeddings,
            JSONObject functionalityTraces,
            int[] weights,
            String translationFileName,
            String accessesFileName,
            Map<String, Short> entityToId
    ) throws Exception {
        float[] weightsAsFloats = new float[weights.length];
        for (int i = 0; i < weights.length; i++)
            weightsAsFloats[i] = weights[i];

        FunctionalityVectorizationSequenceOfAccessesWeights fvsaWeights = (FunctionalityVectorizationSequenceOfAccessesWeights) getWeightsByType(FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS);

        fvsaWeights.setReadMetricWeight(weightsAsFloats[0]);
        fvsaWeights.setWriteMetricWeight(weightsAsFloats[1]);

        HashMap<String, Object> matrix = new HashMap<>();
        List<HashMap> entitiesVectors = this.computeEntitiesVectors(codeEmbeddings, entityToId);
        this.computeSequenceOfAccessesFunctionalityVectors(matrix, functionalityTraces, entitiesVectors);
        matrix.put("translationFileName", translationFileName);
        matrix.put("accessesFileName", accessesFileName);
        JSONObject matrixJSON = new JSONObject(matrix);

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

    @Override
    public String toString() {
        return "SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses";
    }

}
