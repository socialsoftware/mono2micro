package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationCallGraphWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationCallGraphWeights.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS;

public class SimilarityMatrixFunctionalityVectorizationByCallGraph extends SimilarityMatrix {

    private static final Integer MIN_DEPTH = 1;

    private int depth = 2;

    public SimilarityMatrixFunctionalityVectorizationByCallGraph() { super(); }

    public SimilarityMatrixFunctionalityVectorizationByCallGraph(List<Weights> weightsList, int depth) {
        super(weightsList);
        this.depth = depth;
    }

    public SimilarityMatrixFunctionalityVectorizationByCallGraph(String name, List<Weights> weightsList, int depth) {
        super(name, weightsList);
        this.depth = depth;
    }

    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {

        setGridFsService(gridFsService);
        JSONObject codeEmbeddings = getCodeEmbeddings(similarity.getStrategy());

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeMethodCallsFeaturesVectors(matrix, codeEmbeddings);

        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        AccessesRepresentation accessesInfo = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);
        matrix.put("translationFileName", idToEntity.getName());
        matrix.put("accessesFileName", accessesInfo.getName());

        JSONObject matrixJSON = new JSONObject(matrix);
        setName(similarity.getName() + "_similarityMatrix");

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

                } else {
                    System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
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

    public void vectorSum(ArrayList<Double> vector, ArrayList<Double> array)
    {
        for (int i = 0; i < array.size(); i++) {
            vector.set(i, vector.get(i) + array.get(i));
        }
    }

    public void vectorDivision(ArrayList<Double> vector, float count) {
        for (int i = 0; i < vector.size(); i++) {
            vector.set(i, vector.get(i) / count);
        }
    }

    private Weights getWeightsByType(String type) {
        return getWeightsList().stream().filter(weight -> weight.getType().equals(type)).findFirst().orElse(null);
    }

    private JSONObject getCodeEmbeddings(Strategy strategy) throws IOException, JSONException {
        CodeEmbeddingsRepresentation codeEmbeddings = (CodeEmbeddingsRepresentation) strategy.getCodebase().getRepresentationByFileType(CODE_EMBEDDINGS);
        return new JSONObject(
                gridFsService.getFileAsString(codeEmbeddings.getName())
        );
    }

    @Override
    public String toString() {
        return "SimilarityMatrixFunctionalityVectorizationByCallGraph";
    }

}
