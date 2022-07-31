import React from "react";
import {TraceType} from "../../type-declarations/types";
import Recommendation from "./Recommendation";

export default class RecommendAccessesSciPy extends Recommendation {
    profile: string;
    linkageTypes: string[];
    tracesMaxLimit: number;
    traceTypes: TraceType[];

    constructor(recommendation: any) {
        super(recommendation);
        this.profile =                  recommendation.profile              ||     "Generic";
        this.tracesMaxLimit =           recommendation.tracesMaxLimit       ||     0;
        this.traceTypes =               recommendation.traceTypes           ||     [TraceType.ALL];
        this.linkageTypes =             recommendation.linkageTypes         ||     ["average"];
    }
}
