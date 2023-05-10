import Decomposition from "./Decomposition";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import React from "react";
import {Cached} from "@mui/icons-material";
import {APIService} from "../../services/APIService";
// @ts-ignore
import {toast} from "react-toastify";
import {RepresentationType} from "../representation/Representation";

const PARTITIONS_DECOMPOSITION = "Partitions Decomposition";
export {PARTITIONS_DECOMPOSITION};

export default class PartitionsDecomposition extends Decomposition {
    outdated: boolean;
    representationInformationsTypes: string;

    constructor(decomposition: any) {
        super(decomposition);
        this.outdated = decomposition.outdated;
        this.representationInformationsTypes = decomposition.representationInformations.map((rep:any) => rep.type);
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

    printCard(
        reloadDecompositions: () => void,
        handleDeleteDecomposition: (collector: string) => void,
        handleExportDecomposition: (collector: string) => void
    ): JSX.Element {
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
                        {Object.entries(this.metrics).map(([name, val]) => <span key={name}>{name}: {val} <br /></span>)}
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
                    {this.representationInformationsTypes.includes(RepresentationType.ACCESSES_TYPE) &&
                        <>
                            <Button
                                href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.similarityName}/${this.name}/accessesViews`}
                                className="mb-2"
                                variant={"success"}
                            >
                                View Accesses
                            </Button>
                            <br/>
                        </>
                    }
                    {this.representationInformationsTypes.includes(RepresentationType.REPOSITORY_TYPE) &&
                        <>
                            <Button
                                href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.similarityName}/${this.name}/repositoryView`}
                                className="mb-2"
                                variant={"success"}
                            >
                                View Repository
                            </Button>
                            <br/>
                        </>
                    }
                    {this.representationInformationsTypes.includes(RepresentationType.ACCESSES_TYPE) &&
                        <>
                            <Button
                                href={`/codebases/${this.codebaseName}/${this.strategyName}/${this.similarityName}/${this.name}/functionalityRefactor`}
                                className="mb-2"
                                variant={"primary"}
                            >
                                Refactorization Tool
                            </Button>
                            <br/>
                        </>
                    }
                    {this.representationInformationsTypes.includes(RepresentationType.ACCESSES_TYPE) &&
                        <>
                            <Button
                                onClick={() => handleExportDecomposition(this.name)}
                                className="mb-2"
                                variant={"primary"}
                            >
                                Export Data
                            </Button>
                            <br/>
                        </>
                    }
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