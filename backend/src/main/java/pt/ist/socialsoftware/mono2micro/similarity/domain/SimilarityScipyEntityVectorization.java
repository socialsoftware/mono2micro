package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixEntityVectorization;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyEntityVectorizationDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public class SimilarityScipyEntityVectorization extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_ENTITY_VECTORIZATION = "SIMILARITY_SCIPY_ENTITY_VECTORIZATION";

    public SimilarityScipyEntityVectorization() {}

    public SimilarityScipyEntityVectorization(SimilarityMatrixSciPyEntityVectorizationDto dto) {
        super(dto.getLinkageType());
        this.similarityMatrix = new SimilarityMatrixEntityVectorization();
    }

    public SimilarityScipyEntityVectorization(RecommendMatrixSciPy recommendation) {
        super(recommendation.getLinkageType());
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_ENTITY_VECTORIZATION;
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityMatrixSciPyDto))
            return false;

        SimilarityMatrixSciPyDto similarityDto = (SimilarityMatrixSciPyDto) dto;
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
}
