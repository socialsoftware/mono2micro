import Card from "react-bootstrap/Card";
import React from "react";
import Collector from "./Collector";
import Button from "react-bootstrap/Button";
import {SourceType} from "../sources/Source";
import {StrategyType} from "../strategies/Strategy";

export default class AccessesCollector extends Collector {
    addedSources: Map<string, File> | undefined;

    constructor(collector: any) {
        super(collector.type, collector.codebaseName,
            collector.sources ?? [
                SourceType.ACCESSES,
                SourceType.IDTOENTITIY
            ],
            collector.possibleStrategies ?? [
                StrategyType.ACCESSES_SCIPY,
            ]
        );
        this.addedSources = collector.addedSources ?? new Map<string, File>();
    }

    canSubmit(): boolean {
        return this.addedSources !== undefined && Object.keys(this.addedSources).length === this.sources.length;
    }

    copy(): Collector {
        return new AccessesCollector(this);
    }

    printCard(handleDeleteCollector: (collector: Collector) => void): JSX.Element {
        return <Card key={this.type} style={{width: '13rem'}}>
            <Card.Body>
                <Card.Title>
                    {this.type}
                </Card.Title>
                <Button
                    href={`/codebases/${this.codebaseName}/source/Accesses/profiles`}
                    className="mb-2"
                >
                    Change Profile
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