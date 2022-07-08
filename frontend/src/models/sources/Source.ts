export default abstract class Source {
    name!: string;
    type!: string;
    codebaseName!: string;
    inputFile!: string;

    protected constructor(source: Source) {
        this.name = source.name;
        this.type = source.type;
        this.codebaseName = source.codebaseName;
        this.inputFile = source.inputFile;
    }

    // This function is used to display the collector
    abstract printCard(handleDeleteSource: (source: Source) => void): JSX.Element;
}

export enum SourceType {
    ACCESSES = 'Accesses',
    IDTOENTITIY = 'IDToEntity',
}