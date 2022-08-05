export default abstract class Decomposition {
    codebaseName: string;
    strategyName: string;
    name: string;
    type: string;
    metrics: Record<string, any>;

    protected constructor(decomposition: any) {
        this.codebaseName = decomposition.codebaseName;
        this.strategyName = decomposition.strategyName;
        this.name = decomposition.name;
        this.type = decomposition.type;
        this.metrics = decomposition.metrics;
    }

    abstract printCard(reloadDecompositions: () => void, handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}