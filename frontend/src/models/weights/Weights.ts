export default abstract class Weights {
    type!: string;
    numberOfWeights?: number;
    weightsLabel?: Record<string,string>;

    protected constructor(weights: any) {
        this.type = weights.type;
    }
}
