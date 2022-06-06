import {Accordion, ListGroup, ListGroupItem, Modal, ModalBody, ModalTitle} from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Divider from "@mui/material/Divider";
import React, {useContext, useEffect, useState} from "react";
import AppContext from "../../AppContext";
import {ArrowRightAlt} from "@mui/icons-material";

const ACCESS_TYPES = {
    1: "READ",
    2: "WRITE",
    3: "READ/WRITE"
};

export const ClusterViewModal = ({clusters, edgeWeights, clustersFunctionalities, visGraph, network, showModal, setShowModal, clickedComponent, setClickedComponent}) => {
    const context = useContext(AppContext);
    const { translateEntity } = context;

    const [title, setTitle] = useState("Options");
    const [informationText, setInformationText] = useState(undefined);

    useEffect(() => {
        if (clickedComponent === undefined)
            return;
        if (clickedComponent.eventType === "doubleClickCluster") {
            const cluster = clusters.find(cluster => "c" + cluster.id === clickedComponent.id);
            setTitle("Cluster " + cluster.name + " informations");
        }
        else if (clickedComponent.eventType === "doubleClickEntity") {
            setTitle("Entity " + translateEntity(clickedComponent.id.substring(1)) + " informations");
        }
        else if (clickedComponent.eventType === "doubleClickEdge") {
            const edge = network.body.edges[clickedComponent.id];
            let text = "Edge with ";
            text += network.clustering.isCluster(edge.fromId)?
                "cluster " + clusters.find(cluster => "c" + cluster.id === edge.fromId).name :
                "entity " + translateEntity(Number(edge.fromId.substring(1)));

            text += network.clustering.isCluster(edge.toId)?
                " and cluster " + clusters.find(cluster => "c" + cluster.id === edge.toId).name :
                " and entity " + translateEntity(Number(edge.toId.substring(1)));
            setTitle(text);
        }

    }, [clickedComponent]);

    function handleClusterEntities() {
        const cluster = clusters.find(cluster => "c" + cluster.id === clickedComponent.id);
        setTitle("Entities that belong to cluster " + cluster.name);

        setInformationText(
            <ListGroup>
                {
                    cluster.entities.map(entityId =>
                        <ListGroupItem key={entityId}>{translateEntity(entityId)}</ListGroupItem>
                    )
                }
            </ListGroup>
        );
    }

    function handleCouplingDependenciesCluster() {
        setTitle("Dependencies between other clusters");
        const cluster = clusters.find(cluster => "c" + cluster.id === clickedComponent.id);
        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    Object.entries(cluster.couplingDependencies).map(([clusterId, couplingDependency]) => {
                        const dependencyCluster = clusters.find(c => c.id == clusterId);
                        return (
                            <Accordion.Item eventKey={dependencyCluster.id} key={dependencyCluster.id}>
                                <Accordion.Header>Depending on the following cluster {dependencyCluster.name} entities:</Accordion.Header>
                                <Accordion.Body>
                                    <ListGroup>
                                        {couplingDependency.map(entityId => <ListGroupItem key={entityId}>{translateEntity(entityId)}</ListGroupItem>)}
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>
                        );
                    })
                }
            </Accordion>
        );
    }

    function handleRelatedFunctionalitiesCluster() {
        const cluster = clusters.find(cluster => "c" + cluster.id === clickedComponent.id);
        setTitle("Functionalities that access cluster " + cluster.name);

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    clustersFunctionalities[cluster.id].map(functionality => {
                        return (
                            <Accordion.Item eventKey={functionality.name} key={functionality.name}>
                                <Accordion.Header><i>({functionality.type})</i>&ensp;{functionality.name}</Accordion.Header>
                                <Accordion.Body>
                                    <ListGroup>
                                        {
                                            functionality.entitiesPerCluster[cluster.id].map(entityId =>
                                                <ListGroupItem key={entityId}>{translateEntity(entityId)}</ListGroupItem>
                                            )
                                        }
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>
                        );
                    })
                }
            </Accordion>
        );
    }

    function handleRelatedFunctionalitiesEntities() {
        const entity = visGraph.nodes.get(clickedComponent.id);
        const entityId = entity.id.substring(1);

        setTitle("Functionalities accessing " + translateEntity(entityId) + " and respective type of access");

        setInformationText(
            <ListGroup>
                {
                    clustersFunctionalities[entity.cid].map(functionality => {
                        if (functionality.entities[entityId] !== undefined)
                            return <ListGroupItem key={functionality.name}><b>{functionality.name}:</b> {ACCESS_TYPES[functionality.entities[entityId]]}</ListGroupItem>;
                    })
                }
            </ListGroup>
        );
    }

    function handleRelatedClustersEntities() {
        const entityId = Number(clickedComponent.id.substring(1));
        setTitle("Functionalities in common between " + translateEntity(entityId) + " and respective Clusters");
        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    Object.entries(clustersFunctionalities).map(([clusterId, functionalities]) => {
                        const cluster = clusters.find(c => c.id == clusterId);
                        return (
                            <Accordion.Item eventKey={cluster.id} key={cluster.id}>
                                {cluster.entities.includes(entityId) &&
                                    <Accordion.Header>In common inside own cluster ({cluster.name}):</Accordion.Header>
                                }
                                {!cluster.entities.includes(entityId) &&
                                    <Accordion.Header>In common with cluster {cluster.name}:</Accordion.Header>
                                }
                                <Accordion.Body>
                                    <ListGroup>
                                        {
                                            functionalities.map(functionality => {
                                                if (functionality.entities[entityId] !== undefined)
                                                    return <ListGroupItem key={functionality.name}>{functionality.name}</ListGroupItem>;
                                            })
                                        }
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>
                        );
                    })
                }
            </Accordion>
        );
    }

    function handleCouplingDependenciesFunctionality() {
        const edge = network.body.edges[clickedComponent.id];
        const c1 = clusters.find(cluster => "c" + cluster.id === edge.fromId);
        const c2 = clusters.find(cluster => "c" + cluster.id === edge.toId);

        setTitle("Dependencies between Cluster " + c1.name + " and " + c2.name);

        let couplingC1C2 = c1.couplingDependencies[c2.id] === undefined ? [] : c1.couplingDependencies[c2.id];
        let couplingC2C1 = c2.couplingDependencies[c1.id] === undefined ? [] : c2.couplingDependencies[c1.id];

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    couplingC1C2.length > 0 &&
                    <Accordion.Item eventKey="0" key="0">
                        <Accordion.Header>Coupling dependencies from {c1.name} <ArrowRightAlt/> {c2.name}:</Accordion.Header>
                        <Accordion.Body>
                            <ListGroup>
                                { couplingC1C2.map(entity => <ListGroupItem key={entity}>{translateEntity(entity)}</ListGroupItem>) }
                            </ListGroup>
                        </Accordion.Body>
                    </Accordion.Item>
                }
                { couplingC1C2.length <= 0 && <Accordion.Item eventKey="0">
                    <Accordion.Header>No coupling dependencies from {c1.name} <ArrowRightAlt/> {c2.name}</Accordion.Header>
                </Accordion.Item> }
                {
                    couplingC2C1.length > 0 &&
                    <Accordion.Item eventKey="1" key="1">
                        <Accordion.Header>Coupling dependencies from {c2.name} <ArrowRightAlt/> {c1.name}:</Accordion.Header>
                        <Accordion.Body>
                            <ListGroup>
                                { couplingC2C1.map(entity => <ListGroupItem key={entity}>{translateEntity(entity)}</ListGroupItem>) }
                            </ListGroup>
                        </Accordion.Body>
                    </Accordion.Item>
                }
                { couplingC2C1.length <= 0 && <Accordion.Item eventKey="0">
                    <Accordion.Header>No coupling dependencies from {c2.name} <ArrowRightAlt/> {c1.name}</Accordion.Header>
                </Accordion.Item> }
            </Accordion>
        );
    }

    function handleCommonFunctionalitiesBetweenClusters() {
        const edge = network.body.edges[clickedComponent.id];
        const c1 = clusters.find(cluster => "c" + cluster.id === edge.fromId);
        const c2 = clusters.find(cluster => "c" + cluster.id === edge.toId);
        const cluster1Functionalities = clustersFunctionalities[c1.id].map(c => c.name);
        const cluster2Functionalities = clustersFunctionalities[c2.id].map(c => c.name);
        const functionalitiesInCommon = cluster1Functionalities.filter(functionalityName => cluster2Functionalities.includes(functionalityName))

        setTitle("Functionalities that interact with Cluster " + c1.name + " and Cluster " + c2.name);
        setInformationText(
            <ListGroup>
                {functionalitiesInCommon.map(func => <ListGroupItem key={func}>{func}</ListGroupItem>)}
            </ListGroup>
        );
    }

    function handleCommonFunctionalitiesBetweenClusterEntity() {
        const edge = network.body.edges[clickedComponent.id];
        let cluster;
        let entity;
        if (network.clustering.isCluster(edge.fromId)) {
            cluster = clusters.find(c => "c" + c.id === edge.fromId);
            entity = edge.toId;
        }
        else {
            cluster = clusters.find(c => "c" + c.id === edge.toId);
            entity = edge.fromId;
        }
        const entityId = Number(entity.substring(1));

        setTitle("Functionalities that interact with Cluster " + cluster.name + " and entity " + translateEntity(entityId));

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    clustersFunctionalities[cluster.id].map(functionality => {
                        if (functionality.entities[entityId] !== undefined) {
                            return <Accordion.Item eventKey={functionality.name} key={functionality.name}>
                                <Accordion.Header>{functionality.name}</Accordion.Header>
                                <Accordion.Body>
                                    Accesses entity {translateEntity(entityId)} in <i>{ACCESS_TYPES[functionality.entities[entityId]]}</i><br/>
                                    And accesses the following entities of Cluster {cluster.name}:
                                    <ListGroup>
                                        {
                                            functionality.entitiesPerCluster[cluster.id].map(e =>
                                                <ListGroupItem key={e}>{translateEntity(e)} <i>({ACCESS_TYPES[functionality.entities[e]]})</i></ListGroupItem>
                                            )
                                        }
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>;
                        }
                    })
                }
            </Accordion>
        );
    }

    function handleCommonFunctionalitiesBetweenEntities() {
        const edge = network.body.edges[clickedComponent.id];
        const e1ID = Number(edge.fromId.substring(1));
        const e2ID = Number(edge.toId.substring(1));
        const e1Cluster = clusters.find(c => c.id === visGraph.nodes.get(edge.fromId).cid);
        const weights = edgeWeights.find(w => w.e1ID === e1ID && w.e2ID === e2ID);

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    weights.functionalities.map(functionalityName => {
                        const functionality = clustersFunctionalities[e1Cluster.id].find(f => f.name === functionalityName);
                        return <Accordion.Item eventKey={functionalityName} key={functionalityName}>
                            <Accordion.Header>{functionalityName}</Accordion.Header>
                            <Accordion.Body>
                                <ListGroup>
                                    <ListGroupItem> Entity {translateEntity(e1ID)} <i>({ACCESS_TYPES[functionality.entities[e1ID]]})</i> </ListGroupItem>
                                    <ListGroupItem> Entity {translateEntity(e2ID)} <i>({ACCESS_TYPES[functionality.entities[e2ID]]})</i> </ListGroupItem>
                                </ListGroup>
                            </Accordion.Body>
                        </Accordion.Item>;
                    })
                }
            </Accordion>
        );
    }

    function copheneticDistance() {
        const edge = network.body.edges[clickedComponent.id];
        const e1ID = Number(edge.fromId.substring(1));
        const e2ID = Number(edge.toId.substring(1));
        const weights = edgeWeights.find(w => w.e1ID === e1ID && w.e2ID === e2ID);
        return (<Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                Cophenetic Distance: {weights.dist}
            </Button>);
    }

    function isDependencyClusterEntityEdge() {
        const edge = network.body.edges[clickedComponent.id];
        let cluster;
        let entityId;
        if (network.clustering.isCluster(edge.fromId)) {
            cluster = clusters.find(c => "c" + c.id === edge.fromId);
            entityId = Number(edge.toId.substring(1));
        } else {
            cluster = clusters.find(c => "c" + c.id === edge.toId);
            entityId = Number(edge.fromId.substring(1));
        }
        if (Object.values(cluster.couplingDependencies).flatMap(dep => dep).includes(entityId))
            return (<><Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                            Cluster {cluster.name} depends on entity {translateEntity(entityId)}
                        </Button></>);
        else return (<><Button className="flex-grow-1 mt-2" variant="warning" disabled={true}>
                            Cluster {cluster.name} does not depend on entity {translateEntity(entityId)}
                        </Button></>);
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
                    {informationText === undefined && clickedComponent !== undefined && clickedComponent.eventType === "doubleClickCluster" &&
                        <>
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleClusterEntities}>
                                    Cluster's Entities
                                </Button>
                            </div>
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCouplingDependenciesCluster}>
                                    Coupling Dependencies
                                </Button>
                            </div>
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleRelatedFunctionalitiesCluster}>
                                    Related Functionalities
                                </Button>
                            </div>
                            <Divider className="mt-2"></Divider>
                        </>
                    }
                    {informationText === undefined && clickedComponent !== undefined && clickedComponent.eventType === "doubleClickEntity" &&
                        <>
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleRelatedFunctionalitiesEntities}>
                                    Related Functionalities
                                </Button>
                            </div>
                            <div className="d-flex flex-row">
                                <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleRelatedClustersEntities}>
                                    Related Clusters
                                </Button>
                            </div>
                            <Divider className="mt-2"></Divider>
                        </>
                    }
                    {informationText === undefined && clickedComponent !== undefined && clickedComponent.eventType === "doubleClickEdge" &&
                        <>
                            {/*Between clusters*/}
                            {network.clustering.isCluster(network.body.edges[clickedComponent.id].fromId) &&
                             network.clustering.isCluster(network.body.edges[clickedComponent.id].toId) &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCouplingDependenciesFunctionality}>
                                            Coupling Dependencies
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonFunctionalitiesBetweenClusters}>
                                            Functionalities in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {/*Between entities*/}
                            {!network.clustering.isCluster(network.body.edges[clickedComponent.id].fromId) &&
                             !network.clustering.isCluster(network.body.edges[clickedComponent.id].toId) &&
                                <>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonFunctionalitiesBetweenEntities}>
                                            Functionalities in Common
                                        </Button>
                                    </div>
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {/*Between cluster and entity*/}
                            {(!network.clustering.isCluster(network.body.edges[clickedComponent.id].fromId) && network.clustering.isCluster(network.body.edges[clickedComponent.id].toId) ||
                              network.clustering.isCluster(network.body.edges[clickedComponent.id].fromId) && !network.clustering.isCluster(network.body.edges[clickedComponent.id].toId)) &&
                                <>
                                    <div className="d-flex flex-row">
                                        {isDependencyClusterEntityEdge()}
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleCommonFunctionalitiesBetweenClusterEntity}>
                                            Functionalities in Common
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
