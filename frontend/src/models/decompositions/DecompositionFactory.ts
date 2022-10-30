import Decomposition from "./Decomposition";
import PartitionsDecomposition, {PARTITIONS_DECOMPOSITION} from "./PartitionsDecomposition";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.type) {
            case PARTITIONS_DECOMPOSITION:
                return new PartitionsDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}