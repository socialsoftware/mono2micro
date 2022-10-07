import Strategy from "./Strategy";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import Similarity from "../similarity/Similarity";

export default class AccAndRepoSciPyStrategy extends Strategy {
    similarities?: Similarity[];

    public constructor(strategy: any) {
        super(strategy);
        this.hasSimilarities = true;
        this.similarities = strategy.similarities;
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card className={"text-center"} key={this.type} style={{width: '13rem'}}>
            <Card.Header>{this.type}</Card.Header>
            <Card.Body>
                <Button
                    href={`/codebases/${this.codebaseName}/${this.name}`}
                    className="mb-2"
                >
                    Similarity Distance Generation
                </Button>
                <br/>
                <Button
                    onClick={() => handleDeleteStrategy(this)}
                    className="mb-2"
                    variant={"danger"}
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}
