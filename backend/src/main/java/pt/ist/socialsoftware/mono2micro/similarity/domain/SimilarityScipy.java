package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.Matrix;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("similarity")
public abstract class SimilarityScipy extends Similarity {

    // Used in Clustering Algorithm
    String linkageType;
    Matrix similarityMatrix;

    // Dendrogram created in the Python services
    Dendrogram dendrogram;

    public SimilarityScipy() {}

    public SimilarityScipy(String linkageType) {
        this.linkageType = linkageType;
        setDecompositions(new ArrayList<>());
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

    @Override
    public void generate() throws Exception {
        // 1 - Generate Matrix
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        this.similarityMatrix.generate(gridFsService, this);

        // 2 - Generate Dendrogram
        this.dendrogram = new Dendrogram(getName(), similarityMatrix.getName(), getLinkageType());
    }

    @Override
    public void removeProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        gridFsService.deleteFile(similarityMatrix.getName());
        gridFsService.deleteFile(dendrogram.getDendrogramName());
        gridFsService.deleteFile(dendrogram.getCopheneticDistanceName());
    }

    public String getLinkageType() {
        return linkageType;
    }
    public Matrix getSimilarityMatrix() {
        return similarityMatrix;
    }
    public Dendrogram getDendrogram() {
        return dendrogram;
    }
    public void setSimilarityMatrix(Matrix similarityMatrix) {
        this.similarityMatrix = similarityMatrix;
    }
    public void setDendrogram(Dendrogram dendrogram) {
        this.dendrogram = dendrogram;
    }

    public abstract String getProfile();
    public abstract int getTracesMaxLimit();
    public abstract Constants.TraceType getTraceType();
}
