import Strategy from "./Strategy";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import Dendrogram from "../dendrogram/Dendrogram";

export default class AccessesSciPyStrategy extends Strategy {
    dendrograms?: Dendrogram[];

    public constructor(strategy: any) {
        super(strategy);
        this.dendrograms = strategy.dendrograms;
    }

    printCard(handleDeleteStrategy: (strategy: Strategy) => void): JSX.Element {
        return <Card className={"text-center"} key={this.type} style={{width: '13rem'}}>
            <Card.Header>{this.type}</Card.Header>
            <Card.Body>
                <Button
                    href={`/codebases/${this.codebaseName}/strategy/${this.name}`}
                    className="mb-2"
                >
                    Dendrogram Generation
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