import {StrategyType} from "../strategy/Strategy";
import Recommendation from "./Recommendation";
import RecommendAccessesSciPy from "./RecommendAccessesSciPy";
import RecommendRepositorySciPy from "./RecommendRepositorySciPy";
import RecommendAccAndRepoSciPy from "./RecommendAccAndRepoSciPy";

export abstract class RecommendationFactory {
    static getRecommendation(recommendation: any) : Recommendation {
        switch (recommendation.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new RecommendAccessesSciPy(recommendation);
            case StrategyType.REPOSITORY_SCIPY:
                return new RecommendRepositorySciPy(recommendation);
            case StrategyType.ACC_AND_REPO_SCIPY:
                return new RecommendAccAndRepoSciPy(recommendation);
            default:
                throw new Error('Type ' + recommendation.type + ' unknown.');
        }
    }
}
