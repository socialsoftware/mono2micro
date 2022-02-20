import React, {useContext, useEffect, useState} from 'react';
import {RepositoryService} from '../../services/RepositoryService';
import {VisNetwork} from '../util/VisNetwork';
import { DataSet } from "vis";
import {types, views} from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import CardGroup from 'react-bootstrap/CardGroup';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Row from 'react-bootstrap/Row';
import Card from 'react-bootstrap/Card';
import Col from 'react-bootstrap/Col';
import Container from 'react-bootstrap/Container';

import {FunctionalityRedesignMenu, redesignOperations} from './FunctionalityRedesignMenu';
import {ModalMessage} from "../util/ModalMessage";
import {DEFAULT_REDESIGN_NAME} from "../../constants/constants";
import {TransactionOperationsMenu} from "./TransactionOperationsMenu";
import AppContext from "./../AppContext";
import {useParams} from "react-router-dom";

const HttpStatus = require('http-status-codes');

export const transactionViewHelp = (<div>
    Hover or double click cluster to see entities inside.<br />
    Hover or double click controller to see entities accessed.<br />
    Hover or double click edge to see entities accessed in a cluster.<br />
</div>);

const options = {
    height: "700",
    layout: {
        hierarchical: {
            direction: 'UD',
            nodeSpacing: 120
        }
    },
    edges: {
        smooth: false,
        arrows: {
            to: {
                enabled: true,
            }
        },
        scaling: {
            label: {
                enabled: true
            },
        },
        color: {
            color: "#2B7CE9",
            hover: "#2B7CE9",
            highlight: "#FFA500"
        }
    },
    nodes: {
        shape: 'ellipse',
        color: {
            border: "#2B7CE9",
            background: "#D2E5FF",
            highlight: {
                background: "#FFA500",
                border: "#FFA500"
            }
        }
    },
    interaction: {
        hover: true
    },
    physics: {
        enabled: false
    }
};

const optionsSeq = {
    height: "700",
    layout: {
        hierarchical: {
            direction: 'UD',
            nodeSpacing: 200
        }
    },
    edges: {
        smooth: false,
        arrows: {
            to: {
                enabled: true,
            }
        },
        scaling: {
            label: {
                enabled: true
            },
        },
        color: {
            color: "#2B7CE9",
            hover: "#2B7CE9",
            highlight: "#FFA500"
        }
    },
    nodes: {
        shape: 'ellipse',
        scaling: {
            label: {
                enabled: true
            },
        },
        color: {
            border: "#2B7CE9",
            background: "#D2E5FF",
            highlight: {
                background: "#FFA500",
                border: "#FFA500"
            }
        }
    },
    interaction: {
        hover: true
    },
    physics: {
        enabled: false
    }
};

const optionsFunctionalityRedesign = {
    height: "700",
    layout: {
        hierarchical: false,
        improvedLayout: false
    },
    edges: {
        smooth: false,
        arrows: {
            to: {
                enabled: true,
            }
        },
        scaling: {
            label: {
                enabled: true
            },
        },
        color: {
            color: "#2B7CE9",
            hover: "#2B7CE9",
            highlight: "#FFA500"
        }
    },
    nodes: {
        shape: 'ellipse',
        scaling: {
            label: {
                enabled: true
            },
        },
        color: {
            border: "#2B7CE9",
            background: "#D2E5FF",
            highlight: {
                background: "#FFA500",
                border: "#FFA500"
            }
        }
    },
    interaction: {
        hover: true
    },
    physics: {
        enabled: true,
        solver: 'hierarchicalRepulsion'
    },
};


export const TransactionView = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { codebaseName, dendrogramName, decompositionName } = useParams();

    const [visGraph, setVisGraph] = useState({});
    const [visGraphSeq, setVisGraphSeq] = useState({});
    const [redesignVisGraph, setRedesignVisGraph] = useState({});
    const [clusters, setClusters] = useState([]);
    const [controller, setController] = useState({});
    const [controllers, setControllers] = useState([]);
    const [controllersClusters, setControllersClusters] = useState([]);
    const [showGraph, setShowGraph] = useState(false);
    const [localTransactionsSequence, setLocalTransactionsSequence] = useState([]);
    const [currentSubView, setCurrentSubView] = useState("Graph");
    const [showMenu, setShowMenu] = useState(false);
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [selectedRedesign, setSelectedRedesign] = useState(null);
    const [selectedOperation, setSelectedOperation] = useState(redesignOperations.NONE);
    const [selectedLocalTransaction, setSelectedLocalTransaction] = useState(null);
    const [newCaller, setNewCaller] = useState(null);
    const [modifiedEntities, setModifiedEntities] = useState(null);
    const [DCGISelectedClusters, setDCGISelectedClusters] = useState(null);
    const [DCGIAvailableClusters, setDCGIAvailableClusters] = useState(null);
    const [DCGILocalTransactionsForTheSelectedClusters, setDCGILocalTransactionsForTheSelectedClusters] = useState(null);
    const [DCGISelectedLocalTransactions, setDCGISelectedLocalTransactions] = useState([]);
    const [selectedRedesignsToCompare, setSelectedRedesignsToCompare] = useState(["Select a Redesign", "Select a Redesign"]);
    const [compareRedesigns, setCompareRedesigns] = useState(false);

    useEffect(() => loadTransaction(), []);

    function loadTransaction() {
        const service = new RepositoryService();

        service.getControllersClusters(
            codebaseName,
            dendrogramName,
            decompositionName
        ).then(response => { setControllersClusters(response.data); });

        service.getDecomposition(
            codebaseName,
            dendrogramName,
            decompositionName,
            ["clusters", "controllers"]
        ).then(response => {
            setControllers(Object.values(response.data.controllers));
            setClusters(Object.values(response.data.clusters));
        });
    }

    function handleControllerSubmit(value) {
        const currentController = controllers.find(c => c.name === value);
        setController(currentController);
        loadGraph(currentController);
    }

    function loadGraph(currentController) {
        createTransactionDiagram(currentController);

        const service = new RepositoryService();

        service.getLocalTransactionsGraphForController(
            codebaseName,
            dendrogramName,
            decompositionName,
            currentController.name
        ).then(response => {
            createSequenceDiagram(response.data, currentController);
            setShowGraph(true);
        });
    }

    function createTransactionDiagram(currentController) {
        const visGraph = {
            nodes: new DataSet(controllersClusters[currentController.name].map(cluster => createNode(cluster))),
            edges: new DataSet(controllersClusters[currentController.name].map(cluster => createEdge(cluster, currentController)))
        };

        visGraph.nodes.add({
            id: currentController.name,
            title: Object.entries(currentController.entities).map(e => translateEntity(e[0]) + " " + e[1]).join('<br>') + "<br>Total: " + Object.keys(currentController.entities).length,
            label: currentController.name,
            level: 0,
            value: 1,
            type: types.CONTROLLER
        });

        setVisGraph(visGraph);
    }

    function createNode(cluster) {
        return {
            id: cluster.id,
            title: cluster.entities.map((entityID) => translateEntity(entityID)).join('<br>') + "<br>Total: " + cluster.entities.length,
            label: cluster.name,
            value: cluster.entities,
            level: 1,
            type: types.CLUSTER
        };
    }

    function createEdge(cluster, currentController) {
        const text = [];

        Object.entries(currentController.entities).forEach(([entityID, value]) => {
            if (cluster.entities.includes(Number(entityID)))
                text.push(translateEntity(entityID) + " " + value)
        });

        return {
            from: currentController.name,
            to: cluster.id,
            label: text.length.toString(),
            title: text.join('<br>')
        };
    }

    function createSequenceDiagram(localTransactionsGraph, currentController) {
        let nodes = [];
        let edges = [];
        let localTransactionsSequence = [];
        const localTransactionIdToClusterAccesses = {};

        nodes.push({
            id: 0,
            label: currentController.name,
            level: 0,
            value: 1,
            type: types.CONTROLLER,
            title: Object.entries(currentController.entities)
                .map(e => translateEntity(e[0]) + " " + e[1])
                .join('<br>') + "<br>Total: " + Object.keys(currentController.entities).length,
        });

        localTransactionIdToClusterAccesses[0] = [];

        let {
            nodes: localTransactionsList,
            links: linksList,
        } = localTransactionsGraph;


        for (let i = 1; i < localTransactionsList.length; i++) {

            let {
                id: localTransactionId,
                clusterID,
                clusterAccesses,
            } = localTransactionsList[i];

            localTransactionIdToClusterAccesses[localTransactionId] = clusterAccesses;

            let cluster = clusters.find(cluster => Number(cluster.id) === clusterID);
            const clusterEntityNames = cluster.entities;

            nodes.push({
                id: localTransactionId,
                title: clusterEntityNames.map(entityID => translateEntity(entityID)).join('<br>') + "<br>Total: " + clusterEntityNames.length,
                label: cluster.name,
                value: clusterEntityNames.length,
                level: 1,
                type: types.CLUSTER
            });

            localTransactionsSequence.push({
                id: localTransactionId,
                cluster: cluster.name,
                entities: <pre>{clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('\n')}</pre>
            });
        }

        linksList.forEach(link => {
            const [
                sourceNodeId,
                targetNodeId,
            ] = link.split('->');

            const clusterAccesses = localTransactionIdToClusterAccesses[Number(targetNodeId)];

            edges.push({
                from: Number(sourceNodeId),
                to: Number(targetNodeId),
                title: clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('<br>'),
                label: clusterAccesses.length.toString()
            })

            let sourceNodeIndex;
            let targetNodeIndex;

            for (let i = 0; i < nodes.length; i++) {
                if (nodes[i].id === Number(sourceNodeId)) {
                    sourceNodeIndex = i;
                }

                if (nodes[i].id === Number(targetNodeId)) {
                    targetNodeIndex = i;
                }
                if (sourceNodeIndex !== undefined && targetNodeIndex !== undefined) {
                    nodes[targetNodeIndex].level = nodes[sourceNodeIndex].level + 1;
                }

            }
        });

        const visGraphSeq = {
            nodes: new DataSet(nodes),
            edges: new DataSet(edges)
        };

        setVisGraphSeq(visGraphSeq);
        setLocalTransactionsSequence(localTransactionsSequence);
    }

    function createRedesignGraph(functionalityRedesign, controller){
        let nodes = [];
        let edges = [];

        nodes.push({
            id: 0,
            title: Object.entries(controller.entities).map(e => translateEntity(e[0]) + " " + e[1]).join('<br>') + "<br>Total: " + Object.keys(controller.entities).length,
            label: controller.name,
            level: -1,
            value: 1,
            type: types.CONTROLLER
        });

        functionalityRedesign.redesign.find(e => e.id === 0)
            .remoteInvocations.forEach((id) => {
                const lt = functionalityRedesign.redesign.find(e => e.id === id);
                nodes.push({
                    id: lt.id,
                    title: lt.type,
                    label: lt.name,
                    level: 0,
                    type: types.CLUSTER
                });

                edges.push({
                    from: 0,
                    to: lt.id,
                    title: lt.clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('<br>'),
                    label: lt.clusterAccesses.length.toString()
                });
            });

        for(let i = 0; i < nodes.length; i++){
            if(nodes[i].id > 0) {
                let localTransaction = functionalityRedesign.redesign.find(lt => lt.id === nodes[i].id);
                localTransaction.remoteInvocations.forEach((id) => {
                    let lt = functionalityRedesign.redesign.find(lt => lt.id === id);

                    nodes.push({
                        id: lt.id,
                        title: lt.type,
                        label: lt.name,
                        level: nodes[i].level + 1,
                        type: types.CLUSTER
                    });

                    edges.push({
                        from: nodes[i].id,
                        to: id,
                        title: lt.clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('<br>'),
                        label: lt.clusterAccesses.length.toString()
                    });
                });
            }
        }

        return {
            nodes: new DataSet(nodes),
            edges: new DataSet(edges)
        };
    }

    function identifyModifiedEntities(cluster){
        const modifiedEntities = [];
        Object.entries(controller.entities).forEach(e => {
            if (cluster.entities.includes(parseInt(e[0])) && e[1] >= 2) // 2 -> W , 3 -> RW - we want all writes
                modifiedEntities.push(e[0]);
        })

        return {
            cluster: cluster.id,
            modifiedEntities,
        }
    }


    function handleSelectNode(nodeId) {
        if (currentSubView === "Sequence Graph") return;

        if(compareRedesigns) return;

        if(selectedOperation === redesignOperations.NONE && selectedRedesign) {
            setSelectedLocalTransaction(selectedRedesign.redesign.find(c => c.id === nodeId));
            setShowMenu(true);
            return;
        }

        if(nodeId === -1 && selectedOperation !== redesignOperations.SQ) return;

        if(selectedOperation === redesignOperations.SQ){
            if(nodeId === selectedLocalTransaction.id){
                setError(true);
                setErrorMessage("One local transaction cannot call itself");
            } else if(selectedRedesign.redesign.find(c => c.id === nodeId).remoteInvocations
                .includes(parseInt(selectedLocalTransaction.id))) {
                const lt = selectedRedesign.redesign.find(e => e.id === nodeId);
                setError(true);
                setErrorMessage("The local transaction " + lt.name
                    + " is already invoking local transaction " + selectedLocalTransaction.name);
            } else if(checkTransitiveClosure(nodeId)){
                setError(true);
                setErrorMessage("There cannot exist a cyclic dependency");
            } else {
                setNewCaller(selectedRedesign.redesign.find(c => c.id === nodeId));
            }
        }
        else if(selectedOperation === redesignOperations.DCGI &&
            DCGILocalTransactionsForTheSelectedClusters !== null){
            const localTransaction = selectedRedesign.redesign.find(c => c.id === nodeId);

            if(!DCGISelectedClusters.includes(localTransaction.clusterID))
                return

            if(!DCGISelectedLocalTransactions.map(e => e.id).includes(nodeId)){
                const aux = DCGISelectedLocalTransactions;
                aux.push(DCGILocalTransactionsForTheSelectedClusters.find(e => e.id === nodeId));
                setDCGISelectedLocalTransactions(aux);
            }
        }
    }

    function checkTransitiveClosure(nodeId){
        let transitiveClosure = selectedLocalTransaction.remoteInvocations;

        for(let i = 0; i < transitiveClosure.length; i++) {
            if(transitiveClosure[i] === parseInt(nodeId))
                return true;

            transitiveClosure = transitiveClosure.concat(
                selectedRedesign.redesign.find(
                    e => e.id === transitiveClosure[i]
                ).remoteInvocations
            );
        }
        return false;
    }

    function handleDeselectNode(nodeId) {}

    function changeSubView(value) {
        setCurrentSubView(value);
    }

    function handleSelectOperation(value){
        setSelectedOperation(value);

        if(value === redesignOperations.AC){
            const modifiedEntities = [];

            controllersClusters[controller.name].forEach(cluster => {
                const clusterModifiedEntities = identifyModifiedEntities(cluster);

                if (clusterModifiedEntities.modifiedEntities.length > 0 && clusterModifiedEntities.cluster !== selectedLocalTransaction.cluster)
                    modifiedEntities.push(clusterModifiedEntities);
            })
            setModifiedEntities(modifiedEntities);
        } else if(value === redesignOperations.DCGI) {

            const DCGIAvailableClusters = [];

            controllersClusters[controller.name].forEach(cluster => {
                if (cluster.id !== selectedLocalTransaction.clusterID)
                    DCGIAvailableClusters.push(cluster.id);
            })

            setDCGIAvailableClusters(DCGIAvailableClusters);
            setDCGISelectedClusters([selectedLocalTransaction.clusterID]);
        }
    }

    function closeErrorMessageModal() {
        setError(false);
        setErrorMessage('');
    }

    function handleSubmit(value){
        const service = new RepositoryService();

        switch (selectedOperation) {
            case redesignOperations.AC:
                service.addCompensating(
                    codebaseName,
                    dendrogramName,
                    decompositionName,
                    controller.name,
                    selectedRedesign.name,
                    value.cluster,
                    value.entities,
                    selectedLocalTransaction.id
                )
                    .then(response => {
                        rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.error(err);
                        setError(true);
                        setErrorMessage('ERROR: Add Compensating failed.');
                    });

                break;

            case redesignOperations.SQ:
                service.sequenceChange(
                    codebaseName,
                    dendrogramName,
                    decompositionName,
                    controller.name,
                    selectedRedesign.name,
                    selectedLocalTransaction.id,
                    newCaller.id
                )
                    .then(response => {
                        rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.error(err);
                        setError(true);
                        setErrorMessage('ERROR: Sequence Change failed.');
                });

                break;

            case redesignOperations.DCGI:
                DCGISelectedLocalTransactions.sort((a,b) => {
                    if(a.id <= b.id)
                        return -1;
                    else
                        return 1;
                });
                service.dcgi(
                    codebaseName,
                    dendrogramName,
                    decompositionName,
                    controller.name,
                    selectedRedesign.name,
                    DCGISelectedClusters[0],
                    DCGISelectedClusters[1],
                    JSON.stringify(DCGISelectedLocalTransactions.map(e => e.id))
                )
                    .then(response => {
                        rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.error(err);
                        setError(true);
                        setErrorMessage('ERROR: DCGI failed.');
                });

                break;

            case redesignOperations.PIVOT:
                service.selectPivotTransaction(
                    codebaseName,
                    dendrogramName,
                    decompositionName,
                    controller.name,
                    selectedRedesign.name,
                    selectedLocalTransaction.id,
                    selectedRedesign.pivotTransaction === -1 ? value : null
                )
                    .then(response => {
                        handlePivotTransactionSubmit(response);

                    }).catch(error => {
                        console.error(error.response)

                        if(error.response !== undefined && error.response.status === HttpStatus.FORBIDDEN){
                            setError(true);
                            setErrorMessage('Pivot selection failed - ' + error.response.data);
                        } else {
                            setError(true);
                            setErrorMessage('Pivot selection failed.');
                        }
                    });
                break;

            case redesignOperations.RENAME:
                service.changeLTName(
                    codebaseName,
                    dendrogramName,
                    decompositionName,
                    controller.name,
                    selectedRedesign.name,
                    selectedLocalTransaction.id,
                    value
                )
                    .then(response => {
                        rebuildRedesignGraph(response);
                    })
                    .catch((err) => {
                        console.error(err);
                        setError(true);
                        setErrorMessage('ERROR: Pivot selection failed.');
                    });
                break;

            default:
                break;
        }
    }

    function rebuildRedesignGraph(value){
        const tempControllers = controllers;
        const index = tempControllers.indexOf(controller);
        tempControllers[index] = value.data;
        const redesign = value.data.functionalityRedesigns.find(e => e.name === selectedRedesign.name);

        setControllers(tempControllers);
        setController(value.data);
        setSelectedRedesign(redesign);
        setRedesignVisGraph(createRedesignGraph(redesign, value.data));
        handleCancel();
    }

    function handlePivotTransactionSubmit(value){
        const tempControllers = controllers;
        const index = tempControllers.indexOf(controller);
        tempControllers[index] = value.data;

        setControllers(tempControllers);
        setController(value.data);
        setSelectedRedesign(null);
        handleCancel();
    }

    function handleCancel(){
        setShowMenu(false);
        setSelectedLocalTransaction(null);
        setSelectedOperation(redesignOperations.NONE);
        setNewCaller(null);
        setModifiedEntities(null);
        setDCGIAvailableClusters(null);
        setDCGILocalTransactionsForTheSelectedClusters(null);
        setDCGISelectedLocalTransactions([]);
    }

    function DCGISelectCluster(value){
        const selectedClusters = DCGISelectedClusters;
        //console.log("Maybe this needs a change after distinction between id and name");
        selectedClusters.push(parseInt(value));

        const localTransactionsForTheSelectedClusters =
            selectedRedesign.redesign.filter(e => selectedClusters.includes(e.clusterID));

        setDCGILocalTransactionsForTheSelectedClusters(localTransactionsForTheSelectedClusters);
        setDCGISelectedLocalTransactions([selectedLocalTransaction]);
        setDCGISelectedClusters(selectedClusters);
    }

    function handleDCGISelectLocalTransaction(value){
        if(value === null || value.length === 0){
            setDCGISelectedLocalTransactions([]);
        } else {
            let selectedLocalTransactions = DCGILocalTransactionsForTheSelectedClusters
                .filter(e => value.map(entry => entry.value).includes(e.id));
            setDCGISelectedLocalTransactions(selectedLocalTransactions);
        }
    }

    function renderFunctionalityRedesigns(){
        return <CardGroup style={{ width: "fit-content" }}>
            {controller.functionalityRedesigns.map(fr =>
                <Card className="mb-4" key={fr.name} style={{ width: "30rem" }}>
                    <Card.Body>
                        {fr.usedForMetrics ? <Card.Title>
                                {fr.name + " (Used For Metrics)"}
                            </Card.Title> :
                            <Card.Title>
                                {fr.name}
                            </Card.Title>
                        }
                        {controller.type === "QUERY" ?
                            <Card.Text>
                                Type: Query <br/>
                                Inconsistency Complexity: {fr.inconsistencyComplexity}
                            </Card.Text>
                            :
                            <Card.Text>
                                Type: Saga <br/>
                                Functionality Complexity: {fr.functionalityComplexity}< br/>
                                System Complexity: {fr.systemComplexity}
                            </Card.Text>
                        }
                        <Button onClick={() => handleSelectRedesign(fr)} className="me-2">
                            {fr.name === DEFAULT_REDESIGN_NAME ? "Create a new Redesign" : "Go to Redesign"}
                        </Button>
                        <Button onClick={() => handleUseForMetrics(fr)} className="me-2" disabled={fr.usedForMetrics}>
                            Use For Metrics
                        </Button>
                        <Button onClick={() => handleDeleteRedesign(fr)} variant="danger" className="me-2" disabled={fr.name === DEFAULT_REDESIGN_NAME}>
                            Delete
                        </Button>
                    </Card.Body>
                </Card>
            )}
        </CardGroup>
    }

    function handleUseForMetrics(value){
        const service = new RepositoryService();
        service.setUseForMetrics(codebaseName, dendrogramName, decompositionName,
            controller.name, value.name)
            .then(response => {
                const tempControllers = controllers;
                const index = tempControllers.indexOf(controller);
                tempControllers[index] = response.data;
                setControllers(tempControllers);
                setController(response.data);
            }).catch(() => {
                setError(true);
                setErrorMessage('ERROR: Change Functionality Used for Metrics failed.');
            }
        );
    }

    function handleSelectRedesign(value){
        setSelectedRedesign(value);
        setRedesignVisGraph(createRedesignGraph(value, controller));
    }

    function handleDeleteRedesign(value){
        const service = new RepositoryService();
        service.deleteRedesign(codebaseName, dendrogramName, decompositionName,
            controller.name, value.name)
            .then(response => {
                const tempControllers = controllers;
                const index = tempControllers.indexOf(controller);
                tempControllers[index] = response.data;
                setControllers(tempControllers);
                setController(response.data);
            }).catch(() => {
                setError(true);
                setErrorMessage('ERROR: Delete Redesign failed.');
            }
        );
    }

    function renderRedesignGraph(graph){
        return <div>
            <div style={{display:'none'}}>
                {/*this div functions as a "cache". Is is used to render the graph with the optionsSeq
                                     options in order to save the positions such that when the graph is generated with the
                                     optionsFunctionalityRedesign options is much quicker and there is no buffering*/}
                <VisNetwork
                    visGraph={graph}
                    options={optionsSeq}
                    onSelection={handleSelectNode}
                    onDeselection={handleDeselectNode}
                    view={views.TRANSACTION} />
            </div>
            <div style={{height: '700px'}}>
                <VisNetwork
                    visGraph={graph}
                    options={optionsFunctionalityRedesign}
                    onSelection={handleSelectNode}
                    onDeselection={handleDeselectNode}
                    view={views.TRANSACTION} />
            </div>
        </div>
    }

    function setComparingRedesign(index, name){
        const selectedRedesigns = selectedRedesignsToCompare;
        selectedRedesigns[index] = name;
        setSelectedRedesignsToCompare(selectedRedesigns);
    }

    const metricsRows = controllers.map(controller => {
        return controller.type === "QUERY" ?
            {
                controller: controller.name,
                clusters: controllersClusters[controller.name] === undefined ? 0 : controllersClusters[controller.name].length,
                type: controller.type,
                complexity: controller.complexity,
                inconsistencyComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).inconsistencyComplexity
            }
            :
            {
                controller: controller.name,
                clusters: controllersClusters[controller.name] === undefined ? 0 : controllersClusters[controller.name].length,
                type: controller.type,
                complexity: controller.complexity,
                functionalityComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).functionalityComplexity,
                systemComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).systemComplexity,
                total: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).functionalityComplexity + controller.functionalityRedesigns.find(fr => fr.usedForMetrics).systemComplexity
            }
    });

    const metricsColumns = [{
        dataField: 'controller',
        text: 'Controller',
        sort: true
    }, {
        dataField: 'clusters',
        text: '# of Clusters Accessed',
        sort: true
    }, {
        dataField: 'complexity',
        text: 'Complexity',
        sort: true
    }, {
        dataField: 'type',
        text: 'Type',
        sort: true
    }, {
        dataField: 'functionalityComplexity',
        text: 'Functionality Complexity',
        sort: true
    }, {
        dataField: 'systemComplexity',
        text: 'System Complexity',
        sort: true
    }, {
        dataField: 'total',
        text: 'Total',
        sort: true
    },  {
        dataField: 'inconsistencyComplexity',
        text: 'Query Inconsistency Complexity',
        sort: true
    }];
    const seqColumns = [{
        dataField: 'id',
        text: 'Order'
    }, {
        dataField: 'cluster',
        text: 'Cluster Accessed'
    }, {
        dataField: 'entities',
        text: 'Entities Accessed'
    }];

    let controllersClustersAmount = Object.keys(controllersClusters).map(controller => controllersClusters[controller].length);
    let averageClustersAccessed = controllersClustersAmount.reduce((a, b) => a + b, 0) / controllersClustersAmount.length;

    return (
        <div>
            {
                error && (
                    <ModalMessage
                        title='Error Message'
                        message={errorMessage}
                        onClose={closeErrorMessageModal}
                    />
                )
            }
            <Container fluid>
                <Row>
                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                        <ButtonGroup className="mb-2">
                            <Button
                                disabled={currentSubView === "Graph"}
                                onClick={() => changeSubView("Graph")}
                            >
                                Graph
                            </Button>
                            <Button
                                disabled={currentSubView === "Sequence Graph"}
                                onClick={() => changeSubView("Sequence Graph")}
                            >
                                Sequence Graph
                            </Button>
                            <Button
                                disabled={currentSubView === "Metrics"}
                                onClick={() => changeSubView("Metrics")}
                            >
                                Metrics
                            </Button>
                            <Button
                                disabled={currentSubView === "Sequence Table"}
                                onClick={() => changeSubView("Sequence Table")}
                            >
                                Sequence Table
                            </Button>
                            <Button
                                disabled={currentSubView === "Functionality Redesign"}
                                onClick={() => changeSubView("Functionality Redesign")}
                            >
                                Functionality Redesign
                            </Button>
                        </ButtonGroup>
                    </Col>
                    {selectedRedesign !== null &&
                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                        {showGraph && currentSubView === "Functionality Redesign" &&
                        controller.type === "SAGA" &&
                        <h4 style={{color: "#666666", textAlign: "center"}}>
                            Functionality Complexity: {selectedRedesign.functionalityComplexity} - System Complexity: {selectedRedesign.systemComplexity}
                        </h4>
                        }
                        {showGraph && currentSubView === "Functionality Redesign" &&
                        controller.type === "QUERY" &&
                        <h4 style={{color: "#666666", textAlign: "center"}}>
                            Query Inconsistency Complexity: {selectedRedesign.inconsistencyComplexity}
                        </h4>
                        }
                    </Col>
                    }
                </Row>
            </Container>
            {currentSubView === "Graph" &&
            <span>
                    <TransactionOperationsMenu
                        handleControllerSubmit={handleControllerSubmit}
                        controllersClusters={controllersClusters}
                    />
                    <div style={{height: '700px'}}>
                        <VisNetwork
                            visGraph={visGraph}
                            options={options}
                            onSelection={handleSelectNode}
                            onDeselection={handleDeselectNode}
                            view={views.TRANSACTION} />
                    </div>
                </span>
            }
            {currentSubView === "Sequence Graph" &&
            <div style={{height: '700px'}}>
                <VisNetwork
                    visGraph={visGraphSeq}
                    options={optionsSeq}
                    onSelection={handleSelectNode}
                    onDeselection={handleDeselectNode}
                    view={views.TRANSACTION}
                />
            </div>
            }
            {currentSubView === "Metrics" &&
            <div>
                Number of Clusters : {clusters.length}
                < br />
                Number of Controllers that access a single Cluster : {Object.keys(controllersClusters).filter(key => controllersClusters[key].length === 1).length}
                < br />
                Maximum number of Clusters accessed by a single Controller : {Math.max(...Object.keys(controllersClusters).map(key => controllersClusters[key].length))}
                < br />
                Average Number of Clusters accessed (Average number of microservices accessed during a transaction) : {Number(averageClustersAccessed.toFixed(2))}
                <BootstrapTable
                    bootstrap4
                    keyField='controller'
                    data={metricsRows}
                    columns={metricsColumns}
                />
            </div>
            }
            {showGraph && currentSubView === "Sequence Table" &&
            <>
                <h4>{controller.name}</h4>
                <BootstrapTable
                    bootstrap4
                    keyField='id'
                    data={localTransactionsSequence}
                    columns={seqColumns}
                />
            </>
            }
            {showGraph && currentSubView === "Functionality Redesign" && selectedRedesign === null &&
            !compareRedesigns &&
            <div>
                <br/>
                <h4 style={{color: "#666666"}}>{controller.name} Redesigns</h4>
                {controller.functionalityRedesigns.length >= 2 &&
                <ButtonGroup className="mb-2">
                    <Button>Compare Two Redesigns</Button>
                    <DropdownButton as={ButtonGroup}
                                    title={selectedRedesignsToCompare[0]}>
                        {controller.functionalityRedesigns.map(e =>
                            <Dropdown.Item
                                key={e.name}
                                onSelect={() => setComparingRedesign(0, e.name)}>{e.name}
                            </Dropdown.Item>)}
                    </DropdownButton>
                    <DropdownButton as={ButtonGroup}
                                    title={selectedRedesignsToCompare[1]}>
                        {controller.functionalityRedesigns.filter(e => selectedRedesignsToCompare[0] !== e.name).map(e =>
                            <Dropdown.Item
                                key={e.name}
                                onSelect={() => setComparingRedesign(1, e.name)}>{e.name}
                            </Dropdown.Item>)}
                    </DropdownButton>
                    <Button onClick={() => setCompareRedesigns(true)}>Submit</Button>
                </ButtonGroup>
                }
                <br/>
                <br/>
                {renderFunctionalityRedesigns()}
            </div>
            }
            {showGraph && currentSubView === "Functionality Redesign" && selectedRedesign === null &&
            compareRedesigns &&
            <div>
                <Button className="mb-2"
                        onClick={() => {setCompareRedesigns(false); setSelectedRedesignsToCompare(["Select a Redesign", "Select a Redesign"]);}}>
                    Back
                </Button>
                <Container fluid>
                    <Row>
                        <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                            {controller.type === "SAGA" ?
                                <div>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        {selectedRedesignsToCompare[0]}
                                    </h4>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        Functionality Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[0])[0].functionalityComplexity} - System Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[0])[0].systemComplexity}
                                    </h4>
                                </div>
                                :
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Query Inconsistency Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[0])[0].inconsistencyComplexity}
                                </h4>
                            }
                            {renderRedesignGraph(createRedesignGraph(controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[0])[0], controller))}
                        </Col>
                        <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                            {controller.type === "SAGA" ?
                                <div>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        {selectedRedesignsToCompare[1]}
                                    </h4>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        Functionality Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[1])[0].functionalityComplexity} - System Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[1])[0].systemComplexity}
                                    </h4>
                                </div>
                                :
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Query Inconsistency Complexity: {controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[1])[0].inconsistencyComplexity}
                                </h4>
                            }
                            {renderRedesignGraph(createRedesignGraph(controller.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare[1])[0], controller))}
                        </Col>
                    </Row>
                </Container>
            </div>
            }
            {showGraph && currentSubView === "Functionality Redesign" && selectedRedesign !== null &&
            <Container fluid>
                <Row>
                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                        <Button className="mb-2"
                                onClick={() => {setSelectedRedesign(null); handleCancel();}}>
                            Back
                        </Button>
                        {showMenu &&
                        <FunctionalityRedesignMenu
                            selectedRedesign = {selectedRedesign}
                            selectedLocalTransaction = {selectedLocalTransaction}
                            newCaller = {newCaller}
                            modifiedEntities = {modifiedEntities}
                            DCGIAvailableClusters = {DCGIAvailableClusters}
                            DCGILocalTransactionsForTheSelectedClusters = {DCGILocalTransactionsForTheSelectedClusters}
                            DCGISelectedLocalTransactions = {DCGISelectedLocalTransactions}
                            handleSelectOperation = {handleSelectOperation}
                            handleCancel = {handleCancel}
                            handleSubmit = {handleSubmit}
                            DCGISelectCluster = {DCGISelectCluster}
                            handleDCGISelectLocalTransaction = {handleDCGISelectLocalTransaction}
                        />
                        }
                    </Col>
                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                        {renderRedesignGraph(redesignVisGraph)}
                    </Col>
                </Row>
            </Container>
            }
        </div>
    );
}
