import Decomposition from "./Decomposition";
import AccessesSciPyDecomposition, {ACCESSES_SCIPY} from "./AccessesSciPyDecomposition";
import RepositorySciPyDecomposition, {REPOSITORY_SCIPY} from "./RepositorySciPyDecomposition";
import AccAndRepoSciPyDecomposition, {ACC_AND_REPO_SCIPY} from "./AccAndRepoSciPyDecomposition";

export abstract class DecompositionFactory {
    static getDecomposition(decomposition: any) : Decomposition {
        switch (decomposition.type) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyDecomposition(decomposition);
            case REPOSITORY_SCIPY:
                return new RepositorySciPyDecomposition(decomposition);
            case ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPyDecomposition(decomposition);
            default:
                throw new Error('Type ' + decomposition.strategyType + ' unknown.');
        }
    }
}