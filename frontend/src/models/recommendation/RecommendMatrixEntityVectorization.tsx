import Recommendation from "./Recommendation";

const RECOMMEND_MATRIX_ENTITY_VECTORIZATION = "RECOMMEND_MATRIX_ENTITY_VECTORIZATION";
export {RECOMMEND_MATRIX_ENTITY_VECTORIZATION};

export default class RecommendMatrixEntityVectorization extends Recommendation {
    linkageType: string[];

    constructor(recommendation: any) {
        super(recommendation);
        this.linkageType =              recommendation.linkageType          ||     "average";
    }
}