import AccessesCollector from "./AccessesCollector";
import Collector, {CollectorType} from "./Collector";

export abstract class CollectorFactory {
    static getCollector(codebaseName: string, name: string) : Collector {
        switch (name) {
            case CollectorType.ACCESSES:
                return new AccessesCollector(name, codebaseName);
            default:
                throw new Error('Type ' + name + ' unknown.');
        }
    }
}
