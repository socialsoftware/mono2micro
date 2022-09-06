import Dendrogram from "./Dendrogram";
import AccessesSciPyDendrogram from "./AccessesSciPyDendrogram";
import { StrategyType } from "../strategy/Strategy";

export abstract class DendrogramFactory {
    static getDendrogram(dendrogram: any) : Dendrogram {
        switch (dendrogram.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPyDendrogram(dendrogram);
            default:
                throw new Error('Type ' + dendrogram.type + ' unknown.');
        }
    }
}
