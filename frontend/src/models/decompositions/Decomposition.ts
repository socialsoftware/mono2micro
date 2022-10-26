export default abstract class Decomposition {
    codebaseName: string;
    strategyName: string;
    similarityName: string;
    name: string;
    type: string;
    clusters: any;
    metrics: Record<string, any>;

    protected constructor(decomposition: any) {
        this.codebaseName = decomposition.codebaseName;
        this.strategyName = decomposition.strategyName;
        this.similarityName = decomposition.similarityName;
        this.name = decomposition.name;
        this.type = decomposition.type;
        this.clusters = decomposition.clusters;
        this.metrics = decomposition.metrics;
    }

    abstract printCard(reloadDecompositions: () => void, handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}