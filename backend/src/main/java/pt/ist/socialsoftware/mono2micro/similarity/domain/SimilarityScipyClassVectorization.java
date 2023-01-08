package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixEntityVectorization;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyClassVectorizationDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public class SimilarityScipyClassVectorization extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_CLASS_VECTORIZATION = "SIMILARITY_SCIPY_CLASS_VECTORIZATION";

    public SimilarityScipyClassVectorization() {}

    public SimilarityScipyClassVectorization(SimilarityMatrixSciPyClassVectorizationDto dto) {
        super(dto.getLinkageType());
        this.similarityMatrix = new SimilarityMatrixEntityVectorization();
    }

    public SimilarityScipyClassVectorization(RecommendMatrixSciPy recommendation) {
        super(recommendation.getLinkageType());
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_CLASS_VECTORIZATION;
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
