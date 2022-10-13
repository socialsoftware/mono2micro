import {Accordion, ListGroup, ListGroupItem, Modal, ModalBody, ModalTitle} from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Divider from "@mui/material/Divider";
import React, {useEffect, useState} from "react";
import {ArrowRightAlt} from "@mui/icons-material";
import CircularProgress from "@mui/material/CircularProgress";
import {EDGE_LENGTH, types} from "../../utils/GraphUtils";

const ACCESS_TYPES = {
    1: "READ",
    2: "WRITE",
    3: "READ/WRITE"
};

export const AccessViewModal = ({clusters, edgeWeights, outdated, clustersFunctionalities, visGraph, showModal, setShowModal, clickedComponent, setClickedComponent}) => {
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
            const fromNode = visGraph.nodes.get(clickedComponent.from);
            const toNode = visGraph.nodes.get(clickedComponent.to);

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

    function handleCouplingDependenciesCluster() {
        setTitle("Dependencies between other clusters");
        const cluster = clusters.find(cluster => cluster.name === clickedComponent.group);
        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    Object.entries(cluster.couplingDependencies).map(([clusterName, couplingDependency]) => {
                        const dependencyCluster = clusters.find(c => c.name === clusterName);
                        return (
                            <Accordion.Item eventKey={dependencyCluster.name} key={dependencyCluster.name}>
                                <Accordion.Header>Depending on the following cluster {dependencyCluster.name} entities:</Accordion.Header>
                                <Accordion.Body>
                                    <ListGroup>
                                        {couplingDependency.map(entityId => {
                                            let entity = dependencyCluster.elements.find(e => e.id === entityId);
                                            return <ListGroupItem key={entity.id}>{entity.name}</ListGroupItem>
                                        })}
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
        setTitle("Functionalities that access cluster " + clickedComponent.label);

        setInformationText(
            <>
                <h4>{clustersFunctionalities[clickedComponent.id].length} Functionalities:</h4>
                <Accordion alwaysOpen={true}>
                    {
                        clustersFunctionalities[clickedComponent.id].map(functionality => {
                            return (
                                <Accordion.Item eventKey={functionality.name} key={functionality.name}>
                                    <Accordion.Header><i>({functionality.type})</i>&ensp;{functionality.name}</Accordion.Header>
                                    <Accordion.Body>
                                        <ListGroup>
                                            {
                                                functionality.entitiesPerCluster[clickedComponent.id].map(entityId => {
                                                    let entity = clickedComponent.elements.find(e => e.id === entityId);
                                                    return <ListGroupItem key={entity.id}>{entity.name}</ListGroupItem>;
                                                })
                                            }
                                        </ListGroup>
                                    </Accordion.Body>
                                </Accordion.Item>
                            );
                        })
                    }
                </Accordion>
            </>
        );
    }

    function handleRelatedFunctionalitiesEntities() {
        setTitle("Functionalities accessing " + clickedComponent.label + " and respective type of access");
        const functionalities = clustersFunctionalities[clickedComponent.group].filter(functionality => functionality.entities[clickedComponent.id]);

        setInformationText(
            <>
                <h4>{functionalities.length} Functionalities:</h4>
                <ListGroup>
                    {
                        functionalities.map(functionality =>
                            <ListGroupItem key={functionality.name}><b>{functionality.name}:</b> {ACCESS_TYPES[functionality.entities[clickedComponent.id]]}</ListGroupItem>)
                    }
                </ListGroup>
            </>
        );
    }

    function handleRelatedClustersEntities() {
        setTitle("Functionalities in common between " + clickedComponent.label + " and respective Clusters");
        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    Object.entries(clustersFunctionalities).map(([clusterName, functionalities]) => {
                        const cluster = clusters.find(c => c.name === clusterName);
                        const filteredFunctionalities = functionalities.filter(functionality => functionality.entities[clickedComponent.id]);
                        if (filteredFunctionalities.length === 0)
                            return undefined;
                        return (
                            <Accordion.Item eventKey={cluster.name} key={cluster.name}>
                                {cluster.elements.find(e => e.id === clickedComponent.id) &&
                                    <Accordion.Header>In common inside own cluster ({cluster.name}):</Accordion.Header>
                                }
                                {!cluster.elements.find(e => e.id === clickedComponent.id) &&
                                    <Accordion.Header>In common with cluster {cluster.name}:</Accordion.Header>
                                }
                                <Accordion.Body>
                                    <ListGroup>
                                        <h5>{filteredFunctionalities.length} Functionalities in common:</h5>
                                        { filteredFunctionalities.map(functionality => <ListGroupItem key={functionality.name}>{functionality.name}</ListGroupItem>) }
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
        const c1 = clusters.find(cluster => cluster.name === clickedComponent.from);
        const c2 = clusters.find(cluster => cluster.name === clickedComponent.to);

        setTitle("Dependencies between Cluster " + c1.name + " and " + c2.name);

        let couplingC1C2 = c1.couplingDependencies[c2.name] === undefined ? [] : c1.couplingDependencies[c2.name];
        let couplingC2C1 = c2.couplingDependencies[c1.name] === undefined ? [] : c2.couplingDependencies[c1.name];

        setInformationText(
            <Accordion alwaysOpen={true}>
                {
                    couplingC1C2.length > 0 &&
                    <Accordion.Item eventKey="0" key="0">
                        <Accordion.Header>Coupling dependencies from {c1.name} <ArrowRightAlt/> {c2.name}:</Accordion.Header>
                        <Accordion.Body>
                            <ListGroup>
                                {
                                    couplingC1C2.map(entityId => {
                                        let entity = c2.elements.find(e => e.id === entityId);
                                        return <ListGroupItem key={entity.id}>{entity.name}</ListGroupItem>;
                                    })
                                }
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
                                {
                                    couplingC2C1.map(entityId => {
                                        let entity = c1.elements.find(e => e.id === entityId);
                                        return <ListGroupItem key={entity.id}>{entity.name}</ListGroupItem>;
                                    })
                                }
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
        const cluster1Functionalities = clustersFunctionalities[clickedComponent.from].map(c => c.name);
        const cluster2Functionalities = clustersFunctionalities[clickedComponent.to].map(c => c.name);
        const functionalitiesInCommon = cluster1Functionalities.filter(functionalityName => cluster2Functionalities.includes(functionalityName))

        setTitle("Functionalities that interact with Cluster " + clickedComponent.from + " and Cluster " + clickedComponent.to);
        setInformationText(
            <>
                <h4>{functionalitiesInCommon.length} functionalities in common:</h4>
                <ListGroup>
                    {functionalitiesInCommon.map(func => <ListGroupItem key={func}>{func}</ListGroupItem>)}
                </ListGroup>
            </>
        );
    }

    function handleCommonFunctionalitiesBetweenClusterEntity() {
        let cluster = visGraph.nodes.get(clickedComponent.from);
        let entity;
        if (cluster.type === types.CLUSTER) {
            entity = visGraph.nodes.get(clickedComponent.to);
        }
        else {
            entity = cluster;
            cluster = visGraph.nodes.get(clickedComponent.to);
        }
        const filteredClustersFunctionalities = clustersFunctionalities[cluster.id].filter(functionality => functionality.entities[entity.id]);

        setTitle("Functionalities that interact with Cluster " + cluster.id + " and entity " + entity.label);

        setInformationText(
            <>
                <h4>{filteredClustersFunctionalities.length} Functionalities in common:</h4>
                <Accordion alwaysOpen={true}>
                    {
                        filteredClustersFunctionalities.map(functionality => {
                            return <Accordion.Item eventKey={functionality.name} key={functionality.name}>
                                <Accordion.Header>{functionality.name}</Accordion.Header>
                                <Accordion.Body>
                                    Accesses entity {entity.label} in <i>{ACCESS_TYPES[functionality.entities[entity.id]]}</i><br/>
                                    And accesses the following entities of Cluster {cluster.id}:
                                    <ListGroup>
                                        {
                                            functionality.entitiesPerCluster[cluster.id].map(e => {
                                                let entity = cluster.elements.find(elem => elem.id === e);
                                                return <ListGroupItem key={entity.id}>{entity.name} <i>({ACCESS_TYPES[functionality.entities[entity.id]]})</i></ListGroupItem>;
                                            }
                                            )
                                        }
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>;
                        })
                    }
                </Accordion>
            </>
        );
    }

    function handleCommonFunctionalitiesBetweenEntities() {
        const e1ID = visGraph.nodes.get(clickedComponent.from);
        const e2ID = visGraph.nodes.get(clickedComponent.to);
        const weights = edgeWeights.find(w => w.e1ID === e1ID.id && w.e2ID === e2ID.id);

        setTitle("Functionalities that interact with Entity " + e1ID.label + " and Entity " + e2ID.label);

        setInformationText(
            <>
                <h4>{weights.functionalities.length} Functionalities in common:</h4>
                <Accordion alwaysOpen={true}>
                    {
                        weights.functionalities.map(functionalityName => {
                            const functionality = clustersFunctionalities[e1ID.group].find(f => f.name === functionalityName);
                            return <Accordion.Item eventKey={functionalityName} key={functionalityName}>
                                <Accordion.Header>{functionalityName}</Accordion.Header>
                                <Accordion.Body>
                                    <ListGroup>
                                        <ListGroupItem> Entity {e1ID.label} <i>({ACCESS_TYPES[functionality.entities[e1ID.id]]})</i> </ListGroupItem>
                                        <ListGroupItem> Entity {e2ID.label} <i>({ACCESS_TYPES[functionality.entities[e2ID.id]]})</i> </ListGroupItem>
                                    </ListGroup>
                                </Accordion.Body>
                            </Accordion.Item>;
                        })
                    }
                </Accordion>
            </>
        );
    }

    function copheneticDistance() {
        return (<Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                {clickedComponent.type !== types.BETWEEN_ENTITIES && "Average"} Cophenetic Distance: {clickedComponent.length/EDGE_LENGTH}
            </Button>);
    }

    function isDependencyClusterEntityEdge() {
        const fromNode = visGraph.nodes.get(clickedComponent.from);
        const toNode = visGraph.nodes.get(clickedComponent.to);
        let cluster;
        let entity;
        if (fromNode.type === types.CLUSTER) {
            cluster = clusters.find(c => c.name === fromNode.id);
            entity = toNode;
        } else {
            cluster = clusters.find(c => c.name === toNode.id);
            entity = fromNode;
        }
        if (Object.values(cluster.couplingDependencies).flatMap(dep => dep).includes(entity.id))
            return (<><Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                            Cluster {cluster.name} depends on entity {entity.label}
                        </Button></>);
        else return (<><Button className="flex-grow-1 mt-2" variant="warning" disabled={true}>
                            Cluster {cluster.name} does not depend on entity {entity.label}
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
                    {(Object.keys(clustersFunctionalities).length === 0 || outdated) &&
                        <div style={{ margin: "auto", textAlign: "center"}}>
                            {outdated && <h2>Waiting for metrics and functionalities to be updated...</h2>}
                            <CircularProgress/>
                        </div>
                    }
                    {Object.keys(clustersFunctionalities).length !== 0 && !outdated && informationText === undefined && clickedComponent !== undefined &&
                        <>
                            {clickedComponent.type === types.CLUSTER &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleClusterEntities}>
                                            Cluster's Entities ({clickedComponent.elements.length} Entities)
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
                            {clickedComponent.type === types.ENTITY &&
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
                            {clickedComponent.type === types.BETWEEN_CLUSTERS &&
                                <>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
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
                            {clickedComponent.type === types.BETWEEN_ENTITIES &&
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
                            {clickedComponent.type === types.BETWEEN_CLUSTER_ENTITY &&
                                <>
                                    <div className="d-flex flex-row">
                                        {copheneticDistance()}
                                    </div>
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