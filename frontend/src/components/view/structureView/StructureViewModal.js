import {Accordion, ListGroup, ListGroupItem, Modal, ModalBody, ModalTitle} from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Divider from "@mui/material/Divider";
import React, {useEffect, useState} from "react";
import {EDGE_LENGTH, types} from "../utils/GraphUtils";

export const StructureViewModal = ({entitiesContained, clusters, showModal, setShowModal, clickedComponent, setClickedComponent, entitySuperClass}) => {
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

            let text = "Edge between ";
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

    function handleEntitiesContainedWithin() {
        setTitle("Entities Contained Within " + clickedComponent.label);
        const entitiesWithin = entitiesContained[clickedComponent.label] || [];

        setInformationText(
            <>
                <h4>{entitiesWithin.length} Entities:</h4>
                <ListGroup>
                    {entitiesWithin.length > 0 ? (
                        entitiesWithin.map(entity => <ListGroupItem key={entity}>{entity}</ListGroupItem>)
                    ) : (
                        <ListGroupItem>No entities within</ListGroupItem>
                    )}
                </ListGroup>
            </>
        );
    }


    function handleClusterAndCluster() {
        const cluster1 = clusters.find(cluster => cluster.name === clickedComponent.from);
        const cluster2 = clusters.find(cluster => cluster.name === clickedComponent.to);

        const cluster1Entities = cluster1.elements.map(element => element.name);
        const cluster2Entities = cluster2.elements.map(element => element.name);

        const cluster1ReferencesCluster2 = cluster1Entities.flatMap(cluster1Entity =>
            entitiesContained[cluster1Entity]?.filter(reference =>
                cluster2Entities.includes(reference) || cluster2Entities.some(entity => reference.startsWith(`${entity} `))
            ).map(reference => ({
                cluster1Entity,
                reference: reference.startsWith(`${cluster2Entities.find(entity => reference.startsWith(`${entity} `))} `) ? reference : reference
            })) || []
        );

        const cluster2ReferencesCluster1 = cluster2Entities.flatMap(cluster2Entity =>
            entitiesContained[cluster2Entity]?.filter(reference =>
                cluster1Entities.includes(reference) || cluster1Entities.some(entity => reference.startsWith(`${entity} `))
            ).map(reference => ({
                cluster2Entity,
                reference: reference.startsWith(`${cluster1Entities.find(entity => reference.startsWith(`${entity} `))} `) ? reference : reference
            })) || []
        );

        setTitle(`References between ${cluster1.name} and ${cluster2.name}`);
        setInformationText(
            <>
                {cluster1ReferencesCluster2.length === 0 && cluster2ReferencesCluster1.length === 0 && <h4>No references found.</h4>}
                {cluster1ReferencesCluster2.length !== 0 &&
                    <>
                        <h4>{cluster1.name} references {cluster2.name}:</h4>
                        <ListGroup>
                            {cluster1ReferencesCluster2.map(({ cluster1Entity, reference }) => (
                                <ListGroupItem key={`${cluster1Entity}-${reference}`}>
                                    {cluster1Entity} references {reference}
                                </ListGroupItem>
                            ))}
                        </ListGroup>
                    </>
                }
                {cluster2ReferencesCluster1.length !== 0 &&
                    <>
                        <h4>{cluster2.name} references {cluster1.name}:</h4>
                        <ListGroup>
                            {cluster2ReferencesCluster1.map(({ cluster2Entity, reference }) => (
                                <ListGroupItem key={`${cluster2Entity}-${reference}`}>
                                    {cluster2Entity} references {reference}
                                </ListGroupItem>
                            ))}
                        </ListGroup>
                    </>
                }
            </>
        );
    }


    function handleClusterAndEntity() {
        const entityLabel = clickedComponent.fromNode ? clickedComponent.fromNode.label : clickedComponent.toNode.label;
        const clusterLabel = clickedComponent.fromNode ? clickedComponent.to : clickedComponent.from;

        const cluster = clusters.find(cluster => cluster.name === clusterLabel);
        const entityReferences = entitiesContained[entityLabel] || [];

        const clusterReferencesEntity = cluster.elements.flatMap(element =>
            entitiesContained[element.name]?.filter(reference =>
                reference === entityLabel || reference.startsWith(`${entityLabel} `)
            ).map(reference => ({
                element: element.name,
                reference
            })) || []
        );

        const entityReferencesCluster = entityReferences.filter(reference =>
            cluster.elements.some(element => reference === element.name || reference.startsWith(`${element.name} `))
        );

        setTitle(`References between ${cluster.name} and ${entityLabel}`);
        setInformationText(
            <>
                {clusterReferencesEntity.length === 0 && entityReferencesCluster.length === 0 && <h4>No references found.</h4>}
                {clusterReferencesEntity.length !== 0 &&
                    <>
                        <h4>{cluster.name} references {entityLabel}:</h4>
                        <ListGroup>
                            {clusterReferencesEntity.map(({ element, reference }) => (
                                <ListGroupItem key={`${element}-${reference}`}>
                                    {element} references {reference.startsWith(`${entityLabel} `) ? `${entityLabel} List` : reference}
                                </ListGroupItem>
                            ))}
                        </ListGroup>
                    </>
                }
                {entityReferencesCluster.length !== 0 &&
                    <>
                        <h4>{entityLabel} references {cluster.name}:</h4>
                        <ListGroup>
                            {entityReferencesCluster.map(reference => {
                                const referencedElement = cluster.elements.find(element => reference === element.name || reference.startsWith(`${element.name} `));
                                return (
                                    <ListGroupItem key={reference}>
                                        {entityLabel} references {reference.startsWith(`${referencedElement.name} `) ? `${referencedElement.name} List` : referencedElement.name}
                                    </ListGroupItem>
                                );
                            })}
                        </ListGroup>
                    </>
                }
            </>
        );
    }

    function handleEntitiesBetweenEntities() {
        const entity1Entities = entitiesContained[clickedComponent.fromNode.label] || [];
        const entity2Entities = entitiesContained[clickedComponent.toNode.label] || [];

        const fromNodeLabel = clickedComponent.fromNode.label;
        const toNodeLabel = clickedComponent.toNode.label;

        const entity1Superclass = entitySuperClass[clickedComponent.fromNode.label] || [];
        const entity2Superclass = entitySuperClass[clickedComponent.toNode.label] || [];

        const entity1SuperclassIsEntity2 = entity1Superclass.includes(toNodeLabel);
        const entity2SuperclassIsEntity1 = entity2Superclass.includes(fromNodeLabel);

        const entity1ReferencesEntity2 = entity1Entities.includes(toNodeLabel) ||
            entity1Entities.some(entity => entity.startsWith(`${toNodeLabel} `));

        const entity2ReferencesEntity1 = entity2Entities.includes(fromNodeLabel) ||
            entity2Entities.some(entity => entity.startsWith(`${fromNodeLabel} `));

        setTitle("Edge between  " + fromNodeLabel + " and " + toNodeLabel);
        setInformationText(
            <>
                {!entity1ReferencesEntity2 && !entity2ReferencesEntity1 && <h4>No references found.</h4>}
                {entity1ReferencesEntity2 &&
                    <>
                        <h4>{fromNodeLabel} references {toNodeLabel}:</h4>
                        <ListGroup>
                            {entity1Entities.filter(entity => entity === toNodeLabel || entity.startsWith(`${toNodeLabel} `)).map(entity =>
                                <ListGroupItem key={entity}>{entity}</ListGroupItem>
                            )}
                        </ListGroup>
                    </>
                }
                {entity2ReferencesEntity1 &&
                    <>
                        <h4>{toNodeLabel} references {fromNodeLabel}:</h4>
                        <ListGroup>
                            {entity2Entities.filter(entity => entity === fromNodeLabel || entity.startsWith(`${fromNodeLabel} `)).map(entity =>
                                <ListGroupItem key={entity}>{entity}</ListGroupItem>
                            )}
                        </ListGroup>
                    </>
                }
                {entity1SuperclassIsEntity2 &&
                    <>
                        <h4>{toNodeLabel} extends {fromNodeLabel}</h4>
                    </>
                }
                {entity2SuperclassIsEntity1 &&
                <>
                    <h4>{fromNodeLabel} extends {toNodeLabel}</h4>
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
                                    <Divider className="mt-2"></Divider>
                                </>
                            }
                            {clickedComponent.type === types.ENTITY &&
                                <>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="success" disabled={true}>
                                            Belongs to: {clickedComponent.group}
                                        </Button>
                                    </div>
                                    <div className="d-flex flex-row">
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleEntitiesContainedWithin}>
                                            Entities Contained Within
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
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleClusterAndCluster}>
                                            Entities Contained Within
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
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleEntitiesBetweenEntities}>
                                            Contained Entities
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
                                        <Button className="flex-grow-1 mt-2" variant="primary" onClick={handleClusterAndEntity}>
                                            Contained Entities
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