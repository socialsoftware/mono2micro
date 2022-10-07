import Similarity from "./Similarity";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import {URL} from "../../constants/constants";

export default class RepositorySciPySimilarity extends Similarity {
    authorMetricWeight: number;
    commitMetricWeight: number;

    constructor(similarity: any) {
        super(similarity);
        // Initializes default values if no previous value is provided
        this.authorMetricWeight =   similarity.authorMetricWeight === undefined? 50 : similarity.authorMetricWeight;
        this.commitMetricWeight =   similarity.commitMetricWeight === undefined? 50 : similarity.commitMetricWeight;
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
