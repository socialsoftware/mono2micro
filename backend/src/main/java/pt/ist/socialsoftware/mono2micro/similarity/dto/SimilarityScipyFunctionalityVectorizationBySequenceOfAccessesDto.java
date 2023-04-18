package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;

public class SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto extends SimilarityDto {
    private String linkageType;

    private List<Weights> weightsList;

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto() { this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES; }

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto(SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
        this.weightsList = similarity.getWeightsList();
    }

    public SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.name = recommend.getName();
        this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
        this.linkageType = recommend.getLinkageType();
        this.weightsList = weightsList;
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.strategyName + " "
                    + "params" + "(" + this.linkageType + ") "
                    + this.weightsList.stream()
                    .map(weights -> weights.getName())
                    .collect(Collectors.joining(", "));
        }

        return this.name;
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
