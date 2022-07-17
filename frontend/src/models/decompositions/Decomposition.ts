import {Metric} from "../../type-declarations/types.d";

export default abstract class Decomposition {
    codebaseName: string;
    strategyName: string;
    name: string;
    strategyType: string;
    metrics: Metric[];

    protected constructor(decomposition: any) {
        this.codebaseName = decomposition.codebaseName;
        this.strategyName = decomposition.strategyName;
        this.name = decomposition.name;
        this.strategyType = decomposition.strategyType;
        this.metrics = decomposition.metrics;
    }

    abstract printCard(handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}