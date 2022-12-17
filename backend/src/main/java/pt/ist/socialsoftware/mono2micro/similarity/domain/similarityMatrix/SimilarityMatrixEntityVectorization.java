package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;

public class SimilarityMatrixEntityVectorization extends SimilarityMatrix {

    private static final int INTERVAL = 100;
    private static final int STEP = 10;
    private static final int CODE_VECTOR_SIZE = 384;

    public SimilarityMatrixEntityVectorization() { super(); }

    public SimilarityMatrixEntityVectorization(String name) {
        this.name = name;
    }

    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {

        setGridFsService(gridFsService);
        JSONObject codeEmbeddings = getCodeEmbeddings(similarity.getStrategy());
        Map<String, Short> entityToId = getEntitiesNamesToIds(similarity.getStrategy());

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeEntitiesVectors(matrix, codeEmbeddings, entityToId);

        JSONObject matrixJSON = new JSONObject(matrix);
        setName(similarity.getName() + "_similarityMatrix");

        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());
    }

    private void computeEntitiesVectors(HashMap<String, Object> matrix, JSONObject codeEmbeddings, Map<String, Short> entityToId) throws JSONException {
        List<List<Double>> entitiesVectors = new ArrayList<>();
        List<Short> entitiesIds = new ArrayList<>();
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

                    entitiesIds.add(entityToId.get(className));
                    entitiesVectors.add(acumulator.getMeanVector());
                }
            }
        }
        matrix.put("elements", entitiesIds);
        matrix.put("matrix", entitiesVectors);
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

    private Map<String, Short> getEntitiesNamesToIds(Strategy strategy) throws IOException {
        EntityToIDRepresentation entityToId = (EntityToIDRepresentation) strategy.getCodebase().getRepresentationByFileType(ENTITY_TO_ID);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(entityToId.getName()),
                new TypeReference<Map<String, Short>>() {}
        );
    }

    private JSONObject getCodeEmbeddings(Strategy strategy) throws IOException, JSONException {
        CodeEmbeddingsRepresentation codeEmbeddings = (CodeEmbeddingsRepresentation) strategy.getCodebase().getRepresentationByFileType(CODE_EMBEDDINGS);
        return new JSONObject(
                gridFsService.getFileAsString(codeEmbeddings.getName())
        );
    }

    @Override
    public String toString() {
        return "SimilarityMatrixEntityVectorization";
    }

}
