import Decomposition from "./Decomposition";
import AccessesDecomposition, {ACCESSES_DECOMPOSITION} from "./AccessesDecomposition";
import RepositoryDecomposition, {REPOSITORY_DECOMPOSITION} from "./RepositoryDecomposition";
import AccAndRepoDecomposition, {ACC_AND_REPO_DECOMPOSITION} from "./AccAndRepoDecomposition";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.type) {
            case ACCESSES_DECOMPOSITION:
                return new AccessesDecomposition(decomposition);
            case REPOSITORY_DECOMPOSITION:
                return new RepositoryDecomposition(decomposition);
            case ACC_AND_REPO_DECOMPOSITION:
                return new AccAndRepoDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}