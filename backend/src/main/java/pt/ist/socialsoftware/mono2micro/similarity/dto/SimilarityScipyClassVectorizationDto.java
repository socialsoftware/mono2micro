package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;

public class SimilarityScipyClassVectorizationDto extends SimilarityDto {
    private String linkageType;

    public SimilarityScipyClassVectorizationDto() { this.type = SIMILARITY_SCIPY_CLASS_VECTORIZATION; }

    public SimilarityScipyClassVectorizationDto(SimilarityScipyClassVectorization similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
    }

    public SimilarityScipyClassVectorizationDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.name = recommend.getName();
        this.type = SIMILARITY_SCIPY_CLASS_VECTORIZATION;
        this.linkageType = recommend.getLinkageType();
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.strategyName + " " + "params" + "(" + this.linkageType + ")";
        }

        return this.name;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

}
