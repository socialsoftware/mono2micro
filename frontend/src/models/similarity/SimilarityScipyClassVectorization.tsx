import Similarity from "./Similarity";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";

const SIMILARITY_SCIPY_CLASS_VECTORIZATION = 'SIMILARITY_SCIPY_CLASS_VECTORIZATION';
export {SIMILARITY_SCIPY_CLASS_VECTORIZATION};

export default class SimilarityScipyClassVectorization extends Similarity {
    linkageType: string;

    constructor(similarity: any) {
        super(similarity);
        // Initializes default values if no previous value is provided
        this.linkageType =          similarity.linkageType             ||     "average";
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
                    Linkage Type: {this.linkageType} < br />
                </Card.Text>
                <Button href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/decomposition`}
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