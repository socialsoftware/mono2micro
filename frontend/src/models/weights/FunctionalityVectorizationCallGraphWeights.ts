import Weights from "./Weights";

const FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS = 'FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS';
export {FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS};

export default class FunctionalityVectorizationCallGraphWeights extends Weights {
    controllersWeight?: number;
    servicesWeight?: number;
    intermediateMethodsWeight?: number;
    entitiesWeight?: number;

    public constructor(weights: any) {
        super(weights);
        this.numberOfWeights = 4;
        this.type = FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS;
        this.weightsLabel = {
            controllersWeight: "Controllers Weight",
            servicesWeight: "Services Weight",
            intermediateMethodsWeight: "Intermediate Methods Weight",
            entitiesWeight: "Entities Weight"
        }

        this.controllersWeight = weights.controllersWeight;
        this.servicesWeight = weights.servicesWeight;
        this.intermediateMethodsWeight = weights.intermediateMethodsWeight;
        this.entitiesWeight = weights.entitiesWeight;
    }
}