import Strategy from "./Strategy";
import {TraceType} from "../../type-declarations/types.d";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";

export default class AccessesSciPyStrategy extends Strategy {
    accessMetricWeight: number;
    writeMetricWeight: number;
    readMetricWeight: number;
    sequenceMetricWeight: number;
    profile: string;
    linkageType: string;
    tracesMaxLimit: number;
    traceType: TraceType;

    constructor(strategy: any) {
        super(strategy.type, strategy.codebaseName, strategy.name, strategy.decompositionsNames);
        // Initializes default values if no previous value is provided
        this.accessMetricWeight =   strategy.accessMetricWeight      ||     25;
        this.writeMetricWeight =    strategy.writeMetricWeight       ||     25;
        this.readMetricWeight =     strategy.readMetricWeight        ||     25;
        this.sequenceMetricWeight = strategy.sequenceMetricWeight    ||     25;
        this.profile =              strategy.profile                 ||     "";
        this.linkageType =          strategy.linkageType             ||     "average";
        this.tracesMaxLimit =       strategy.tracesMaxLimit          ||     0;
        this.traceType =            strategy.traceType               ||     TraceType.ALL;
    }

    readyToSubmit(): boolean {
        return this.linkageType !== "" &&
        this.accessMetricWeight + this.writeMetricWeight + this.readMetricWeight + this.sequenceMetricWeight === 100 &&
        this.profile !== "" &&
        this.traceType !== ""
    }

    copy(): Strategy {
        return new AccessesSciPyStrategy(this);
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{ width: '20rem' }}>
            <Card.Img
                variant="top"
                src={URL + "codebase/" + this.codebaseName + "/strategy/" + this.name + "/image?" + new Date().getTime()}
            />
            <Card.Body>
                <Card.Title>{this.name}</Card.Title>
                <Card.Text>
                    Profile: {this.profile} <br />
                    AmountOfTraces: {this.tracesMaxLimit} <br />
                    Type of traces: {this.traceType} <br />
                    Linkage Type: {this.linkageType} < br />
                    Access: {this.accessMetricWeight}% < br />
                    Write: {this.writeMetricWeight}% < br />
                    Read: {this.readMetricWeight}% < br />
                    Sequence: {this.sequenceMetricWeight}%
                </Card.Text>
                <Button href={`/codebases/${this.codebaseName}/strategies/${this.name}`}
                        className="mb-2">
                    Go to Strategy
                </Button>
                <br />
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