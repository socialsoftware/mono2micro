import {StrategyType} from "../strategy/Strategy";
import Recommendation from "./Recommendation";
import RecommendAccessesSciPy from "./RecommendAccessesSciPy";

export abstract class RecommendationFactory {
    static getRecommendation(recommendation: any) : Recommendation {
        switch (recommendation.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new RecommendAccessesSciPy(recommendation);
            default:
                throw new Error('Type ' + recommendation.type + ' unknown.');
        }
    }
}
