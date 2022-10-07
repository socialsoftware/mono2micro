import Decomposition from "./Decomposition";
import AccessesSciPyDecomposition from "./AccessesSciPyDecomposition";
import {StrategyType} from "../strategy/Strategy";
import RepositorySciPyDecomposition from "./RepositorySciPyDecomposition";
import AccAndRepoSciPyDecomposition from "./AccAndRepoSciPyDecomposition";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition(decomposition);
            case StrategyType.REPOSITORY_SCIPY:
                return new RepositorySciPyDecomposition(decomposition);
            case StrategyType.ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}