import Strategy, { StrategyType } from "../strategy/Strategy";
import AccessesSciPyStrategy from "./AccessesSciPyStrategy";
import RepositorySciPyStrategy from "./RepositorySciPyStrategy";
import AccAndRepoSciPyStrategy from "./AccAndRepoSciPyStrategy";

export abstract class StrategyFactory {
    static getStrategy(strategy: any) : Strategy {
        switch (strategy.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyStrategy(strategy);
            case StrategyType.REPOSITORY_SCIPY:
                return new RepositorySciPyStrategy(strategy);
            case StrategyType.ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyStrategy(strategy);
            default:
                throw new Error('Type ' + strategy.type + ' unknown.');
        }
    }
}
