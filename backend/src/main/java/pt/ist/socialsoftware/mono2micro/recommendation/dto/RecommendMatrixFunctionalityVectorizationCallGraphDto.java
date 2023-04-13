package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

public class RecommendMatrixFunctionalityVectorizationCallGraphDto extends RecommendMatrixSciPyDto {

    public RecommendMatrixFunctionalityVectorizationCallGraphDto() {
        this.type = RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

        this.setProfile("Generic");
        this.setTracesMaxLimit(0);
        this.setTraceType(Constants.TraceType.ALL);
    }

    public RecommendMatrixFunctionalityVectorizationCallGraphDto(RecommendMatrixSciPy recommendation) {
        this();
        this.setStrategyName(recommendation.getStrategy().getName());
        this.name = recommendation.getName();
        this.setLinkageType(recommendation.getLinkageType());
        this.setWeightsList(recommendation.getWeightsList());
        this.isCompleted = recommendation.isCompleted();
    }
}
