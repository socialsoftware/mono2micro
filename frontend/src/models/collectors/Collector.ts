export default abstract class Collector {
    type!: string;
    codebaseName!: string;
    sources!: string[];
    possibleStrategies!: string[];

    protected constructor(type: string, codebaseName: string, sources: string[], possibleStrategies: string[]) {
        this.type = type;
        this.codebaseName = codebaseName;
        this.sources = sources;
        this.possibleStrategies = possibleStrategies;
    }

    // This function is used to display the collector
    abstract printCard(handleDeleteCollector: (collector: Collector) => void): JSX.Element;

    // This function is used to verify if conditions are met to submit
    abstract canSubmit(): boolean;

    // Required for creating a new copy when updating the state
    abstract copy(): Collector;
}

export enum CollectorType {
    ACCESSES = 'Accesses Collector',
    GITHUB = 'GitHub Collector',
}
