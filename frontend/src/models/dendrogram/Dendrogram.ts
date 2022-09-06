export default abstract class Dendrogram {
    codebaseName!: string;
    strategyName!: string;
    type!: string;
    name?: string;

    protected constructor(dendrogram: any) {
        this.codebaseName = dendrogram.codebaseName;
        this.strategyName = dendrogram.strategyName;
        this.type = dendrogram.type;
        this.name = dendrogram.name;
    }

    // This function is used to display the decompositions
    abstract printCard(handleDeleteDendrogram: (dendrogram: Dendrogram) => void): JSX.Element;
}

