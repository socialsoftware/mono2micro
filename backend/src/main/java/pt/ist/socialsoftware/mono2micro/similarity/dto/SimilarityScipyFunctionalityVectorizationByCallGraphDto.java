package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

public class SimilarityScipyFunctionalityVectorizationByCallGraphDto extends SimilarityDto {
    private String linkageType;

    private int depth;

    private List<Weights> weightsList;

    public SimilarityScipyFunctionalityVectorizationByCallGraphDto() { this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH; }

    public SimilarityScipyFunctionalityVectorizationByCallGraphDto(SimilarityScipyFunctionalityVectorizationByCallGraph similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
        this.weightsList = similarity.getWeightsList();
        this.depth = similarity.getDepth();
    }

    public SimilarityScipyFunctionalityVectorizationByCallGraphDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.name = recommend.getName();
        this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;
        this.linkageType = recommend.getLinkageType();
        this.weightsList = weightsList;
        // this.depth = depth;
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.strategyName + " "
                    + "params" + "(" + this.linkageType + "," + this.depth + ") "
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

}
