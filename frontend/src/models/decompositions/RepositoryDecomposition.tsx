import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import {MetricType} from "../../type-declarations/types";
import Button from "react-bootstrap/Button";
import React from "react";

const REPOSITORY_DECOMPOSITION = "Repository Decomposition";
export {REPOSITORY_DECOMPOSITION};

export default class RepositoryDecomposition extends Decomposition {
    expert: boolean;
    entityIDToClusterName: any;
    authors: Record<number, string[]>;
    commitsInCommon: Record<number, Record<number, number>>;
    totalCommits: Record<number, number>;

    constructor(decomposition: any) {
        super(decomposition);

        this.expert = decomposition.expert;
        this.entityIDToClusterName = decomposition.entityIDToClusterName;
        this.authors = decomposition.authors;
        this.commitsInCommon = decomposition.commitsInCommon;
        this.totalCommits = decomposition.totalCommits;
    }

    printCard(reloadDecompositions: () => void, handleDeleteDecomposition: (collector: string) => void): JSX.Element {
        let amountOfSingletonClusters = 0;
        let maxClusterSize = 0;
        let totalEntities = 0;

        Object.values(this.clusters).forEach((c:any) => {
            const numberOfEntities = c.elements.length;
            totalEntities += numberOfEntities;
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
                        Number of Entities: {totalEntities} <br />
                        Singleton Clusters: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        {MetricType.SILHOUETTE_SCORE}: {parseFloat(this.metrics[MetricType.SILHOUETTE_SCORE])} <br />
                        TSR: {parseFloat(this.metrics[MetricType.TSR]).toFixed(3)} <br />
                    </Card.Text>
                    <br/>
                    <Button
                        href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.similarityName}/${this.name}/repositoryView`}
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
