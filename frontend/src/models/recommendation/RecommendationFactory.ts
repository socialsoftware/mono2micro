import Recommendation from "./Recommendation";
import RecommendMatrixSciPy, {RECOMMEND_MATRIX_SCIPY} from "./RecommendMatrixSciPy";

export abstract class RecommendationFactory {
    static getRecommendation(recommendation: any) : Recommendation {
        switch (recommendation.type) {
            case RECOMMEND_MATRIX_SCIPY:
                return new RecommendMatrixSciPy(recommendation);
            default:
                throw new Error('Type ' + recommendation.type + ' unknown.');
        }
    }
}
