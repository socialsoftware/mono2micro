package pt.ist.socialsoftware.mono2micro.similarity.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixWeights;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class SimilarityScipyWeights extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_WEIGHTS = "SIMILARITY_SCIPY_WEIGHTS";

    // Used during Similarity Generation
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;

    public SimilarityScipyWeights() {}

    public SimilarityScipyWeights(SimilarityMatrixSciPyDto dto) {
        super(dto.getLinkageType());
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.similarityMatrix = new SimilarityMatrixWeights(dto.getWeightsList());
    }

    public SimilarityScipyWeights(RecommendMatrixSciPy recommendation) {
        super(recommendation.getLinkageType());
        this.profile = recommendation.getProfile();
        this.tracesMaxLimit = recommendation.getTracesMaxLimit();
        this.traceType = recommendation.getTraceType();
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_WEIGHTS;
    }

    @Override
    public Map<Short, String> getIDToEntityName() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
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
                similarityDto.getLinkageType().equals(this.linkageType);
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
}
