import {Accordion, ListGroup, ListGroupItem, Modal, ModalBody, ModalTitle} from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Divider from "@mui/material/Divider";
import React, {useEffect, useState} from "react";
import {EDGE_LENGTH, types} from "../utils/GraphUtils";

export const RepositoryViewModal = ({authors, clusters, commitsInCommon, totalCommits, showModal, setShowModal, clickedComponent, setClickedComponent}) => {
    const [title, setTitle] = useState("Options");
    const [informationText, setInformationText] = useState(undefined);

    useEffect(() => {
        if (clickedComponent === undefined || clickedComponent.operation !== "doubleClickEvent")
            return;
        if (clickedComponent.type === types.CLUSTER)
            setTitle("Cluster " + clickedComponent.label + " informations");
        else if (clickedComponent.type === types.ENTITY)
            setTitle("Entity " + clickedComponent.label + " informations");
        else {
            const fromNode = clickedComponent.fromNode;
            const toNode = clickedComponent.toNode;

            let text = "Edge with ";
            text += fromNode.type === types.CLUSTER? "cluster " + fromNode.label : "entity " + fromNode.label;
            text += toNode.type === types.CLUSTER? " and cluster " + toNode.label : " and entity " + toNode.label;
            setTitle(text);
        }
    }, [clickedComponent]);

    function handleClusterEntities() {
        setTitle("Entities that belong to cluster " + clickedComponent.label);

        setInformationText(
            <>
                <h4>{clickedComponent.elements.length} entities:</h4>
                <ListGroup>
                    {clickedComponent.elements.map(entity => <ListGroupItem key={entity.id}>{entity.name}</ListGroupItem>)}
                </ListGroup>
            </>
        );
    }

    function handleRelatedAuthorsEntities() {
        setTitle("Authors that made modifications to " + clickedComponent.label);
        const entityAuthors = authors[clickedComponent.id]

        setInformationText(
            <>
                <h4>{entityAuthors.length} Authors:</h4>
                <ListGroup>
                    { entityAuthors.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>) }
                </ListGroup>
            </>
        );
    }

    function handleRelatedAuthorsCluster() {
        setTitle("Authors that made modifications to " + clickedComponent.label);
        let clickedCluster = clusters.find(cluster => cluster.name === clickedComponent.id);
        let clickedClusterAuthors = [...new Set(clickedCluster.elements.flatMap(element => authors[element.id]))];

        setInformationText(
            <>
                <h4>{clickedClusterAuthors.length} Authors:</h4>
                <ListGroup>
                    { clickedClusterAuthors.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>) }
                </ListGroup>
            </>
        );
    }

    function handleCommonAuthorsCluster() {
        setTitle("Authors in common between " + clickedComponent.label + " and other Clusters");
        let clickedCluster = clusters.find(cluster => cluster.name === clickedComponent.id);
        let clickedClusterAuthors = [...new Set(clickedCluster.elements.flatMap(element => authors[element.id]))];

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    clusters.map(otherCluster => {
                        let otherClusterAuthors = [...new Set(otherCluster.elements.flatMap(element => authors[element.id]))];
                        let authorsInCommon = clickedClusterAuthors.filter(author => otherClusterAuthors.includes(author));

                        if (clickedCluster.name === otherCluster.name)
                            return undefined;
                        return (
                            <Accordion.Item eventKey={otherCluster.name} key={otherCluster.name}>
                                {authorsInCommon.length === 0 && <Accordion.Header>No authors in common with cluster {otherCluster.name}:</Accordion.Header>}
                                {authorsInCommon.length !== 0 && <Accordion.Header>In common with cluster {otherCluster.name}:</Accordion.Header>}
                                {authorsInCommon.length !== 0 &&
                                    <Accordion.Body>
                                        <ListGroup>
                                            <h5>{authorsInCommon.length} Authors in common:</h5>
                                            { authorsInCommon.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>) }
                                        </ListGroup>
                                    </Accordion.Body>
                                }
                            </Accordion.Item>
                        );
                    })
                }
            </Accordion>
        );
    }

    function handleCommonAuthorsBetweenClusters() {
        const cluster1 = clusters.find(cluster => cluster.name === clickedComponent.from);
        const cluster2 = clusters.find(cluster => cluster.name === clickedComponent.to);
        let cluster1Authors = [...new Set(cluster1.elements.flatMap(element => authors[element.id]))];
        let cluster2Authors = [...new Set(cluster2.elements.flatMap(element => authors[element.id]))];
        let authorsInCommon = cluster1Authors.filter(author => cluster2Authors.includes(author));

        setTitle("Authors that modify entities of " + clickedComponent.from + " and " + clickedComponent.to);
        setInformationText(
            <>
                {authorsInCommon.length === 0 && <h4> No authors in common.</h4>}
                {authorsInCommon.length !== 0 &&
                    <>
                        <h4>{authorsInCommon.length} authors in common:</h4>
                        <ListGroup>
                            {authorsInCommon.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>)}
                        </ListGroup>
                    </>
                }
            </>
        );
    }

    function handleCommonAuthorsBetweenClusterAndEntity() {
        let entity, cluster = clusters.find(cluster => cluster.name === clickedComponent.from);

        if (cluster) {
            entity = clickedComponent.toNode;
        }
        else {
            entity = clickedComponent.fromNode;
            cluster = clusters.find(cluster => cluster.name === clickedComponent.to);
        }

        let clusterAuthors = [...new Set(cluster.elements.flatMap(element => authors[element.id]))];
        let entityAuthors = authors[entity.id];

        let authorsInCommon = clusterAuthors.filter(author => entityAuthors.includes(author));

        setTitle("Authors that modify entities of " + cluster.name + " and " + entity.label);
        setInformationText(
            <>
                {authorsInCommon.length === 0 && <h4> No authors in common.</h4>}
                {authorsInCommon.length !== 0 &&
                    <>
                        <h4>{authorsInCommon.length} authors in common:</h4>
                        <ListGroup>
                            {authorsInCommon.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>)}
                        </ListGroup>
                    </>
                }
            </>
        );
    }

    function handleCommonAuthorsBetweenEntities() {
        const entity1Authors = authors[clickedComponent.from];
        const entity2Authors = authors[clickedComponent.to];
        let authorsInCommon = entity1Authors.filter(author => entity2Authors.includes(author));

        setTitle("Authors that modify entities of " + clickedComponent.fromNode.label + " and " + clickedComponent.toNode.label);
        setInformationText(
            <>
                {authorsInCommon.length === 0 && <h4> No authors in common.</h4>}
                {authorsInCommon.length !== 0 &&
                    <>
                        <h4>{authorsInCommon.length} authors in common:</h4>
                        <ListGroup>
                            {authorsInCommon.map(author => <ListGroupItem key={author}>{author}</ListGroupItem>)}
                        </ListGroup>
                    </>
                }
            </>
        );
    }

    function copheneticDistance() {
        return (<Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                {clickedComponent.type !== types.BETWEEN_ENTITIES && "Average"} Cophenetic Distance: {clickedComponent.length/EDGE_LENGTH}
            </Button>);
    }

    const closePopup = function () {
        setShowModal(false);
        setClickedComponent(undefined);
        setTitle("Options");
        setInformationText(undefined);
    };

    const backMenu = function () {setTitle("Options"); setInformationText(undefined)};

    return (
        <>
            <Modal
                show={showModal}
                onHide={() => closePopup()}
                backdrop="static"
                size='lg'
            >
                <ModalTitle className="ms-3">{title}</ModalTitle>
                <ModalBody style={{maxHeight: '90vh', overflowY: 'auto'}}>
                    {informationText !== undefined &&
                        <>
                            {informationText}
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="secondary" onClick={backMenu}>
                                    Go Back
                                </Button>
                            </div>
                        </>
                    }
                    {informationText === undefined && clickedComponent !== undefined &&
                        <>
                            {clickedComponent.type === types.CLUSTER &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleClusterEntities}>
                                            Cluster's Entities ({clickedComponent.elements.length} Entities)
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleRelatedAuthorsCluster}>
                                            Related Authors
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonAuthorsCluster}>
                                            Authors in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {clickedComponent.type === types.ENTITY &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                                            Total commits: {totalCommits[clickedComponent.id]}
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                                            Belongs to: {clickedComponent.group}
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                                            Number of modifications done: {totalCommits[clickedComponent.id]}
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleRelatedAuthorsEntities}>
                                            Related Authors
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {clickedComponent.type === types.BETWEEN_CLUSTERS &&
                                <>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonAuthorsBetweenClusters}>
                                            Authors in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {clickedComponent.type === types.BETWEEN_ENTITIES &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                                            Commits in Common: {commitsInCommon[clickedComponent.from][clickedComponent.to]}
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonAuthorsBetweenEntities}>
                                            Authors in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {clickedComponent.type === types.BETWEEN_CLUSTER_ENTITY &&
                                <>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonAuthorsBetweenClusterAndEntity}>
                                            Authors in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                        </>
                    }
                    <div className="d-flex flex-row">
                        <Button className="flex-grow-1 mt-2" variant="danger" onClick={() => closePopup()}>
                            Close
                        </Button>
                    </div>
                </ModalBody>
            </Modal>
        </>
    );
}