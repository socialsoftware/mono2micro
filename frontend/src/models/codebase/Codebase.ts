import Source from "../sources/Source";
import {SourceFactory} from "../sources/SourceFactory";

export default class Codebase {
    name!: string;
    sources: Source[] | null;
    isEmpty: boolean | null;

    public constructor(codebase: any) {
        this.name = codebase.name;
        this.sources = SourceFactory.getSources(codebase.sources);
        this.isEmpty = codebase.empty;
    }
}
