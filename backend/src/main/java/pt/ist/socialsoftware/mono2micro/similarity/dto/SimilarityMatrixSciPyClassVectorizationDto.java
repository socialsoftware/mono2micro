package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;

public class SimilarityMatrixSciPyClassVectorizationDto extends SimilarityDto {
    private String linkageType;

    public SimilarityMatrixSciPyClassVectorizationDto() { this.type = SIMILARITY_SCIPY_CLASS_VECTORIZATION; }

    public SimilarityMatrixSciPyClassVectorizationDto(SimilarityScipyClassVectorization similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
    }

    public SimilarityMatrixSciPyClassVectorizationDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.type = SIMILARITY_SCIPY_CLASS_VECTORIZATION;
        this.linkageType = recommend.getLinkageType();
    }


    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

}
