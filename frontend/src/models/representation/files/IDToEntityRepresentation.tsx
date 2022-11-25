import Representation from "../Representation";
import React from "react";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";

export default class IDToEntityRepresentation extends Representation {

    constructor(representation: IDToEntityRepresentation) {
        super(representation);
    }

    printCard(handleDeleteRepresentation: (representation: Representation) => void): JSX.Element {
        return <Card className={"text-center"} key={this.type} style={{width: '13rem'}}>
            <Card.Header>{this.type}</Card.Header>
            <Card.Body>
                <Button
                    onClick={() => handleDeleteRepresentation(this)}
                    className="mb-2"
                    variant={"danger"}
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}
