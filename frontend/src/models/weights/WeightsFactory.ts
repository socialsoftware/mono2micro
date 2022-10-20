import {ACCESSES_DECOMPOSITION} from "../decompositions/AccessesDecomposition";
import {REPOSITORY_DECOMPOSITION} from "../decompositions/RepositoryDecomposition";
import {ACC_AND_REPO_DECOMPOSITION} from "../decompositions/AccAndRepoDecomposition";
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
            case ACCESSES_DECOMPOSITION:
                return [new AccessesWeights({accessMetricWeight: 25, writeMetricWeight: 25, readMetricWeight: 25, sequenceMetricWeight: 25})];
            case REPOSITORY_DECOMPOSITION:
                return [new RepositoryWeights({authorMetricWeight: 50, commitMetricWeight: 50})];
            case ACC_AND_REPO_DECOMPOSITION:
                return [
                    new AccessesWeights({ accessMetricWeight: 17, writeMetricWeight: 17, readMetricWeight: 17, sequenceMetricWeight: 17}),
                    new RepositoryWeights({authorMetricWeight: 16, commitMetricWeight: 16})
                ];
            default:
                throw new Error('Type ' + decompositionType + ' unknown.');
        }
    }
}
