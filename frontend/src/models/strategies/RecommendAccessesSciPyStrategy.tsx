import Strategy from "./Strategy";
import Card from "react-bootstrap/Card";
import React from "react";
import {TraceType} from "../../type-declarations/types.d";
import Button from "react-bootstrap/Button";

export default class RecommendAccessesSciPyStrategy extends Strategy {
    profile: string;
    linkageTypes: string[];
    tracesMaxLimit: number;
    traceTypes: TraceType[];
    isCompleted: boolean;

    constructor(strategy: any) {
        super(strategy);
        this.profile =                  strategy.profile              ||     "Generic";
        this.tracesMaxLimit =           strategy.tracesMaxLimit       ||     0;
        this.traceTypes =               strategy.traceTypes           ||     [TraceType.ALL];
        this.linkageTypes =             strategy.linkageTypes         ||     ["average"];
        this.isCompleted =              Boolean(strategy.completed)   ||     false;
    }

    readyToSubmit(): boolean {
        return this.linkageTypes.length !== 0 && this.profile !== "" && this.traceTypes.length !== 0;
    }

    copy(): Strategy {
        return new RecommendAccessesSciPyStrategy(this);
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{width: '20rem'}}>
            <Card.Body>
                <Card.Title>{this.name}</Card.Title>
                <Card.Text>
                    Profile: {this.profile} <br />
                    AmountOfTraces: {this.tracesMaxLimit} <br />
                    Type of traces: {this.traceTypes.reduce((p, c) => p + c + ", ", "")} <br />
                    Linkage Type: {this.linkageTypes.reduce((p, c) => p + c + ", ", "")} < br />
                </Card.Text>
                <Button
                    onClick={() => handleDeleteStrategy(this)}
                    variant="danger"
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}
