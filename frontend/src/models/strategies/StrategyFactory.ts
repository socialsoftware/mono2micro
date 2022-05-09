import Strategy, {StrategyType} from "./Strategy";
import AccessesSciPyStrategy from "./AccessesSciPyStrategy";
import RecommendAccessesSciPyStrategy from "./RecommendAccessesSciPyStrategy";

export abstract class StrategyFactory {
    static getStrategy(strategy: any) : Strategy {
        switch (strategy.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyStrategy(strategy);
            case StrategyType.RECOMMENDATION_ACCESSES_SCIPY:
                return new RecommendAccessesSciPyStrategy(strategy);
            default:
                throw new Error('Type ' + strategy.type + ' unknown.');
        }
    }
}
