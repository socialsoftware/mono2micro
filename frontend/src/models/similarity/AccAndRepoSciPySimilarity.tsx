import Similarity from "./Similarity";
import {TraceType} from "../../type-declarations/types.d";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";

export default class AccAndRepoSciPySimilarity extends Similarity {
    accessMetricWeight: number;
    writeMetricWeight: number;
    readMetricWeight: number;
    sequenceMetricWeight: number;
    authorMetricWeight: number;
    commitMetricWeight: number;
    profile: string;
    linkageType: string;
    tracesMaxLimit: number;
    traceType: TraceType;

    constructor(similarity: any) {
        super(similarity);
        // Initializes default values if no previous value is provided
        this.accessMetricWeight =   similarity.accessMetricWeight === undefined? 17 : similarity.accessMetricWeight;
        this.writeMetricWeight =    similarity.writeMetricWeight === undefined? 17 : similarity.writeMetricWeight;
        this.readMetricWeight =     similarity.readMetricWeight === undefined? 17 : similarity.readMetricWeight;
        this.sequenceMetricWeight = similarity.sequenceMetricWeight === undefined? 17 : similarity.sequenceMetricWeight;
        this.authorMetricWeight = similarity.authorMetricWeight === undefined? 16 : similarity.authorMetricWeight;
        this.commitMetricWeight = similarity.commitMetricWeight === undefined? 16 : similarity.commitMetricWeight;
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
                    Sequence: {this.sequenceMetricWeight}% < br />
                    Author: {this.authorMetricWeight}% < br />
                    Commit: {this.commitMetricWeight}%
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
