import {Metric} from "../../type-declarations/types.d";

export default abstract class Decomposition {
    name: string;
    codebaseName: string;
    strategyName: string;
    strategyType: string;
    metrics: Metric[];

    protected constructor(decomposition: any) {
        this.name = decomposition.name;
        this.codebaseName = decomposition.codebaseName;
        this.strategyName = decomposition.strategyName;
        this.strategyType = decomposition.strategyType;
        this.metrics = decomposition.metrics;
    }

    abstract printCard(handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}