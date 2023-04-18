package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;

public class SimilarityScipyFactory {

    public static SimilarityScipy getSimilarityScipy(RecommendMatrixSciPy recommendation) {
        switch (recommendation.getType()) {
            case RECOMMEND_MATRIX_SCIPY:
                return new SimilarityScipyAccessesAndRepository(recommendation);
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
                return new SimilarityScipyClassVectorization(recommendation);
            case RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
                return new SimilarityScipyEntityVectorization(recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new SimilarityScipyFunctionalityVectorizationByCallGraph(recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(recommendation);
            default:
                throw new RuntimeException("The recommendation type " + recommendation.getType() + " is not present.");
        }
    }


}
