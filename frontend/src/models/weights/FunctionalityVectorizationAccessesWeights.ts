import Weights from "./Weights";

const FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS = 'FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS';
export {FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS};

export default class FunctionalityVectorizationAccessesWeights extends Weights {
    readMetricWeight?: number;
    writeMetricWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 4;
        this.type = FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS;
        this.weightsLabel = {
            readMetricWeight: "Read Metric Weight",
            writeMetricWeight: "Write Metric Weight"
        }

        this.readMetricWeight = weights.readMetricWeight;
        this.writeMetricWeight = weights.writeMetricWeight;
    }
}