import Strategy from "./Strategy";
import Card from "react-bootstrap/Card";
import React from "react";
import {TraceType} from "../../type-declarations/types.d";

export default class RecommendAccessesSciPyStrategy extends Strategy {
    profile: string;
    linkageTypes: string[];
    tracesMaxLimit: number;
    traceTypes: TraceType[];
    completed: boolean;

    constructor(strategy: any) {
        super(strategy.type, strategy.codebaseName, strategy.name, strategy.decompositionsNames);
        this.profile =                  strategy.profile              ||     "Generic";
        this.tracesMaxLimit =           strategy.tracesMaxLimit       ||     0;
        this.traceTypes =               strategy.traceTypes           ||     [TraceType.ALL];
        this.linkageTypes =             strategy.linkageTypes         ||     ["average"];
        this.completed =                Boolean(strategy.completed)   ||     false;
    }

    readyToSubmit(): boolean {
        return this.linkageTypes.length !== 0 && this.profile !== "" && this.traceTypes.length !== 0;
    }

    copy(): Strategy {
        return new RecommendAccessesSciPyStrategy(this);
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{width: '20rem'}}>
        </Card>;
    }
}
