package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;

public class SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto extends SimilarityDto {
    private String linkageType;

    private List<Weights> weightsList;

    public SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto() { this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES; }

    public SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto(SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
        this.weightsList = ((SimilarityMatrixFunctionalityVectorizationBySequenceOfAccesses) similarity.getSimilarityMatrix()).getWeightsList();
    }

    public SimilarityMatrixSciPyFunctionalityVectorizationBySequenceOfAccessesDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
        this.weightsList = weightsList;
        this.linkageType = recommend.getLinkageType();
    }


    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

}
