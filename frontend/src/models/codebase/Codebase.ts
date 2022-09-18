import Representation from "../representation/Representation";
import {RepresentationFactory} from "../representation/RepresentationFactory";

export default class Codebase {
    name!: string;
    representations: Representation[] | null;

    public constructor(codebase: any) {
        this.name = codebase.name;
        this.representations = RepresentationFactory.getRepresentations(codebase.representations);
    }
}
