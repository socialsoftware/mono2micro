import Similarity from "./Similarity";
import AccessesSciPySimilarity from "./AccessesSciPySimilarity";
import { StrategyType } from "../strategy/Strategy";
import RepositorySciPySimilarity from "./RepositorySciPySimilarity";
import AccAndRepoSciPySimilarity from "./AccAndRepoSciPySimilarity";

export abstract class SimilarityFactory {
    static getSimilarity(similarity: any) : Similarity {
        switch (similarity.type) {
            case StrategyType.ACCESSES_SCIPY:
                return new AccessesSciPySimilarity(similarity);
            case StrategyType.REPOSITORY_SCIPY:
                return new RepositorySciPySimilarity(similarity);
            case StrategyType.ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPySimilarity(similarity);
            default:
                throw new Error('Type ' + similarity.type + ' unknown.');
        }
    }
}
