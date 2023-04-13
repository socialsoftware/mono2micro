import Recommendation from "./Recommendation";

const RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES = "RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES";
export {RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES};

export default class RecommendMatrixFunctionalityVectorizationBySequenceOfAccesses extends Recommendation {
    linkageType: string[];

    constructor(recommendation: any) {
        super(recommendation);
        this.linkageType =              recommendation.linkageType          ||     "average";
    }
}