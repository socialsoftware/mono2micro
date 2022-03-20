import Source from "./Source";
import Card from "react-bootstrap/Card";
import React from "react";

export default class TranslationSource extends Source {

    constructor(source: TranslationSource) {
        super(source.type, source.inputFilePath, source.codebaseName);
    }

    printCard(): JSX.Element {
        return <Card key={this.type} style={{width: '16rem'}}>
            <Card.Body>
                <Card.Title>
                    {this.type}
                </Card.Title>
                <Card.Text className={"text-secondary mt-4"}>(No properties in this source)</Card.Text>
            </Card.Body>
        </Card>;
    }
}
