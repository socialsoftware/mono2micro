import AccessesCollector from "./AccessesCollector";
import Collector, {CollectorType} from "./Collector";

export abstract class CollectorFactory {
    static getCollector(collector: any) : Collector {
        switch (collector.type) {
            case CollectorType.ACCESSES:
                return new AccessesCollector(collector);
            default:
                throw new Error('Type ' + collector.type + ' unknown.');
        }
    }
}
