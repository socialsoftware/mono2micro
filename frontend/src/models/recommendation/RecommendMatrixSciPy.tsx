import React from "react";
import {TraceType} from "../../type-declarations/types.d";
import Recommendation from "./Recommendation";

const RECOMMEND_MATRIX_SCIPY = "RECOMMEND_MATRIX_SCIPY";
export {RECOMMEND_MATRIX_SCIPY};

export default class RecommendMatrixSciPy extends Recommendation {
    profile: string;
    linkageType: string[];
    traceType: TraceType[];
    tracesMaxLimit: number;
    weightsList: any[];

    constructor(recommendation: any) {
        super(recommendation);
        this.profile =                  recommendation.profile              ||     "Generic";
        this.tracesMaxLimit =           recommendation.tracesMaxLimit       ||     0;
        this.traceType =                recommendation.traceType            ||     TraceType.ALL;
        this.linkageType =              recommendation.linkageType          ||     "average";
        this.weightsList =              recommendation.weightsList;
    }
}