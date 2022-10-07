import {RepresentationType} from "../representation/Representation";

export default abstract class Strategy {
    name!: string;
    type!: string;
    codebaseName!: string;
    representationTypes!: string[]
    hasSimilarities!: boolean;

    protected constructor(strategy: any) {
        this.name = strategy.name;
        this.type = strategy.type;
        this.codebaseName = strategy.codebaseName;
        this.representationTypes = StrategyRepresentations[this.type];
    }

    // This function is used to be displayed in the context of the codebase
    abstract printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element;
}


export enum StrategyType {
    ACCESSES_SCIPY = 'Accesses SciPy',
    REPOSITORY_SCIPY = 'Repository SciPy',
    ACC_AND_REPO_SCIPY = 'Accesses and Repository SciPy',
}

export enum StrategyDescription {
    'Accesses SciPy' = 'Accesses-Based Similarity and SciPy Clustering Algorithm',
    'Repository SciPy' = 'Repository-Based Similarity and SciPy Clustering Algorithm',
    'Accesses and Repository SciPy' = 'Accesses and Repository-Based Similarity and SciPy Clustering Algorithm',
}

export const StrategyRepresentations: Record<string, RepresentationType[]> = {
    'Accesses SciPy' : [RepresentationType.ACCESSES, RepresentationType.IDTOENTITIY],
    'Repository SciPy' : [RepresentationType.ACCESSES, RepresentationType.IDTOENTITIY, RepresentationType.AUTHOR, RepresentationType.COMMIT],
    'Accesses and Repository SciPy' : [RepresentationType.ACCESSES, RepresentationType.IDTOENTITIY, RepresentationType.AUTHOR, RepresentationType.COMMIT],
}