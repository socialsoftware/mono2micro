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
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyClassVectorizationDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyAccessesAndRepositoryDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;

public class SimilarityScipyClassVectorization extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_CLASS_VECTORIZATION = "SIMILARITY_SCIPY_CLASS_VECTORIZATION";

    public SimilarityScipyClassVectorization() {}

    public SimilarityScipyClassVectorization(Strategy strategy, String name, SimilarityScipyClassVectorizationDto dto) {
        super(strategy, name, dto.getLinkageType(), new ArrayList<>());
    }

    public SimilarityScipyClassVectorization(RecommendMatrixSciPy recommendation) {
        super(recommendation.getStrategy(), recommendation.getName(), recommendation.getLinkageType(), recommendation.getWeightsList());
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_CLASS_VECTORIZATION;
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityScipyAccessesAndRepositoryDto))
            return false;

        SimilarityScipyAccessesAndRepositoryDto similarityDto = (SimilarityScipyAccessesAndRepositoryDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
                similarityDto.getLinkageType().equals(this.linkageType);
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
        Map<String, Short> entityToId = getEntitiesNamesToIds(similarity.getStrategy());
        this.matchEntitiesTranslationIds(similarity.getStrategy(), codeEmbeddings);

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeClassesVectors(matrix, codeEmbeddings, entityToId);

        JSONObject matrixJSON = new JSONObject(matrix);

        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());
    }

    private void computeClassesVectors(HashMap<String, Object> matrix, JSONObject codeEmbeddings, Map<String, Short> entityToId) throws JSONException {
        List<List<Double>> classesVectors = new ArrayList<>();
        List<String> classesNames = new ArrayList<>();
        List<Integer> translationIds = new ArrayList<>();
        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String className = cls.getString("name");

                if (!className.endsWith("_Base")) {

                    Acumulator acumulator = new Acumulator();
                    getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        JSONArray codeVector = method.getJSONArray("codeVector");
                        acumulator.addVector(codeVector);
                    }

                    if (acumulator.getCount() > 0) {
                        HashMap<String, Object> classEmbeddings = new HashMap<>();
                        classEmbeddings.put("package", pack.getString("name"));
                        classEmbeddings.put("name", className);
                        classEmbeddings.put("type", cls.getString("type"));

                        if (cls.has("translationID")) {
                            translationIds.add(cls.getInt("translationID"));
                        } else {
                            translationIds.add(-1);
                        }
                        classesNames.add(className);
                        classesVectors.add(acumulator.getMeanVector());
                    }
                }
            }
        }
        matrix.put("elements", classesNames);
        matrix.put("labels", classesNames);
        matrix.put("translationIds", translationIds);
        matrix.put("matrix", classesVectors);
        matrix.put("clusterPrimitiveType", "Class");
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

    public void matchEntitiesTranslationIds(Strategy strategy, JSONObject codeEmbeddings)
            throws JSONException, IOException
    {
        Map<String, Short> translationEntityToId = getEntitiesNamesToIds(strategy);
        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                String className = cls.getString("name");

                if (translationEntityToId.containsKey(className)) {
                    int entityId = translationEntityToId.get(className);
                    cls.put("type", "Entity");
                    cls.put("translationID", entityId);
                }
            }
        }
    }

    private Map<String, Short> getEntitiesNamesToIds(Strategy strategy) throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        EntityToIDRepresentation entityToId = (EntityToIDRepresentation) strategy.getCodebase().getRepresentationByFileType(ENTITY_TO_ID);
        return new ObjectMapper().readValue(
                gridFsService.getFileAsString(entityToId.getName()),
                new TypeReference<Map<String, Short>>() {}
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
        Map<String, Short> entityToId = getEntitiesNamesToIds(recommendation.getStrategy());
        this.matchEntitiesTranslationIds(recommendation.getStrategy(), codeEmbeddings);

        HashMap<String, Object> matrix = new HashMap<>();
        this.computeClassesVectors(matrix, codeEmbeddings, entityToId);

        JSONObject matrixJSON = new JSONObject(matrix);

        Set<String> similarityMatrices = new HashSet<>();
        similarityMatrices.add(getName());
        gridFsService.saveFile(new ByteArrayInputStream(matrixJSON.toString().getBytes()), getName());

        return similarityMatrices;
    }

    @Override
    public String toString() {
        return "SimilarityScipyClassVectorization";
    }

}
