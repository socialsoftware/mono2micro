package pt.ist.socialsoftware.mono2micro.similarity.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationCallGraphWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyAccessesAndRepositoryDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyFunctionalityVectorizationByCallGraphDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationCallGraphWeights.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS;

public class SimilarityScipyFunctionalityVectorizationByCallGraph extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH = "SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH";
    private static final Integer MIN_DEPTH = 1;
    private static final int INTERVAL = 100;
    private static final int STEP = 10;

    private int depth = 2;

    public SimilarityScipyFunctionalityVectorizationByCallGraph() {}

    public SimilarityScipyFunctionalityVectorizationByCallGraph(Strategy strategy, String name, SimilarityScipyFunctionalityVectorizationByCallGraphDto dto) {
        super(strategy, name, dto.getLinkageType(), dto.getWeightsList());
        this.depth = dto.getDepth();
    }

    public SimilarityScipyFunctionalityVectorizationByCallGraph(RecommendMatrixSciPy recommendation) {
        super(recommendation.getStrategy(), recommendation.getName(), recommendation.getLinkageType(), recommendation.getWeightsList());
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;
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
        this.matchEntitiesTranslationIds(codeEmbeddings);

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeMethodCallsFeaturesVectors(matrix, codeEmbeddings);

        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        AccessesRepresentation accessesInfo = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);
        matrix.put("translationFileName", idToEntity.getName());
        matrix.put("accessesFileName", accessesInfo.getName());

        JSONObject matrixJSON = new JSONObject(matrix);
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());
    }

    private void computeMethodCallsFeaturesVectors(HashMap<String, Object> matrix, JSONObject codeEmbeddings) throws JSONException {
        List<List<Double>> featuresVectors = new ArrayList<>();
        List<String> featuresNames = new ArrayList<>();
        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String className = cls.getString("name");
                String classType = cls.getString("type");

                for (int k = 0; k < methods.length(); k++) {
                    JSONObject method = methods.getJSONObject(k);
                    String methodSignature = method.getString("signature");

                    if (method.getString("type").equals("Controller")) {

                        Acumulator acumulator = getMethodCallsVectors(packages, classType, method, this.depth);

                        if (acumulator.getCount() > 0) {
                            vectorDivision(acumulator.getSum(), acumulator.getCount());
                        }

                        String[] splitFeatureName = methodSignature.split("\\(")[0].split("\\.");
                        String featureName = splitFeatureName[splitFeatureName.length - 1];
                        featuresNames.add(className + "." + featureName);
                        featuresVectors.add(acumulator.getSum());
                    }
                }
            }
        }

        matrix.put("elements", featuresNames);
        matrix.put("labels", featuresNames);
        matrix.put("matrix", featuresVectors);
        matrix.put("clusterPrimitiveType", "Functionality");
    }

    public Acumulator getMethodCallsVectors(
            JSONArray packages,
            String classType,
            JSONObject method,
            int maxDepth
    )
            throws JSONException
    {
        float count = 0;
        ArrayList<Double> vector = new ArrayList<Double>();
        JSONArray code_vector = method.getJSONArray("codeVector");
        JSONArray methodCalls = method.optJSONArray("methodCalls");
        String methodType = method.getString("type");

        FunctionalityVectorizationCallGraphWeights weights = (FunctionalityVectorizationCallGraphWeights) getWeightsByType(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS);

        if (methodType.equals("Controller")) {
            count = weights.getControllersWeight();
        } else if (classType.equals("Service") || methodType.equals("Service")) {
            count = weights.getServicesWeight();
        } else if (classType.equals("Entity")) {
            count = weights.getEntitiesWeight();
        } else {
            count = weights.getIntermediateMethodsWeight();
        }

        for (int idx = 0; idx < 384; idx++) {
            vector.add(count * code_vector.getDouble(idx));
        }

        if (maxDepth <= MIN_DEPTH || methodCalls.length() == 0) {
            return new Acumulator(vector, count);
        }

        for (int l = 0; l < methodCalls.length(); l++) {
            JSONObject methodCall = methodCalls.getJSONObject(l);

            try {
                JSONObject met = getMethodCall(
                        packages,
                        methodCall.getString("packageName"),
                        methodCall.getString("className"),
                        methodCall.getString("signature")
                );

                if (met != null) {

                    Acumulator acum = getMethodCallsVectors(
                            packages,
                            met.getString("classType"),
                            met,
                            maxDepth - 1
                    );

                    vectorSum(vector, acum.getSum());
                    count += acum.getCount();

                }
            } catch (JSONException je) {
                System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
            }
        }

        return new Acumulator(vector, count);
    }

    public JSONObject getMethodCall(JSONArray packages, String callPackage, String callClass, String callSignature)
            throws JSONException
    {
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);

            if (pack.getString("name").equals(callPackage)) {
                JSONArray classes = pack.optJSONArray("classes");

                for (int j = 0; j < classes.length(); j++) {
                    JSONObject cls = classes.getJSONObject(j);
                    String classType = cls.getString("type");

                    if (cls.getString("name").equals(callClass)) {
                        JSONArray methods = cls.optJSONArray("methods");

                        for (int k = 0; k < methods.length(); k++) {
                            JSONObject method = methods.getJSONObject(k);

                            if (method.getString("signature").equals(callSignature)) {
                                method.put("classType", classType);
                                return method;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void vectorSum(List<Double> vector, List<Double> array)
    {
        for (int i = 0; i < array.size(); i++) {
            vector.set(i, vector.get(i) + array.get(i));
        }
    }

    public void vectorDivision(List<Double> vector, float count) {
        for (int i = 0; i < vector.size(); i++) {
            vector.set(i, vector.get(i) / count);
        }
    }

    private Weights getWeightsByType(String type) {
        return getWeightsList().stream().filter(weight -> weight.getType().equals(type)).findFirst().orElse(null);
    }

    @Override
    public Set<String> generateMultipleMatrices(
            GridFsService gridFsService,
            Recommendation recommendation,
            Set<Short> elements,
            int totalNumberOfWeights
    ) throws Exception {
        JSONObject codeEmbeddings = getCodeEmbeddings(recommendation.getStrategy());
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        AccessesRepresentation accessesInfo = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);

        int[] weights = new int[totalNumberOfWeights];
        weights[0] = INTERVAL;
        int[] remainders = new int[totalNumberOfWeights];
        remainders[0] = INTERVAL;

        Set<String> similarityMatrices = new HashSet<>();
        getMatrixCombinations(similarityMatrices, codeEmbeddings, idToEntity.getName(), accessesInfo.getName(), weights, remainders, 0);
        return similarityMatrices;
    }

    // Creates matrices based on combinations of weights ([100, 0, 0], [90, 10, 0], [90, 0, 10], [80, 20, 0], [80, 10, 10], ...)
    public void getMatrixCombinations(
            Set<String> similarityMatrices,
            JSONObject codeEmbeddings,
            String translationFileName,
            String accessesFileName,
            int[] weights,
            int[] remainders, int i
    )
            throws Exception
    {
        if (i + 1 == remainders.length) {
            createAndWriteSimilarityMatrix(similarityMatrices, codeEmbeddings, weights, translationFileName, accessesFileName);
            return;
        }
        else {
            remainders[i + 1] = remainders[i] - weights[i];
            weights[i + 1] = remainders[i + 1];
            getMatrixCombinations(similarityMatrices, codeEmbeddings, translationFileName, accessesFileName, weights, remainders, i+1);
        }

        weights[i] = weights[i] - STEP;
        if (weights[i] >= 0)
            getMatrixCombinations(similarityMatrices, codeEmbeddings, translationFileName, accessesFileName, weights, remainders, i);
    }

    private void createAndWriteSimilarityMatrix(
            Set<String> similarityMatrices,
            JSONObject codeEmbeddings,
            int[] weights,
            String translationFileName,
            String accessesFileName
    ) throws Exception {

        float[] weightsAsFloats = new float[weights.length];
        for (int i = 0; i < weights.length; i++)
            weightsAsFloats[i] = weights[i];

        FunctionalityVectorizationCallGraphWeights fvcgWeights = (FunctionalityVectorizationCallGraphWeights) getWeightsByType(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS);

        fvcgWeights.setControllersWeight(weightsAsFloats[0]);
        fvcgWeights.setServicesWeight(weightsAsFloats[1]);
        fvcgWeights.setEntitiesWeight(weightsAsFloats[2]);
        fvcgWeights.setIntermediateMethodsWeight(weightsAsFloats[3]);

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeMethodCallsFeaturesVectors(matrix, codeEmbeddings);
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
        return "SimilarityScipyFunctionalityVectorizationByCallGraph";
    }

}
