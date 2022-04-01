
export default abstract class Decomposition {
    name: string;
    codebaseName: string;
    strategyName: string;
    strategyType: string;

    protected constructor(name: string, codebaseName: string, strategyName: string, strategyType: string) {
        this.name = name;
        this.codebaseName = codebaseName;
        this.strategyName = strategyName;
        this.strategyType = strategyType;
    }

    abstract printCard(handleDeleteDecomposition: (collector: string) => void): JSX.Element;
}