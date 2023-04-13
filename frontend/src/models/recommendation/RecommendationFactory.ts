import Recommendation from "./Recommendation";
import RecommendMatrixClassVectorization, { RECOMMEND_MATRIX_CLASS_VECTORIZATION } from "./RecommendMatrixClassVectorization";
import RecommendMatrixEntityVectorization, { RECOMMEND_MATRIX_ENTITY_VECTORIZATION } from "./RecommendMatrixEntityVectorization";
import RecommendMatrixFunctionalityVectorizationByCallGraph, { RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH } from "./RecommendMatrixFunctionalityVectorizationByCallGraph";
import RecommendMatrixFunctionalityVectorizationBySequenceOfAccesses, { RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES } from "./RecommendMatrixFunctionalityVectorizationBySequenceOfAccesses";
import RecommendMatrixSciPy, {RECOMMEND_MATRIX_SCIPY} from "./RecommendMatrixSciPy";

export abstract class RecommendationFactory {
    static getRecommendation(recommendation: any) : Recommendation {
        switch (recommendation.type) {
            case RECOMMEND_MATRIX_SCIPY:
                return new RecommendMatrixSciPy(recommendation);
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
                return new RecommendMatrixClassVectorization(recommendation);
            case RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
                return new RecommendMatrixEntityVectorization(recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new RecommendMatrixFunctionalityVectorizationByCallGraph(recommendation);
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new RecommendMatrixFunctionalityVectorizationBySequenceOfAccesses(recommendation);
            default:
                throw new Error('Type ' + recommendation.type + ' unknown.');
        }
    }
}
