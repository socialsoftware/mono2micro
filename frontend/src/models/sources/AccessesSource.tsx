import Source from "./Source";
import Card from "react-bootstrap/Card";
import React from "react";
import Button from "react-bootstrap/Button";

export default class AccessesSource extends Source {
    profiles!: Map<string, string>;

    constructor(source: AccessesSource) {
        super(source.type, source.inputFilePath, source.codebaseName);
        this.profiles = source.profiles;
    }

    printCard(): JSX.Element {
        return <Card key={this.type} style={{width: '16rem'}}>
            <Card.Body>
                <Card.Title>
                    {this.type}
                </Card.Title>
                <Button
                    href={`/codebases/${this.codebaseName}/profiles`}
                    className="mb-2"
                >
                    Change Controller Profiles
                </Button>
                <br/>
                <Button
                    href={`/codebases/${this.codebaseName}/strategies`}
                    className="mb-2"
                >
                    Go to Strategies
                </Button>
            </Card.Body>
        </Card>;
    }
}
