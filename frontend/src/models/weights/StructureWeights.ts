import Weights from "./Weights";

const STRUCTURE_WEIGHTS = 'STRUCTURE_WEIGHTS';
export {STRUCTURE_WEIGHTS};

export default class StructureWeights extends Weights {
    oneToOneWeight?: number;
    oneToManyWeight?: number;
    heritageWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 3;
        this.type = STRUCTURE_WEIGHTS;
        this.weightsLabel = {
            oneToOneWeight: "One to One Weight",
            oneToManyWeight: "One to Many Weight",
            heritageWeight: "Inheritance Weight"
        }

        this.oneToOneWeight = weights.oneToOneWeight;
        this.oneToManyWeight = weights.oneToManyWeight;
        this.heritageWeight = weights.heritageWeight;
    }
}