package pt.ist.socialsoftware.mono2micro.recommendation.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixClassVectorization;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixClassVectorization.RECOMMEND_MATRIX_CLASS_VECTORIZATION;

public class RecommendMatrixClassVectorizationDto extends RecommendationDto {

    private String linkageType;

    public RecommendMatrixClassVectorizationDto() {this.type = RECOMMEND_MATRIX_CLASS_VECTORIZATION;}

    public RecommendMatrixClassVectorizationDto(RecommendMatrixClassVectorization recommendation) {
        this.type = RECOMMEND_MATRIX_CLASS_VECTORIZATION;
        this.setStrategyName(recommendation.getStrategy().getName());
        this.name = recommendation.getName();
        this.linkageType = recommendation.getLinkageType();
        this.isCompleted = recommendation.isCompleted();
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

}
