import {Metric} from "../../type-declarations/types.d";

export default abstract class Decomposition {
    name: string;
    strategyName: string;
    strategyType: string;
    metrics: Metric[];

    protected constructor(decomposition: any) {
        this.name = decomposition.name;
        this.strategyName = decomposition.strategyName;
        this.strategyType = decomposition.strategyType;
        this.metrics = decomposition.metrics;
    }

    abstract printCard(handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}