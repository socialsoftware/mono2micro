import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import {MetricType} from "../../type-declarations/types";
import {Cached} from "@mui/icons-material";
import {APIService} from "../../services/APIService";
import {toast} from "react-toastify";

const ACC_AND_REPO_DECOMPOSITION = "Accesses and Repository Decomposition";
export {ACC_AND_REPO_DECOMPOSITION};

export default class AccAndRepoDecomposition extends Decomposition {
    outdated: boolean;
    expert: boolean;
    functionalities: any;
    entityIDToClusterName: any;
    authors: Record<number, string[]>;
    commitsInCommon: Record<number, Record<number, number>>;
    totalCommits: Record<number, number>;

    constructor(decomposition: any) {
        super(decomposition);

        this.outdated = decomposition.outdated;
        this.expert = decomposition.expert;
        this.functionalities = decomposition.functionalities;
        this.entityIDToClusterName = decomposition.entityIDToClusterName;
        this.authors = decomposition.authors;
        this.commitsInCommon = decomposition.commitsInCommon;
        this.totalCommits = decomposition.totalCommits;
    }

    handleUpdate(reloadDecompositions: () => void) {
        const service = new APIService();
        const promise = service.updateDecomposition(this.name);
        toast.promise(promise, {
            pending: "Updating Decomposition...",
            success: {render: "Success updating decomposition!", autoClose: 2000},
            error: {render: "Error while updating decomposition.", autoClose: 5000}
        }).then(() => reloadDecompositions());
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
                        Type: {this.type} <br />
                        Number of Clusters: {Object.values(this.clusters).length} <br />
                        Number of Entities: {totalEntities} <br />
                        Singleton Clusters: {amountOfSingletonClusters} <br />
                        Maximum Cluster Size: {maxClusterSize} <br />
                        {MetricType.SILHOUETTE_SCORE}: {parseFloat(this.metrics[MetricType.SILHOUETTE_SCORE])} <br />
                        {MetricType.COMPLEXITY}: {parseFloat(this.metrics[MetricType.COMPLEXITY]).toFixed(3)} <br />
                        {MetricType.PERFORMANCE}: {parseFloat(this.metrics[MetricType.PERFORMANCE]).toFixed(3)} <br />
                        {MetricType.COHESION}: {parseFloat(this.metrics[MetricType.COHESION]).toFixed(3)} <br />
                        {MetricType.COUPLING}: {parseFloat(this.metrics[MetricType.COUPLING]).toFixed(3)} <br />
                        TSR: {parseFloat(this.metrics[MetricType.TSR]).toFixed(3)} <br />
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