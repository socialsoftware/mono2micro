import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import {MetricType} from "../../type-declarations/types";
import Button from "react-bootstrap/Button";
import React from "react";

const REPOSITORY_SCIPY = "Repository-Based Similarity and SciPy Clustering Algorithm";
export {REPOSITORY_SCIPY};

export default class RepositorySciPyDecomposition extends Decomposition {
    expert: boolean;
    silhouetteScore: number;
    entityIDToClusterName: any;
    authors: Record<number, string[]>;
    commitsInCommon: Record<number, Record<number, number>>;
    totalCommits: Record<number, number>;

    constructor(decomposition: any) {
        super(decomposition);

        this.expert = decomposition.expert;
        this.silhouetteScore = decomposition.silhouetteScore;
        this.entityIDToClusterName = decomposition.entityIDToClusterName;
        this.authors = decomposition.authors;
        this.commitsInCommon = decomposition.commitsInCommon;
        this.totalCommits = decomposition.totalCommits;
    }

    printCard(reloadDecompositions: () => void, handleDeleteDecomposition: (collector: string) => void): JSX.Element {
        let amountOfSingletonClusters = 0;
        let maxClusterSize = 0;

        Object.values(this.clusters).forEach((c:any) => {
            const numberOfEntities = c.elements.length;
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
                        Singleton Clusters: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        TSR: {parseFloat(this.metrics[MetricType.TSR]).toFixed(3)} <br />
                        Silhouette Score: {this.silhouetteScore} <br />
                    </Card.Text>
                    <br/>
                    <Button
                        href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/repositoryView`}
                        className="mb-2"
                        variant={"success"}
                    >
                        Repository View
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
