import Recommendation from "./Recommendation";

const RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH = "RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH";
export {RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH};

export default class RecommendMatrixFunctionalityVectorizationByCallGraph extends Recommendation {
    linkageType: string[];

    constructor(recommendation: any) {
        super(recommendation);
        this.linkageType =              recommendation.linkageType          ||     "average";
    }
}