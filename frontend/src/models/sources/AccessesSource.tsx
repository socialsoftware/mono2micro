import Source from "./Source";
import React from "react";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";

export default class AccessesSource extends Source {
    profiles!: Map<string, string>;

    constructor(source: AccessesSource) {
        super(source);
        this.profiles = source.profiles;
    }

    printCard(handleDeleteSource: (source: Source) => void): JSX.Element {
        return <Card className={"text-center"} key={this.type} style={{width: '13rem'}}>
            <Card.Header>{this.type}</Card.Header>
            <Card.Body>
                <Button
                    href={`/codebase/${this.codebaseName}/${this.name}/profiles`}
                    className="mb-2"
                >
                    Change Profile
                </Button>
                <br/>
                <Button
                    onClick={() => handleDeleteSource(this)}
                    className="mb-2"
                    variant={"danger"}
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}
