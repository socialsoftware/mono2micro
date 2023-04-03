package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.List;

public class SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES = "SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES";

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses() {}

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto dto) {
        super(dto.getLinkageType());
        this.similarityMatrix = new SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses(dto.getWeightsList());
    }

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(RecommendMatrixSciPy recommendation) {
        super(recommendation.getLinkageType());
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
    }

    @Override
    public boolean equalsDto(SimilarityDto dto) {
        if (!(dto instanceof SimilarityMatrixSciPyDto))
            return false;

        SimilarityMatrixSciPyDto similarityDto = (SimilarityMatrixSciPyDto) dto;
        return similarityDto.getStrategyName().equals(this.getStrategy().getName()) &&
                similarityDto.getLinkageType().equals(this.linkageType) &&
                equalWeights(similarityDto.getWeightsList());
    }

    private boolean equalWeights(List<Weights> weightsList) {
        for (int i=0; i < weightsList.size(); i++) {
            if (!weightsList.get(i).equals(this.getSimilarityMatrix().getWeightsList().get(i)) ) {
                return false;
            }
        }

        return true;
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
