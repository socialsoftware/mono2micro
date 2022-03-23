import Card from "react-bootstrap/Card";
import React from "react";
import Collector from "./Collector";
import Button from "react-bootstrap/Button";
import {SourceType} from "../sources/Source";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";

export default class AccessesCollector extends Collector {
    addedSources: Map<string, File> | undefined;

    constructor(type: string, codebaseName: string) {
        super(type, codebaseName,
            [
                SourceType.ACCESSES,
                SourceType.IDTOENTITIY
            ],
            [
                'ACCESSES_SCIPY',
            ]
        );
    }

    addSource(source: string, event: any) {
        if (this.addedSources === undefined)
            this.addedSources = new Map<string, File>();
        this.addedSources = ({...this.addedSources, [source]: event.target.files[0]});
    }

    canSubmit(): boolean {
        return this.addedSources !== undefined && this.addedSources.size === this.sources.length;
    }

    printForm(): JSX.Element {
        return <React.Fragment>
            { this.sources.map(source =>
                <Form.Group key={source} as={Row} className="mb-3 mt-2 align-items-center">
                    <Form.Label column sm={2}>
                        {source + " File"}
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="file"
                            onChange={event => this.addSource(source, event)}
                        />
                    </Col>
                </Form.Group>
            )}
        </React.Fragment>;
    }

    printCard(handleDeleteCollector: (collector: Collector) => void): JSX.Element {
        return <Card key={this.type} style={{width: '16rem'}}>
            <Card.Body>
                <Card.Title>
                    {this.type}
                </Card.Title>
                <Button
                    href={`/codebases/${this.codebaseName}/source/Accesses/profiles`}
                    className="mb-2"
                >
                    Change Controller Profiles
                </Button>
                <br/>
                <Button
                    onClick={() => handleDeleteCollector(this)}
                    className="mb-2"
                    variant={"danger"}
                >
                    Delete
                </Button>
            </Card.Body>
        </Card>;
    }
}