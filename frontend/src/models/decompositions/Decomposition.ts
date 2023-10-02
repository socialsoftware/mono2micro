export default abstract class Decomposition {
    codebaseName: string;
    strategyName: string;
    similarityName: string;
    name: string;
    type: string;
    expert: boolean;
    clusters: any;
    metrics: Record<string, any>;
    representationInformations: string[];

    protected constructor(decomposition: any) {
        this.codebaseName = decomposition.codebaseName;
        this.strategyName = decomposition.strategyName;
        this.similarityName = decomposition.similarityName;
        this.name = decomposition.name;
        this.type = decomposition.type;
        this.expert = decomposition.expert;
        this.clusters = decomposition.clusters;
        this.metrics = decomposition.metrics;
        this.representationInformations = decomposition.representationInformations;
    }

    abstract printCard(
        reloadDecompositions: () => void,
        handleDeleteDecomposition: (collector: string) => void,
        handleExportDecomposition: (collector: string) => void
    ): JSX.Element;
}