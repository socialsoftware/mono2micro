import Recommendation from "./Recommendation";
import {RecommendationType} from "./RecommendationTypes";
import RecommendMatrixSciPy from "./RecommendMatrixSciPy";

export abstract class RecommendationFactory {
    static getRecommendation(recommendation: any) : Recommendation {
        switch (recommendation.type) {
            case RecommendationType.RECOMMEND_MATRIX_SCIPY:
            case RecommendationType.RECOMMEND_MATRIX_CLASS_VECTORIZATION:
            case RecommendationType.RECOMMEND_MATRIX_ENTITY_VECTORIZATION:
            case RecommendationType.RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
            case RecommendationType.RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new RecommendMatrixSciPy(recommendation);
            default:
                throw new Error('Type ' + recommendation.type + ' unknown.');
        }
    }
}
