import Strategy from "./Strategy";
import Card from "react-bootstrap/Card";
import React from "react";
import {TraceType} from "../../type-declarations/types.d";

export default class RecommendAccessesSciPyStrategy extends Strategy {
    profile: string;
    linkageType: string;
    tracesMaxLimit: number;
    traceType: TraceType;

    constructor(strategy: any) {
        super(strategy.type, strategy.codebaseName, strategy.name, strategy.decompositionsNames);
        this.profile =              strategy.profile                 ||     "Generic";
        this.tracesMaxLimit =       strategy.tracesMaxLimit          ||     0;
        this.traceType =            strategy.traceType               ||     TraceType.ALL;
        this.linkageType =          strategy.linkageType             ||     "average";
    }

    readyToSubmit(): boolean {
        return this.linkageType !== "" && this.profile !== "" && this.traceType !== "";
    }

    copy(): Strategy {
        return new RecommendAccessesSciPyStrategy(this);
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{width: '20rem'}}>
        </Card>;
    }
}
