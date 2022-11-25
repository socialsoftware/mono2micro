import Representation, {RepresentationType} from "./Representation";
import AccessesRepresentation from "./files/AccessesRepresentation";
import IDToEntityRepresentation from "./files/IDToEntityRepresentation";
import AuthorRepresentation from "./files/AuthorRepresentation";
import CommitRepresentation from "./files/CommitRepresentation";
import EntityToIDRepresentation from "./files/EntityToIDRepresentation";
import CodeEmbeddingsRepresentation from "./files/CodeEmbeddingsRepresentation";

export abstract class RepresentationFactory {
    static getRepresentation(representation: any) : Representation {
        switch (representation.type) {
            case RepresentationType.ACCESSES:
                return new AccessesRepresentation(representation);
            case RepresentationType.IDTOENTITIY:
                return new IDToEntityRepresentation(representation);
            case RepresentationType.AUTHOR:
                return new AuthorRepresentation(representation);
            case RepresentationType.COMMIT:
                return new CommitRepresentation(representation);
            case RepresentationType.ENTITY_TO_ID:
                return new EntityToIDRepresentation(representation);
            case RepresentationType.CODE_EMBEDDINGS:
                return new CodeEmbeddingsRepresentation(representation);
            default:
                throw new Error('Type ' + representation.type + ' unknown.');
        }
    }

    static getRepresentations(representation: any[]) : Representation[] | null {
        if (representation === null)
            return null;
        return representation.map((representation:any) => this.getRepresentation(representation))
    }
}