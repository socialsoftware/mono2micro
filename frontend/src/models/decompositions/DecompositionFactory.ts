import Decomposition from "./Decomposition";
import AccessesSciPyDecomposition from "./AccessesSciPyDecomposition";
import {StrategyType} from "../strategy/Strategy";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}