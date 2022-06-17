import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import {Metric} from "../../type-declarations/types.d";

export default class AccessesSciPyDecomposition extends Decomposition {
    expert: boolean;
    clusters: any;
    functionalities: any;
    entityIDToClusterID: any;

    constructor(decomposition: any) {
        super(decomposition);

        this.expert = decomposition.expert;
        this.clusters = decomposition.clusters;
        this.functionalities = decomposition.functionalities;
        this.entityIDToClusterID = decomposition.entityIDToClusterID;
    }

    printCard(handleDeleteDecomposition: (collector: string) => void): JSX.Element {
        let amountOfSingletonClusters = 0;
        let maxClusterSize = 0;

        Object.values(this.clusters).forEach((c:any) => {
            const numberOfEntities = c.entities.length;
            if (numberOfEntities === 1) amountOfSingletonClusters++;
            if (numberOfEntities > maxClusterSize) maxClusterSize = numberOfEntities;
        })

        return (
            <Card key={this.name} style={{ width: '16rem', marginBottom: "16px" }}>
                <Card.Body>
                    <Card.Title>
                        {this.name}
                    </Card.Title>
                    <Card.Text>
                        Number of Clusters: {Object.values(this.clusters).length} <br />
                        Singleton Servers: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        {this.metrics.map((metric: Metric) => <React.Fragment key={metric.type}>{metric.type}: {metric.value}<br/></React.Fragment>)}
                    </Card.Text>
                    <Button
                        href={`/codebases/${this.codebaseName}/strategies/${this.strategyName}/decompositions/${this.name}`}
                        className="mb-2"
                        variant={"success"}
                    >
                        View Decomposition
                    </Button>
                    <br/>
                    <Button
                        href={`/codebases/${this.codebaseName}/strategies/${this.strategyName}/decompositions/${this.name}/functionalityRefactor`}
                        className="mb-2"
                        variant={"primary"}
                    >
                        Refactorization Tool
                    </Button>
                    <br/>
                    <Button
                        onClick={() => handleDeleteDecomposition(this.name)}
                        variant="danger"
                    >
                        Delete
                    </Button>
                </Card.Body>
            </Card>
        );
    }
}