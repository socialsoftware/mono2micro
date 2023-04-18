import Similarity from "./Similarity";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {URL} from "../../constants/constants"
import React from "react";
import {WeightsFactory} from "../weights/WeightsFactory";
import Weights from "../weights/Weights";

const SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH = 'SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH';
export {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH};

export default class SimilarityScipyFunctionalityVectorizationByCallGraph extends Similarity {
    linkageType: string;
    depth: number;
    weightsList: Weights[];

    constructor(similarity: any) {
        super(similarity);
        // Initializes default values if no previous value is provided
        this.linkageType =          similarity.linkageType             ||     "average";
        this.depth =                similarity.depth                   ||     2;
        this.weightsList =          WeightsFactory.getWeightsList(similarity.weightsList);
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
                    Depth: {this.depth} < br />
                    {this.weightsList.flatMap((weights: any) =>
                        Object.entries(weights.weightsLabel).map(([key, value]) => <span key={key}>{value + ": " + weights[key] + "%"} <br/></span>)
                    )}
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