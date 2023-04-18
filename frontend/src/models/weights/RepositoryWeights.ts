import Weights from "./Weights";

const REPOSITORY_WEIGHTS = 'REPOSITORY_WEIGHTS';
export {REPOSITORY_WEIGHTS};

export default class RepositoryWeights extends Weights {
    authorMetricWeight?: number;
    commitMetricWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.type = REPOSITORY_WEIGHTS;
        this.numberOfWeights = 2;
        this.weightsLabel = {
            authorMetricWeight: "Author Weight",
            commitMetricWeight: "Commit Weight"
        }

        this.authorMetricWeight = weights.authorMetricWeight;
        this.commitMetricWeight = weights.commitMetricWeight;
    }
}
