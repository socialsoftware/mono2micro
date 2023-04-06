import Recommendation from "./Recommendation";

const RECOMMEND_MATRIX_CLASS_VECTORIZATION = "RECOMMEND_MATRIX_CLASS_VECTORIZATION";
export {RECOMMEND_MATRIX_CLASS_VECTORIZATION};

export default class RecommendMatrixClassVectorization extends Recommendation {
    linkageType: string[];

    constructor(recommendation: any) {
        super(recommendation);
        this.linkageType =              recommendation.linkageType          ||     "average";
    }
}