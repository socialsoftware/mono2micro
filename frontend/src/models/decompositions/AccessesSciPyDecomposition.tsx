import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";

export default class AccessesSciPyDecomposition extends Decomposition {
    expert: boolean;
    silhouetteScore: number;
    complexity: number;
    performance: number;
    cohesion: number;
    coupling: number;
    clusters: any;
    controllers: any;
    entityIDToClusterID: any;

    constructor(decomposition: any) {
        super(
            decomposition.name,
            decomposition.codebaseName,
            decomposition.strategyName,
            decomposition.strategyType
        );

        this.expert = decomposition.expert;
        this.silhouetteScore = decomposition.silhouetteScore;
        this.complexity = decomposition.complexity;
        this.performance = decomposition.performance;
        this.cohesion = decomposition.cohesion;
        this.coupling = decomposition.coupling;
        this.clusters = decomposition.clusters;
        this.controllers = decomposition.controllers;
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
            <Card key={this.name} style={{ width: '15rem', marginBottom: "16px" }}>
                <Card.Body>
                    <Card.Title>
                        {this.name}
                    </Card.Title>
                    <Card.Text>
                        Number of Clusters: {Object.values(this.clusters).length} <br />
                        Singleton Servers: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        Complexity: {this.complexity} <br />
                        Performance: {this.performance} <br />
                        Cohesion: {this.cohesion} <br />
                        Coupling: {this.coupling} < br />
                    </Card.Text>
                    <Button
                    href={`/codebases/${this.codebaseName}/strategies/${this.strategyName}/decompositions/${this.name}`}
                    className="mb-2"
                    variant={"success"}
                        >
                        Go to Decomposition
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