import Similarity from "./Similarity";
import {TraceType} from "../../type-declarations/types.d";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";

export default class AccessesSciPySimilarity extends Similarity {
    accessMetricWeight: number;
    writeMetricWeight: number;
    readMetricWeight: number;
    sequenceMetricWeight: number;
    profile: string;
    linkageType: string;
    tracesMaxLimit: number;
    traceType: TraceType;

    constructor(similarity: any) {
        super(similarity);
        // Initializes default values if no previous value is provided
        this.accessMetricWeight =   similarity.accessMetricWeight === undefined? 25 : similarity.accessMetricWeight;
        this.writeMetricWeight =    similarity.writeMetricWeight === undefined? 25 : similarity.writeMetricWeight;
        this.readMetricWeight =     similarity.readMetricWeight === undefined? 25 : similarity.readMetricWeight;
        this.sequenceMetricWeight = similarity.sequenceMetricWeight === undefined? 25 : similarity.sequenceMetricWeight;
        this.profile =              similarity.profile                 ||     "Generic";
        this.linkageType =          similarity.linkageType             ||     "average";
        this.tracesMaxLimit =       similarity.tracesMaxLimit          ||     0;
        this.traceType =            similarity.traceType               ||     TraceType.ALL;
    }

    printCard(handleDeleteSimilarity: (similarity: Similarity) => void): JSX.Element {
        return <Card key={this.name} className="mb-4" style={{ width: '20rem' }}>
            <Card.Img
                variant="top"
                src={URL + "/similarity/" + this.name + "/image?" + new Date().getTime()}
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
                <Button href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/similarity`}
                        variant={"success"}
                        className="mb-2">
                    Decomposition Generation
                </Button>
                <br />
                <Button
                    onClick={() => handleDeleteSimilarity(this)}
                    variant="danger"
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}