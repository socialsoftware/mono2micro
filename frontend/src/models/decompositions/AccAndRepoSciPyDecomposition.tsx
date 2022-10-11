import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import {MetricType} from "../../type-declarations/types.d";
import {Cached} from "@mui/icons-material";
import {RepositoryService} from "../../services/RepositoryService";
import {toast} from "react-toastify";

const ACC_AND_REPO_SCIPY = "Accesses and Repository-Based Similarity and SciPy Clustering Algorithm";
export {ACC_AND_REPO_SCIPY};

export default class AccAndRepoSciPyDecomposition extends Decomposition {
    outdated: boolean;
    expert: boolean;
    silhouetteScore: number;
    functionalities: any;
    entityIDToClusterName: any;

    constructor(decomposition: any) {
        super(decomposition);

        this.outdated = decomposition.outdated;
        this.expert = decomposition.expert;
        this.silhouetteScore = decomposition.silhouetteScore;
        this.functionalities = decomposition.functionalities;
        this.entityIDToClusterName = decomposition.entityIDToClusterName;
    }

    handleUpdate(reloadDecompositions: () => void) {
        const service = new RepositoryService();
        const promise = service.updatedAccessesSciPyDecomposition(this.name);
        toast.promise(promise, {
            pending: "Updating Decomposition...",
            success: {render: "Success updating decomposition!", autoClose: 2000},
            error: {render: "Error while updating decomposition.", autoClose: 5000}
        }).then(() => reloadDecompositions());
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
                        Type: {this.type} <br />
                        Number of Clusters: {Object.values(this.clusters).length} <br />
                        Singleton Clusters: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        {MetricType.COMPLEXITY}: {parseFloat(this.metrics[MetricType.COMPLEXITY]).toFixed(3)} <br />
                        {MetricType.PERFORMANCE}: {parseFloat(this.metrics[MetricType.PERFORMANCE]).toFixed(3)} <br />
                        {MetricType.COHESION}: {parseFloat(this.metrics[MetricType.COHESION]).toFixed(3)} <br />
                        {MetricType.COUPLING}: {parseFloat(this.metrics[MetricType.COUPLING]).toFixed(3)} <br />
                        TSR: {parseFloat(this.metrics[MetricType.TSR]).toFixed(3)} <br />
                        Silhouette Score: {this.silhouetteScore} <br />
                    </Card.Text>
                    {this.outdated &&
                        <Button
                            onClick={() => this.handleUpdate(reloadDecompositions)}
                            className="mb-2"
                            variant={"warning"}
                        >
                            Update Metrics <Cached/>
                        </Button>
                    }
                    <br/>
                    <Button
                        href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/accessesViews`}
                        className="mb-2"
                        variant={"success"}
                    >
                        View Accesses
                    </Button>
                    <Button
                        href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/repositoryView`}
                        className="mb-2"
                        variant={"success"}
                    >
                        View Repository
                    </Button>
                    <br/>
                    <Button
                        href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.name}/functionalityRefactor`}
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