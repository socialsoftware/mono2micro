import Representation, {RepresentationType} from "./Representation";
import AccessesRepresentation from "./AccessesRepresentation";
import IDToEntityRepresentation from "./IDToEntityRepresentation";

export abstract class RepresentationFactory {
    static getRepresentation(representation: any) : Representation {
        switch (representation.type) {
            case RepresentationType.ACCESSES:
                return new AccessesRepresentation(representation);
            case RepresentationType.IDTOENTITIY:
                return new IDToEntityRepresentation(representation);
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