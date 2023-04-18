import Weights from "./Weights";

const ACCESSES_WEIGHTS = 'ACCESSES_WEIGHTS';
export {ACCESSES_WEIGHTS};

export default class AccessesWeights extends Weights {
    accessMetricWeight?: number;
    writeMetricWeight?: number;
    readMetricWeight?: number;
    sequenceMetricWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 4;
        this.type = ACCESSES_WEIGHTS;
        this.weightsLabel = {
            accessMetricWeight: "Access Weight",
            writeMetricWeight: "Write Weight",
            readMetricWeight: "Read Weight",
            sequenceMetricWeight: "Sequence Weight"
        }

        this.accessMetricWeight = weights.accessMetricWeight;
        this.writeMetricWeight = weights.writeMetricWeight;
        this.readMetricWeight = weights.readMetricWeight;
        this.sequenceMetricWeight = weights.sequenceMetricWeight;
    }
}