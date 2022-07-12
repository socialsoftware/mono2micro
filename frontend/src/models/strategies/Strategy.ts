import {SourceType} from "../sources/Source";

export default abstract class Strategy {
    type!: string;
    name?: string;
    codebaseName!:string;
    sourceTypes!: string[]

    protected constructor(strategy: any) {
        this.type = strategy.type;
        this.name = strategy.name;
        this.codebaseName = strategy.codebaseName;
        this.sourceTypes = StrategySources[this.type];
    }

    // Required to validate submit button
    abstract readyToSubmit(): boolean;

    // Required for creating a new copy when updating the state
    abstract copy(): Strategy;

    // This function is used to display the strategy
    abstract printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element;
}


export enum StrategyType {
    ACCESSES_SCIPY = 'Accesses SciPy',
    RECOMMEND_ACCESSES_SCIPY = 'Recommend Accesses SciPy', // Faster method of creating decompositions based on codebase accesses and SciPy clustering algorithm
}

export enum StrategyDescription {
    'Accesses SciPy' = 'Accesses-Based Similarity and SciPy Clustering Algorithm',
    'Recommend Accesses SciPy' = 'Accesses-Based Similarity and SciPy Clustering Algorithm Recommendations',
}

export const StrategySources: Record<string, SourceType[]> = {
    'Accesses SciPy' : [SourceType.ACCESSES, SourceType.IDTOENTITIY],
    'Recommend Accesses SciPy' : [SourceType.ACCESSES, SourceType.IDTOENTITIY],
}