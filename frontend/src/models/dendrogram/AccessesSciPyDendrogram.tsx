import Dendrogram from "./Dendrogram";
import {TraceType} from "../../type-declarations/types.d";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";

export default class AccessesSciPyDendrogram extends Dendrogram {
    accessMetricWeight: number;
    writeMetricWeight: number;
    readMetricWeight: number;
    sequenceMetricWeight: number;
    profile: string;
    linkageType: string;
    tracesMaxLimit: number;
    traceType: TraceType;

    constructor(dendrogram: any) {
        super(dendrogram);
        // Initializes default values if no previous value is provided
        this.accessMetricWeight =   dendrogram.accessMetricWeight === undefined? 25 : dendrogram.accessMetricWeight;
        this.writeMetricWeight =    dendrogram.writeMetricWeight === undefined? 25 : dendrogram.writeMetricWeight;
        this.readMetricWeight =     dendrogram.readMetricWeight === undefined? 25 : dendrogram.readMetricWeight;
        this.sequenceMetricWeight = dendrogram.sequenceMetricWeight === undefined? 25 : dendrogram.sequenceMetricWeight;
        this.profile =              dendrogram.profile                 ||     "Generic";
        this.linkageType =          dendrogram.linkageType             ||     "average";
        this.tracesMaxLimit =       dendrogram.tracesMaxLimit          ||     0;
        this.traceType =            dendrogram.traceType               ||     TraceType.ALL;
    }

    printCard(handleDeleteDendrogram: (dendrogram: Dendrogram) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{ width: '20rem' }}>
            <Card.Img
                variant="top"
                src={URL + "/dendrogram/" + this.name + "/image?" + new Date().getTime()}
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
                <Button href={`/codebases/${this.codebaseName}/strategies/${this.strategyName}/dendrograms/${this.name}`}
                        variant={"success"}
                        className="mb-2">
                    Decomposition Generation
                </Button>
                <br />
                <Button
                    onClick={() => handleDeleteDendrogram(this)}
                    variant="danger"
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}