export default abstract class Representation {
    name!: string;
    type!: string;
    codebaseName!: string;
    inputFile!: string;

    protected constructor(representation: Representation) {
        this.name = representation.name;
        this.type = representation.type;
        this.codebaseName = representation.codebaseName;
        this.inputFile = representation.inputFile;
    }

    // This function is used to be displayed in the context of the codebase
    abstract printCard(handleDeleteRepresentation: (representation: Representation) => void): JSX.Element;
}

export enum RepresentationFile {
    ACCESSES = 'Accesses',
    IDTOENTITIY = 'IDToEntity',
    AUTHOR = 'Changes Authorship',
    COMMIT = 'File Changes',
    ENTITY_TO_ID = 'EntityToID',
    CODE_EMBEDDINGS = 'Code Embeddings'
}

export enum RepresentationType {
    ACCESSES_TYPE = 'Accesses Based',
    REPOSITORY_TYPE = 'Repository Based',
    CODE_EMBEDDINGS_TYPE = 'Code Embeddings Based'
}