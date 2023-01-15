package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationSequenceOfAccessesWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationSequenceOfAccessesWeights.FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS;

public class SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses extends SimilarityMatrix {

    public SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses() { super(); }

    public SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses(List<Weights> weightsList) {
        super(weightsList);
    }

    public SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses(String name, List<Weights> weightsList) {
        super(name, weightsList);
    }

    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {

        setGridFsService(gridFsService);
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
        setName(similarity.getName() + "_similarityMatrix");

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
        EntityToIDRepresentation entityToId = (EntityToIDRepresentation) strategy.getCodebase().getRepresentationByFileType(ENTITY_TO_ID);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(entityToId.getName()),
                new TypeReference<Map<String, Short>>() {}
        );
    }

    private Map<String, Short> getTranslationIdToEntity(Strategy strategy) throws IOException {
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) strategy.getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(idToEntity.getName()),
                new TypeReference<Map<Short, String>>() {}
        );
    }

    private JSONObject getCodeEmbeddings(Strategy strategy) throws IOException, JSONException {
        CodeEmbeddingsRepresentation codeEmbeddings = (CodeEmbeddingsRepresentation) strategy.getCodebase().getRepresentationByFileType(CODE_EMBEDDINGS);
        return new JSONObject(
                gridFsService.getFileAsString(codeEmbeddings.getName())
        );
    }

    private JSONObject getFunctionalityTraces(Strategy strategy) throws IOException, JSONException {
        AccessesRepresentation accessesRepresentation = (AccessesRepresentation) strategy.getCodebase().getRepresentationByFileType(ACCESSES);
        return new JSONObject(
                gridFsService.getFileAsString(accessesRepresentation.getName())
        );
    }

    @Override
    public String toString() {
        return "SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses";
    }

}
