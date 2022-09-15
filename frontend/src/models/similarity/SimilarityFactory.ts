import Similarity from "./Similarity";
import AccessesSciPySimilarity from "./AccessesSciPySimilarity";
import { StrategyType } from "../strategy/Strategy";

export abstract class SimilarityFactory {
    static getSimilarity(similarity: any) : Similarity {
        switch (similarity.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPySimilarity(similarity);
            default:
                throw new Error('Type ' + similarity.type + ' unknown.');
        }
    }
}
