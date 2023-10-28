import Weights from "./Weights";

const STRUCTURE_WEIGHTS = 'STRUCTURE_WEIGHTS';
export {STRUCTURE_WEIGHTS};

export default class EntityVectorizationWeights extends Weights {

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 0;
        this.type = STRUCTURE_WEIGHTS;
        this.weightsLabel = {}
    }
}