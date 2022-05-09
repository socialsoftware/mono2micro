export default abstract class Strategy {
    type!: string;
    codebaseName!: string;
    name?: string;
    decompositionsNames?: string[];

    protected constructor(type: string, codebaseName: string, name: string, decompositionsNames: string[]) {
        this.type = type;
        this.codebaseName = codebaseName;
        this.name = name;
        this.decompositionsNames = decompositionsNames;
    }

    // Required to validate submit button
    abstract readyToSubmit(): boolean;

    // Required for creating a new copy when updating the state
    abstract copy(): Strategy;

    // This function is used to display the strategy
    abstract printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element;
}


export enum StrategyType {
    RECOMMENDATION_ACCESSES_SCIPY = 'RECOMMENDATION_ACCESSES_SCIPY', // Faster method of creating decompositions based on codebase accesses and SciPy clustering algorithm
    ACCESSES_SCIPY = 'ACCESSES_SCIPY',
}

export enum StrategyDescription {
    ACCESSES_SCIPY = 'Accesses-Based Similarity and SciPy Clustering Algorithm',
    RECOMMENDATION_ACCESSES_SCIPY = 'Accesses-Based Similarity and SciPy Clustering Algorithm',
}
