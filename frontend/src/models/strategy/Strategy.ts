import {SourceType} from "../sources/Source";

export default abstract class Strategy {
    name!: string;
    type!: string;
    codebaseName!: string;
    sourceTypes!: string[]
    hasDendrograms!: boolean;

    protected constructor(strategy: any) {
        this.name = strategy.name;
        this.type = strategy.type;
        this.codebaseName = strategy.codebaseName;
        this.sourceTypes = StrategySources[this.type];
    }

    // This function is used to be displayed in the context of the codebase
    abstract printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element;
}


export enum StrategyType {
    ACCESSES_SCIPY = 'Accesses SciPy',
}

export enum StrategyDescription {
    'Accesses SciPy' = 'Accesses-Based Similarity and SciPy Clustering Algorithm',
}

export const StrategySources: Record<string, SourceType[]> = {
    'Accesses SciPy' : [SourceType.ACCESSES, SourceType.IDTOENTITIY],
}
