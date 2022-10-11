import {ACCESSES_SCIPY} from "../decompositions/AccessesSciPyDecomposition";
import {REPOSITORY_SCIPY} from "../decompositions/RepositorySciPyDecomposition";
import {ACC_AND_REPO_SCIPY} from "../decompositions/AccAndRepoSciPyDecomposition";
import AccessesWeights, {ACCESSES_WEIGHTS} from "./AccessesWeights";
import Weights from "./Weights";
import RepositoryWeights, {REPOSITORY_WEIGHTS} from "./RepositoryWeights";

export abstract class WeightsFactory {
    static getWeights(weights: any) : Weights {
        switch (weights.type) {
            case ACCESSES_WEIGHTS:
                return new AccessesWeights(weights);
            case REPOSITORY_WEIGHTS:
                return new RepositoryWeights(weights);
            default:
                throw new Error('Type ' + weights.type + ' unknown.');
        }
    }

    static getWeightsList(weightsList: any[]) : Weights[] {
        return weightsList.map(weights => WeightsFactory.getWeights(weights));
    }

    static getWeightListByDecompositionType(decompositionType: string) : Weights[] {
        switch (decompositionType) {
            case ACCESSES_SCIPY:
                return [new AccessesWeights({accessMetricWeight: 25, writeMetricWeight: 25, readMetricWeight: 25, sequenceMetricWeight: 25})];
            case REPOSITORY_SCIPY:
                return [new RepositoryWeights({authorMetricWeight: 50, commitMetricWeight: 50})];
            case ACC_AND_REPO_SCIPY:
                return [
                    new AccessesWeights({ accessMetricWeight: 17, writeMetricWeight: 17, readMetricWeight: 17, sequenceMetricWeight: 17}),
                    new RepositoryWeights({authorMetricWeight: 16, commitMetricWeight: 16})
                ];
            default:
                throw new Error('Type ' + decompositionType + ' unknown.');
        }
    }
}
