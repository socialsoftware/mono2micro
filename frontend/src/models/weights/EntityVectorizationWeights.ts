import Weights from "./Weights";

const ENTITY_VECTORIZATION_WEIGHTS = 'ENTITY_VECTORIZATION_WEIGHTS';
export {ENTITY_VECTORIZATION_WEIGHTS};

export default class EntityVectorizationWeights extends Weights {

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 0;
        this.type = ENTITY_VECTORIZATION_WEIGHTS;
        this.weightsLabel = {}
    }
}