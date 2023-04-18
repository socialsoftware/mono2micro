import Weights from "./Weights";

const CLASS_VECTORIZATION_WEIGHTS = 'CLASS_VECTORIZATION_WEIGHTS';
export {CLASS_VECTORIZATION_WEIGHTS};

export default class ClassVectorizationWeights extends Weights {

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 0;
        this.type = CLASS_VECTORIZATION_WEIGHTS;
        this.weightsLabel = {}
    }
}