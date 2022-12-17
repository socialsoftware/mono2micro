package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("similarity")
public class SimilarityMatrixSciPyXYZ extends Similarity {
    public static final String SIMILARITY_MATRIX_SCIPY = "SIMILARITY_MATRIX_SCIPY";

    // Used during Similarity Generation
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private int depth;

    // Used in Clustering Algorithm
    private String linkageType;
    private SimilarityMatrix similarityMatrix;

    // Dendrogram created in the Python services
    private Dendrogram dendrogram;

    public SimilarityMatrixSciPyXYZ() {}

    public SimilarityMatrixSciPyXYZ(SimilarityMatrixSciPyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.depth = dto.getDepth();
        this.linkageType = dto.getLinkageType();
        this.similarityMatrix = new SimilarityMatrix(dto.getWeightsList());
        setDecompositions(new ArrayList<>());
    }

    public SimilarityMatrixSciPyXYZ(RecommendMatrixSciPy recommendation) {
        this.profile = recommendation.getProfile();
        this.tracesMaxLimit = recommendation.getTracesMaxLimit();
        this.traceType = recommendation.getTraceType();
        this.linkageType = recommendation.getLinkageType();
    }

    @Override
    public String getType() {
        return SIMILARITY_MATRIX_SCIPY;
    }

    @Override
    public Clustering getClustering() {
        return new SciPyClustering();
    }

    public Set<Short> fillElements(GridFsService gridFsService) throws IOException {
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = new ObjectMapper().readValue(
                gridFsService.getFileAsString(idToEntityRepresentation.getName()),
                new TypeReference<Map<Short, String>>() {}
        );
        return new TreeSet<>(idToEntity.keySet());
    }

    @Override
    public Map<Short, String> getIDToEntityName() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
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

    public SimilarityMatrix getSimilarityMatrix() {
        return similarityMatrix;
    }

    public void setSimilarityMatrix(SimilarityMatrix similarityMatrix) {
        this.similarityMatrix = similarityMatrix;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public Dendrogram getDendrogram() {
        return dendrogram;
    }

    public void setDendrogram(Dendrogram dendrogram) {
        this.dendrogram = dendrogram;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public void generate() throws Exception {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        Set<Short> elements = fillElements(gridFsService);
        this.similarityMatrix.generate(gridFsService, this);

        this.dendrogram = new Dendrogram(getName(), similarityMatrix.getName(), getLinkageType());
    }

    @Override
    public void removeProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        gridFsService.deleteFile(similarityMatrix.getName());
        gridFsService.deleteFile(dendrogram.getDendrogramName());
        gridFsService.deleteFile(dendrogram.getCopheneticDistanceName());
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityMatrixSciPyDto))
            return false;

        SimilarityMatrixSciPyDto similarityDto = (SimilarityMatrixSciPyDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
                similarityDto.getProfile().equals(this.profile) &&
                similarityDto.getTracesMaxLimit() == this.tracesMaxLimit &&
                similarityDto.getTraceType() == this.traceType &&
                similarityDto.getLinkageType().equals(this.linkageType);// &&
                //similarityMatrix.hasSameWeights(similarityDto.getWeightsList());
    }
}
