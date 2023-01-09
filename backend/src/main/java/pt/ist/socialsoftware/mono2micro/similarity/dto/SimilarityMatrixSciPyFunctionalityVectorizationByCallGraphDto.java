package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixFunctionalityVectorizationByCallGraph;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixWeights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

public class SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto extends SimilarityDto {
    private String linkageType;

    private int depth;

    private List<Weights> weightsList;

    public SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto() { this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH; }

    public SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto(SimilarityScipyFunctionalityVectorizationByCallGraph similarity) {
        this.codebaseName = similarity.getStrategy().getCodebase().getName();
        this.strategyName = similarity.getStrategy().getName();
        this.name = similarity.getName();
        this.type = similarity.getType();
        this.linkageType = similarity.getLinkageType();
        this.depth = similarity.getDepth();
        this.weightsList = ((SimilarityMatrixFunctionalityVectorizationByCallGraph) similarity.getSimilarityMatrix()).getWeightsList();
    }

    public SimilarityMatrixSciPyFunctionalityVectorizationByCallGraphDto(RecommendMatrixSciPy recommend, List<Weights> weightsList) {
        this.strategyName = recommend.getStrategy().getName();
        this.type = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;
        this.weightsList = weightsList;
        this.linkageType = recommend.getLinkageType();
        // this.depth = depth;
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
