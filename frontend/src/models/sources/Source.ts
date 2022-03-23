export default abstract class Source {
    type!: string;
    inputFilePath!: string;
    codebaseName!: string;

    protected constructor(type: string, inputFilePath: string, codebaseName: string) {
        this.type = type;
        this.inputFilePath = inputFilePath;
        this.codebaseName = codebaseName;
    }
}

export enum SourceType {
    ACCESSES = 'Accesses',
    IDTOENTITIY = 'IDToEntity',
}