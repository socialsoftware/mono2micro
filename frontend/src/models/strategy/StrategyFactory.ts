import Strategy, { StrategyType } from "../strategy/Strategy";
import AccessesSciPyStrategy from "./AccessesSciPyStrategy";

export abstract class StrategyFactory {
    static getStrategy(strategy: any) : Strategy {
        switch (strategy.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyStrategy(strategy);
            default:
                throw new Error('Type ' + strategy.type + ' unknown.');
        }
    }
}
