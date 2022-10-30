import AccessesWeights, {ACCESSES_WEIGHTS} from "./AccessesWeights";
import Weights from "./Weights";
import RepositoryWeights, {REPOSITORY_WEIGHTS} from "./RepositoryWeights";
import {RepresentationInfoType} from "../representation/RepresentationInfoTypes";

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

    static getWeightListByRepresentationInfoType(representationTypes: string[]) : Weights[] {

        if (representationTypes.includes(RepresentationInfoType.ACCESSES_INFO) && representationTypes.includes(RepresentationInfoType.REPOSITORY_INFO))
            return [
                new AccessesWeights({ accessMetricWeight: 17, writeMetricWeight: 17, readMetricWeight: 17, sequenceMetricWeight: 17}),
                new RepositoryWeights({authorMetricWeight: 16, commitMetricWeight: 16})
            ];
        else if (representationTypes.includes(RepresentationInfoType.ACCESSES_INFO))
            return [new AccessesWeights({accessMetricWeight: 25, writeMetricWeight: 25, readMetricWeight: 25, sequenceMetricWeight: 25})];
        else if (representationTypes.includes(RepresentationInfoType.REPOSITORY_INFO))
            return [new RepositoryWeights({authorMetricWeight: 50, commitMetricWeight: 50})];
        throw new Error('No known type of Representation Info in Weights Factory.');
    }
}
