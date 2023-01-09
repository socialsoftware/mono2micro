package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixFunctionalityVectorizationByCallGraph;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public class SimilarityScipyFunctionalityVectorizationByCallGraph extends SimilarityScipy {

    public static final String SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH = "SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH";

    // Used during Similarity Generation
    private int depth;

    public SimilarityScipyFunctionalityVectorizationByCallGraph() {}

    public SimilarityScipyFunctionalityVectorizationByCallGraph(SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto dto) {
        super(dto.getLinkageType());
        this.similarityMatrix = new SimilarityMatrixFunctionalityVectorizationByCallGraph(dto.getWeightsList(), dto.getDepth());
        this.depth = dto.getDepth();
    }

    public SimilarityScipyFunctionalityVectorizationByCallGraph(RecommendMatrixSciPy recommendation) {
        super(recommendation.getLinkageType());
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String getType() {
        return SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;
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
