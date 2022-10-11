package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityGenerator.SimilarityMatrix;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.AccessesWeights;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("similarity")
public class SimilarityMatrixSciPy extends Similarity implements AccessesSimilarity, Dendrogram, SimilarityForSciPy, SimilarityMatrix {
    public static final String SIMILARITY_MATRIX_SCIPY = "SIMILARITY_MATRIX_SCIPY";

    // Used during Similarity Generation
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    private List<Weights> weightsList;

    // Used in Clustering Algorithm
    private String linkageType;
    private String similarityMatrixName;

    // Image created in the Python services
    private String dendrogramName;
    private String copheneticDistanceName;

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_SIMILARITY);
        add(DENDROGRAM);
        add(SIMILARITY_FOR_SCIPY);
        add(SIMILARITY_MATRIX);
    }};

    public SimilarityMatrixSciPy() {}

    public SimilarityMatrixSciPy(SimilarityMatrixSciPyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.linkageType = dto.getLinkageType();
        this.weightsList = dto.getWeightsList();
        setDecompositions(new ArrayList<>());
    }

    @Override
    public String getType() {
        return SIMILARITY_MATRIX_SCIPY;
    }

    @Override
    public Set<Short> fillElements(GridFsService gridFsService) throws IOException, JSONException {
        Set<Short> elements = new TreeSet<>();
        AccessesRepresentation accesses = (AccessesRepresentation) getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        AccessesWeights.fillDataStructures( // TODO THIS METHOD SHOULD BE REFACTORED TO ONLY OBTAIN ELEMENTS FROM ACCESSES FILE
                elements, new HashMap<>(), new HashMap<>(),
                new FunctionalityTracesIterator(gridFsService.getFile(accesses.getName()), getTracesMaxLimit()),
                accesses.getProfile(getProfile()),
                getTraceType());
        return elements;
    }

    @Override
    public Map<Short, String> getIDToEntityName(GridFsService gridFsService) throws IOException {
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
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

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }
    @Override
    public List<String> getImplementations() {
        return implementationTypes;
    }
    @Override
    public String getLinkageType() {
        return linkageType;
    }
    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }
    @Override
    public String getSimilarityMatrixName() {
        return similarityMatrixName;
    }
    @Override
    public void setSimilarityMatrixName(String similarityMatrixName) {
        this.similarityMatrixName = similarityMatrixName;
    }
    public String getDendrogramName() {
        return dendrogramName;
    }
    public void setDendrogramName(String dendrogramName) {
        this.dendrogramName = dendrogramName;
    }
    @Override
    public String getCopheneticDistanceName() {
        return copheneticDistanceName;
    }
    public void setCopheneticDistanceName(String copheneticDistanceName) {
        this.copheneticDistanceName = copheneticDistanceName;
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
                similarityDto.getLinkageType().equals(this.linkageType) &&
                hasSameWeights(similarityDto.getWeightsList());
    }
}
