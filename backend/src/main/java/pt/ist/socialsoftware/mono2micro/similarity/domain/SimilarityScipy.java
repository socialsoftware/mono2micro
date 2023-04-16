package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("similarity")
public abstract class SimilarityScipy extends Similarity {
    // Used in Clustering Algorithm
    String linkageType;
    private List<Weights> weightsList;
    // Dendrogram created in the Python services
    Dendrogram dendrogram;

    public SimilarityScipy() {}

    public SimilarityScipy(Strategy strategy, String name, String linkageType, List<Weights> weightsList) {
        super(strategy, name);
        this.linkageType = linkageType;
        this.weightsList = weightsList;
    }

    public SimilarityScipy(Strategy strategy, String name, RecommendMatrixSciPy recommendation) {
        super(strategy, name);
        this.linkageType = recommendation.getLinkageType();
        this.weightsList = recommendation.getWeightsList();
    }

    @Override
    public Clustering getClustering() {
        return new SciPyClustering();
    }

    @Override
    public Map<Short, String> getIDToEntityName() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
    }

    public Map<String, Short> getEntityToId() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        EntityToIDRepresentation entityToId = (EntityToIDRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ENTITY_TO_ID);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(entityToId.getName()), new TypeReference<Map<String, Short>>() {});
    }

    public void matchEntitiesTranslationIds(JSONObject codeEmbeddings)
        throws JSONException, IOException
    {
        Map<String, Short> translationEntityToIdJson = getEntityToId();
        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                String className = cls.getString("name");

                if (translationEntityToIdJson.containsKey(className)) {
                    int entityId = translationEntityToIdJson.get(className);
                    cls.put("type", "Entity");
                    cls.put("translationID", entityId);
                }
            }
        }
    }

    public JSONObject getCodeEmbeddings(Strategy strategy) throws IOException, JSONException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        CodeEmbeddingsRepresentation codeEmbeddingsRepresentation = (CodeEmbeddingsRepresentation) strategy.getCodebase().getRepresentationByFileType(CODE_EMBEDDINGS);
        JSONObject codeEmbeddings = new JSONObject(
                gridFsService.getFileAsString(codeEmbeddingsRepresentation.getName())
        );

        this.matchEntitiesTranslationIds(codeEmbeddings);
        return codeEmbeddings;
    }

    @Override
    public void generate() throws Exception {
        // 1 - Generate Matrix
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        generate(gridFsService, this);

        // 2 - Generate Dendrogram
        this.dendrogram = new Dendrogram(getName(), getName(), getLinkageType());
    }

    @Override
    public void removeProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        gridFsService.deleteFile(dendrogram.getDendrogramName());
        gridFsService.deleteFile(dendrogram.getCopheneticDistanceName());
    }

    public List<Weights> getWeightsList() {
        return weightsList;
    }
    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public String getLinkageType() {
        return linkageType;
    }
    public Dendrogram getDendrogram() {
        return dendrogram;
    }
    public void setDendrogram(Dendrogram dendrogram) {
        this.dendrogram = dendrogram;
    }

    public abstract String getProfile();
    public abstract int getTracesMaxLimit();
    public abstract Constants.TraceType getTraceType();

    public abstract void generate(GridFsService gridFsService, Similarity similarity) throws Exception;

    public abstract Set<String> generateMultipleMatrices(GridFsService gridFsService, Recommendation recommendation, Set<Short> elements, int totalNumberOfWeights) throws Exception;
}
