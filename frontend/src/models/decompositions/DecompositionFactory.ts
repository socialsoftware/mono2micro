import Decomposition from "./Decomposition";
import AccessesSciPyDecomposition from "./AccessesSciPyDecomposition";
import {StrategyType} from "../dendrogram/Dendrogram";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.strategyType) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}