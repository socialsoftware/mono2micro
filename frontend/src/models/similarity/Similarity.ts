export default abstract class Similarity {
    codebaseName!: string;
    strategyName!: string;
    type!: string;
    name?: string;

    protected constructor(similarity: any) {
        this.codebaseName = similarity.codebaseName;
        this.strategyName = similarity.strategyName;
        this.type = similarity.type;
        this.name = similarity.name;
    }

    // This function is used to display the decomposition
    abstract printCard(handleDeleteSimilarity: (similarity: Similarity) => void): JSX.Element;
}

