import Weights from "./Weights";

const STRUCTURE_WEIGHTS = 'STRUCTURE_WEIGHTS';
export {STRUCTURE_WEIGHTS};

export default class StructureWeights extends Weights {
    oneToOneWeight?: number;
    oneToManyWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 2;
        this.type = STRUCTURE_WEIGHTS;
        this.weightsLabel = {
            oneToOneWeight: "One to One Weight",
            oneToManyWeight: "One to Many Weight"
        }

        this.oneToOneWeight = weights.oneToOneWeight;
        this.oneToManyWeight = weights.oneToManyWeight;
    }
}