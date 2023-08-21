import React, {useContext, useEffect, useState} from 'react';
import {APIService} from '../../../../services/APIService';
import {VisNetwork} from '../../utils/VisNetwork';
import { DataSet } from "vis-network/standalone";
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
import {ModalMessage} from "../../utils/ModalMessage";
import {DEFAULT_REDESIGN_NAME, ENTITY_ACCESS_TYPE} from "../../../../constants/constants";
import {FunctionalityOperationsMenu} from "./FunctionalityOperationsMenu";
import AppContext from "../../../AppContext";
import {useParams} from "react-router-dom";
import {MetricType} from "../../../../type-declarations/types";
import {searchType} from "../../utils/ViewSearchBar";
import {Analytics, BubbleChart, Edit, Hub, List, Search} from "@mui/icons-material";
import {types} from "../../utils/GraphUtils";
import {views} from "../AccessesViews";

const HttpStatus = require('http-status-codes');

export const functionalityViewHelp = (<div>
    Hover or double click cluster to see entities inside.<br />
    Hover or double click functionality to see entities accessed.<br />
    Hover or double click edge to see entities accessed in a cluster.<br />
</div>);

const options = {
    height: "890",
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
    height: "890",
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

const optionsFunctionalityRedesignCompare = {
    height: "678",
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
        enabled: true,
        solver: 'hierarchicalRepulsion'
    },
};

const optionsFunctionalityRedesign = {
    height: "890",
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
        enabled: true,
        solver: 'hierarchicalRepulsion'
    },
};


export const FunctionalityView = ({searchedItem, setSearchedItem, outdated, setOutdated, changeToClusters, setOpenSearch, setActions, view}) => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { decompositionName } = useParams();

    const [functionalities, setFunctionalities] = useState(undefined);
    const [visGraph, setVisGraph] = useState({});
    const [redesignVisGraph, setRedesignVisGraph] = useState({});
    const [functionality, setFunctionality] = useState({});
    const [functionalitiesClusters, setFunctionalitiesClusters] = useState({});
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
    const [selectedRedesignsToCompare1, setSelectedRedesignsToCompare1] = useState("Select a Redesign");
    const [selectedRedesignsToCompare2, setSelectedRedesignsToCompare2] = useState("Select a Redesign");
    const [compareRedesigns, setCompareRedesigns] = useState(false);

    const defaultActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <Hub/>, name: 'Go to Clusters', handler: changeToClusters },
    ];

    const completeActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <Hub/>, name: 'Go to Clusters', handler: changeToClusters },
        { icon: <BubbleChart/>, name: 'Functionality Graph', handler: () => changeSubView("Graph") },
        { icon: <Analytics/>, name: 'Metrics', handler: () => changeSubView("Metrics") },
        { icon: <List/>, name: 'Sequence Table', handler: () => changeSubView("Sequence Table") },
        { icon: <Edit/>, name: 'Functionality Redesign', handler: () => changeSubView("Functionality Redesign") },
    ];

    useEffect(() => {
        if (outdated)
            setFunctionalities(undefined);
    }, [outdated]);

    useEffect(() => {
        if (!outdated && functionalities && searchedItem !== undefined && view === views.FUNCTIONALITY) {
            handleFunctionalitySubmit(searchedItem.name);
            setSearchedItem(undefined);
        }
    }, [outdated, functionalities]);

    useEffect(() => {
        if (view === views.FUNCTIONALITY) {
            if ((outdated || !functionalities) && searchedItem === undefined)
                loadFunctionalitiesAndFunctionalitiesClusters();

            if (Object.keys(functionality).length === 0) // Set the options in the speed dial according to a functionality
                setActions(defaultActions);
            else setActions(completeActions);
        }
    }, [view, functionality]);

    useEffect(() => {
        if (searchedItem !== undefined && searchedItem.type === searchType.FUNCTIONALITY) {
            if (!functionalities || outdated) { // Will update and only then will search
                loadFunctionalitiesAndFunctionalitiesClusters();
            }
            else { // Otherwise updates
                handleFunctionalitySubmit(searchedItem.id);
                setSearchedItem(undefined);
            }
        }
    },[searchedItem]);

    function loadFunctionalitiesAndFunctionalitiesClusters() {
        const service = new APIService();

        service.getFunctionalitiesAndFunctionalitiesClusters(decompositionName).then(response => {
            setFunctionalitiesClusters(response.data.functionalitiesClusters);
            setFunctionalities(Object.values(response.data.functionalities));
            setOutdated(false);
        }).catch((error) => console.error("Error while fetching functionalities and functionalities clusters:", error));
    }

    function handleFunctionalitySubmit(value) {
        const currentFunctionality = functionalities.find(c => c.name === value);
        setFunctionality(currentFunctionality);
        setSelectedRedesign(null);
        handleCancel();
        loadGraph(currentFunctionality);
    }

    function loadGraph(currentFunctionality) {
        createTransactionDiagram(currentFunctionality);

        const service = new APIService();

        service.getLocalTransactionsGraphForFunctionality(
            decompositionName,
            currentFunctionality.name
        ).then(response => {
            createSequenceDiagram(response.data, currentFunctionality);
            setShowGraph(true);
        });
    }

    function createTransactionDiagram(currentFunctionality) {
        const visGraph = {
            nodes: new DataSet(functionalitiesClusters[currentFunctionality.name].map(cluster => createNode(cluster))),
            edges: new DataSet(functionalitiesClusters[currentFunctionality.name].map(cluster => createEdge(cluster, currentFunctionality)))
        };

        visGraph.nodes.add({
            id: currentFunctionality.name,
            title: Object.entries(currentFunctionality.entities).map(e => translateEntity(e[0]) + " " + e[1]).join('\n') + "\nTotal: " + Object.keys(currentFunctionality.entities).length,
            label: currentFunctionality.name,
            level: 0,
            value: 1,
            type: types.FUNCTIONALITY
        });

        setVisGraph(visGraph);
    }

    function createNode(cluster) {
        return {
            id: cluster.name,
            title: cluster.elements.map((entity) => entity.name).join('\n') + "\nTotal: " + cluster.elements.length,
            label: cluster.name,
            value: cluster.elements,
            level: 1,
            type: types.CLUSTER
        };
    }

    function createEdge(cluster, currentFunctionality) {
        const text = [];

        Object.entries(currentFunctionality.entities).forEach(([entityID, value]) => {
            if (cluster.elements.find(e => e.id === Number(entityID)))
                text.push(translateEntity(entityID) + " " + value)
        });

        return {
            from: currentFunctionality.name,
            to: cluster.name,
            label: text.length.toString(),
            title: text.join('\n')
        };
    }

    function createSequenceDiagram(localTransactionsGraph, currentFunctionality) {
        let localTransactionsSequence = [];

        localTransactionsGraph.nodes.forEach(node => {
            if (node.clusterName === "-1") return;
            let cluster = functionalitiesClusters[currentFunctionality.name].find(cluster => cluster.name === node.clusterName);

            localTransactionsSequence.push({
                id: node.id,
                cluster: cluster.name,
                entities: <pre>{node.clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('\n')}</pre>
            });
        })

        setLocalTransactionsSequence(localTransactionsSequence);
    }

    function createRedesignGraph(functionalityRedesign, functionality){
        let nodes = [];
        let edges = [];

        nodes.push({
            id: 0,
            title: Object.entries(functionality.entities).map(e => translateEntity(e[0]) + " " + e[1]).join('\n') + "\nTotal: " + Object.keys(functionality.entities).length,
            label: functionality.name,
            level: -1,
            value: 1,
            type: types.FUNCTIONALITY
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
                    title: lt.clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('\n'),
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
                        title: lt.clusterAccesses.map(acc => `${acc[0]} ${translateEntity(acc[1])} ${acc[2] ?? ""}`).join('\n'),
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
        Object.entries(functionality.entities).forEach(e => {
            let entityId = parseInt(e[0]);
            if (cluster.elements.find(e1 => e1.id === entityId) && e[1] > ENTITY_ACCESS_TYPE.READ) // we want all writes
                modifiedEntities.push(e[0]);
        })

        return {
            cluster: cluster.name,
            modifiedEntities,
        }
    }


    function handleSelectNode(nodeId) {
        let selectedOperation;
        setSelectedOperation(prev => {selectedOperation = prev; return prev});

        if(compareRedesigns) return;

        if(selectedOperation === redesignOperations.NONE && selectedRedesign) {
            setSelectedLocalTransaction(selectedRedesign.redesign.find(c => c.id === nodeId));
            setShowMenu(true);
            return;
        }

        if(nodeId === -1 && selectedOperation !== redesignOperations.SQ) return;

        if(selectedOperation === redesignOperations.SQ){
            let selectedLocalTransaction;
            setSelectedLocalTransaction(prev => {selectedLocalTransaction = prev; return prev});
            if(nodeId === selectedLocalTransaction.id){
                setError(true);
                setErrorMessage("One local transaction cannot call itself");
            } else if(selectedRedesign.redesign.find(c => c.id === nodeId).remoteInvocations
                .includes(parseInt(selectedLocalTransaction.id))) {
                const lt = selectedRedesign.redesign.find(e => e.id === nodeId);
                setError(true);
                setErrorMessage("The local transaction " + lt.name
                    + " is already invoking local transaction " + selectedLocalTransaction.name);
            } else if(checkTransitiveClosure(nodeId, selectedLocalTransaction)){
                setError(true);
                setErrorMessage("There cannot exist a cyclic dependency");
            } else {
                setNewCaller(selectedRedesign.redesign.find(c => c.id === nodeId));
            }
        }
        else if(selectedOperation === redesignOperations.DCGI &&
            DCGILocalTransactionsForTheSelectedClusters !== null){
            const localTransaction = selectedRedesign.redesign.find(c => c.id === nodeId);

            if(!DCGISelectedClusters.includes(localTransaction.clusterName))
                return

            if(!DCGISelectedLocalTransactions.map(e => e.id).includes(nodeId)){
                const aux = DCGISelectedLocalTransactions;
                aux.push(DCGILocalTransactionsForTheSelectedClusters.find(e => e.id === nodeId));
                setDCGISelectedLocalTransactions(aux);
            }
        }
    }

    function checkTransitiveClosure(nodeId, selectedLocalTransaction){
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

            functionalitiesClusters[functionality.name].forEach(cluster => {
                const clusterModifiedEntities = identifyModifiedEntities(cluster);

                if (clusterModifiedEntities.modifiedEntities.length > 0 && clusterModifiedEntities.cluster !== selectedLocalTransaction.cluster)
                    modifiedEntities.push(clusterModifiedEntities);
            })
            setModifiedEntities(modifiedEntities);
        } else if(value === redesignOperations.DCGI) {

            const DCGIAvailableClusters = [];

            functionalitiesClusters[functionality.name].forEach(cluster => {
                if (cluster.name !== selectedLocalTransaction.clusterName)
                    DCGIAvailableClusters.push(cluster.name);
            })

            setDCGIAvailableClusters(DCGIAvailableClusters);
            setDCGISelectedClusters([selectedLocalTransaction.clusterName]);
        }
    }

    function closeErrorMessageModal() {
        setError(false);
        setErrorMessage('');
    }

    function handleSubmit(value){
        const service = new APIService();

        switch (selectedOperation) {
            case redesignOperations.AC:
                service.addCompensating(
                    decompositionName,
                    functionality.name,
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
                    decompositionName,
                    functionality.name,
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
                    decompositionName,
                    functionality.name,
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
                    decompositionName,
                    functionality.name,
                    selectedRedesign.name,
                    selectedLocalTransaction.id,
                    selectedRedesign.pivotTransaction === -1 ? value : null
                )
                    .then(response => {
                        handlePivotTransactionSubmit(response);

                    }).catch(error => {
                        console.error(error.response)

                        if(error.response.status === HttpStatus.FORBIDDEN){
                            setError(true);
                            setErrorMessage('Pivot selection failed - Name already used for another functionalityRedesign');
                        } else {
                            setError(true);
                            setErrorMessage('Pivot selection failed.');
                        }
                    });
                break;

            case redesignOperations.RENAME:
                service.changeLTName(
                    decompositionName,
                    functionality.name,
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
        const tempFunctionalities = functionalities;
        const index = tempFunctionalities.indexOf(functionality);
        tempFunctionalities[index] = value.data;
        const redesign = value.data.functionalityRedesigns.find(e => e.name === selectedRedesign.name);

        setFunctionalities(tempFunctionalities);
        setFunctionality(value.data);
        setSelectedRedesign(redesign);
        setRedesignVisGraph(createRedesignGraph(redesign, value.data));
        handleCancel();
    }

    function handlePivotTransactionSubmit(value){
        const tempFunctionalities = functionalities;
        const index = tempFunctionalities.indexOf(functionality);
        tempFunctionalities[index] = value.data;

        setFunctionalities(tempFunctionalities);
        setFunctionality(value.data);
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
        selectedClusters.push(value);

        const localTransactionsForTheSelectedClusters =
            selectedRedesign.redesign.filter(e => selectedClusters.includes(e.clusterName));

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
            {functionality.functionalityRedesigns.map(fr =>
                <Card className="me-4 mb-4" key={fr.name} style={{ width: "30rem" }}>
                    <Card.Body>
                        {functionality.functionalityRedesignNameUsedForMetrics === fr.name ? <Card.Title>
                                {fr.name + " (Used For Metrics)"}
                            </Card.Title> :
                            <Card.Title>
                                {fr.name}
                            </Card.Title>
                        }
                        {functionality.type === "QUERY" ?
                            <Card.Text>
                                Type: Query <br/>
                                Inconsistency Complexity: {fr.metrics[MetricType.INCONSISTENCY_COMPLEXITY]}
                            </Card.Text>
                            :
                            <Card.Text>
                                Type: Saga <br/>
                                Functionality Complexity: {fr.metrics[MetricType.FUNCTIONALITY_COMPLEXITY]}< br/>
                                System Complexity: {fr.metrics[MetricType.SYSTEM_COMPLEXITY]}
                            </Card.Text>
                        }
                        <Button onClick={() => handleSelectRedesign(fr)} className="me-2">
                            {fr.name === DEFAULT_REDESIGN_NAME ? "Create a new Redesign" : "Go to Redesign"}
                        </Button>
                        <Button onClick={() => handleUseForMetrics(fr)} className="me-2" disabled={functionality.functionalityRedesignNameUsedForMetrics === fr.name}>
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
        const service = new APIService();
        service.setUseForMetrics(decompositionName, functionality.name, value.name)
            .then(response => {
                const tempFunctionalities = functionalities;
                const index = tempFunctionalities.indexOf(functionality);
                tempFunctionalities[index] = response.data;
                setFunctionalities(tempFunctionalities);
                setFunctionality(response.data);
            }).catch(() => {
                setError(true);
                setErrorMessage('ERROR: Change Functionality Used for Metrics failed.');
            }
        );
    }

    function handleSelectRedesign(value){
        setSelectedRedesign(value);
        setRedesignVisGraph(createRedesignGraph(value, functionality));
    }

    function handleDeleteRedesign(value){
        const service = new APIService();
        service.deleteRedesign(decompositionName, functionality.name, value.name)
            .then(response => {
                const tempFunctionalities = functionalities;
                const index = tempFunctionalities.indexOf(functionality);
                tempFunctionalities[index] = response.data;
                setFunctionalities(tempFunctionalities);
                setFunctionality(response.data);
            }).catch(() => {
                setError(true);
                setErrorMessage('ERROR: Delete Redesign failed.');
            }
        );
    }

    function renderRedesignGraph(graph, options){
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
                    view={views.FUNCTIONALITY} />
            </div>
            <div>
                <VisNetwork
                    visGraph={graph}
                    options={options}
                    onSelection={handleSelectNode}
                    onDeselection={handleDeselectNode}
                    view={views.FUNCTIONALITY} />
            </div>
        </div>
    }

    const metricsRows = functionalities? functionalities.map(functionality => {
        let metrics = functionality.functionalityRedesigns.find(fr => functionality.functionalityRedesignNameUsedForMetrics === fr.name).metrics;
        return functionality.type === "QUERY" ?
            {
                functionality: functionality.name,
                clusters: functionalitiesClusters[functionality.name] === undefined ? 0 : functionalitiesClusters[functionality.name].length,
                type: functionality.type,
                complexity: functionality.metrics[MetricType.COMPLEXITY],
                inconsistencyComplexity: metrics[MetricType.INCONSISTENCY_COMPLEXITY]
            }
            :
            {
                functionality: functionality.name,
                clusters: functionalitiesClusters[functionality.name] === undefined ? 0 : functionalitiesClusters[functionality.name].length,
                type: functionality.type,
                complexity: functionality.metrics[MetricType.COMPLEXITY],
                functionalityComplexity: metrics[MetricType.FUNCTIONALITY_COMPLEXITY],
                systemComplexity: metrics[MetricType.SYSTEM_COMPLEXITY],
                total: metrics[MetricType.FUNCTIONALITY_COMPLEXITY] + metrics[MetricType.SYSTEM_COMPLEXITY]
            }
    }) : undefined;

    const metricsColumns = [{
        dataField: 'functionality',
        text: 'Functionality',
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

    let functionalitiesClustersAmount = Object.keys(functionalitiesClusters).map(functionality => functionalitiesClusters[functionality].length);
    let averageClustersAccessed = functionalitiesClustersAmount.reduce((a, b) => a + b, 0) / functionalitiesClustersAmount.length;
    let numberOfClusters = [...new Set(Object.values(functionalitiesClusters).flatMap(clusters => clusters.map(cluster => cluster.name)))].length;

    return (
        <div>
            <ModalMessage
                show={error}
                setShow={setError}
                title='Error Message'
                message={errorMessage}
                onClose={closeErrorMessageModal}
            />
            {currentSubView === "Graph" &&
            <span>
                    <FunctionalityOperationsMenu
                        handleFunctionalitySubmit={handleFunctionalitySubmit}
                        functionalitiesClusters={functionalitiesClusters}
                    />
                    <VisNetwork
                        visGraph={visGraph}
                        options={options}
                        onSelection={handleSelectNode}
                        onDeselection={handleDeselectNode}
                        view={views.FUNCTIONALITY}
                    />
                </span>
            }
            {currentSubView === "Metrics" &&
            <div style={{marginTop: "3rem", marginLeft: "0.5rem"}}>
                Number of Clusters : {numberOfClusters}
                < br />
                Number of Functionalities that access a singleton Cluster : {Object.keys(functionalitiesClusters).filter(key => functionalitiesClusters[key].length === 1).length}
                < br />
                Maximum number of Clusters accessed by a single Functionality : {Math.max(...Object.keys(functionalitiesClusters).map(key => functionalitiesClusters[key].length))}
                < br />
                Average Number of Clusters accessed (Average number of microservices accessed during a transaction) : {Number(averageClustersAccessed.toFixed(2))}
                <BootstrapTable
                    bootstrap4
                    keyField='functionality'
                    data={metricsRows}
                    columns={metricsColumns}
                />
            </div>
            }
            {showGraph && currentSubView === "Sequence Table" &&
            <div style={{marginTop: "2.5rem", marginLeft: "0.5rem"}}>
                <h4>{functionality.name}</h4>
                <BootstrapTable
                    bootstrap4
                    keyField='id'
                    data={localTransactionsSequence}
                    columns={seqColumns}
                />
            </div>
            }
            {showGraph && currentSubView === "Functionality Redesign" && selectedRedesign === null &&
            !compareRedesigns &&
            <div style={{marginTop: "1rem", marginLeft: "0.5rem"}}>
                <br/>
                <h4 style={{color: "#666666"}}>{functionality.name} Redesigns</h4>
                {functionality.functionalityRedesigns.length >= 2 &&
                <ButtonGroup className="mb-2">
                    <Button>Compare Two Redesigns</Button>
                    <DropdownButton as={ButtonGroup}
                                    title={selectedRedesignsToCompare1}>
                        {functionality.functionalityRedesigns.map(e =>
                            <Dropdown.Item
                                key={e.name}
                                onClick={() => setSelectedRedesignsToCompare1(e.name)}>{e.name}
                            </Dropdown.Item>)}
                    </DropdownButton>
                    <DropdownButton as={ButtonGroup}
                                    title={selectedRedesignsToCompare2}>
                        {functionality.functionalityRedesigns.filter(e => selectedRedesignsToCompare1 !== e.name).map(e =>
                            <Dropdown.Item
                                key={e.name}
                                onClick={() => setSelectedRedesignsToCompare2(e.name)}>{e.name}
                            </Dropdown.Item>)}
                    </DropdownButton>
                    <Button disabled={selectedRedesignsToCompare1 === "Select a Redesign" || selectedRedesignsToCompare2 === "Select a Redesign"}
                            onClick={() => setCompareRedesigns(true)}>Submit</Button>
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
                <Button style={{position: "absolute", marginLeft: "0.55rem", marginTop: "0.15rem"}}
                        onClick={() => {setCompareRedesigns(false); setSelectedRedesignsToCompare1("Select a Redesign"); setSelectedRedesignsToCompare2("Select a Redesign");}}>
                    Back
                </Button>
                <Container fluid style={{paddingLeft:"0px", paddingRight:"0px", marginTop: "3rem", marginLeft: "-0.15rem"}}>
                    <Row>
                        <Col>
                            {functionality.type === "SAGA" ?
                                <div>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        {selectedRedesignsToCompare1}
                                    </h4>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        Functionality Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare1)[0].metrics[MetricType.FUNCTIONALITY_COMPLEXITY]} - System Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare1)[0].metrics[MetricType.SYSTEM_COMPLEXITY]}
                                    </h4>
                                </div>
                                :
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Query Inconsistency Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare1)[0].metrics[MetricType.INCONSISTENCY_COMPLEXITY]}
                                </h4>
                            }
                            {renderRedesignGraph(createRedesignGraph(functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare1)[0], functionality), optionsFunctionalityRedesignCompare)}
                        </Col>
                        <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                            {functionality.type === "SAGA" ?
                                <div>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        {selectedRedesignsToCompare2}
                                    </h4>
                                    <h4 style={{color: "#666666", textAlign: "center"}}>
                                        Functionality Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare2)[0].metrics[MetricType.FUNCTIONALITY_COMPLEXITY]} - System Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare2)[0].metrics[MetricType.SYSTEM_COMPLEXITY]}
                                    </h4>
                                </div>
                                :
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Query Inconsistency Complexity: {functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare2)[0].metrics[MetricType.INCONSISTENCY_COMPLEXITY]}
                                </h4>
                            }
                            {renderRedesignGraph(createRedesignGraph(functionality.functionalityRedesigns.filter(e => e.name === selectedRedesignsToCompare2)[0], functionality), optionsFunctionalityRedesignCompare)}
                        </Col>
                    </Row>
                </Container>
            </div>
            }
            {showGraph && currentSubView === "Functionality Redesign" && selectedRedesign !== null &&
            <Container fluid style={{paddingLeft:"0px", paddingRight:"0px"}}>
                {showGraph && currentSubView === "Functionality Redesign" && functionality.type === "SAGA" &&
                    <h4 style={{color: "#666666", textAlign: "center", position: "absolute", marginTop: "2rem", marginLeft: "80%", transform: "translate(-50%,0%)"}}>
                        Functionality Complexity: {selectedRedesign.metrics[MetricType.FUNCTIONALITY_COMPLEXITY]} - System Complexity: {selectedRedesign.metrics[MetricType.SYSTEM_COMPLEXITY]}
                    </h4>
                }
                {showGraph && currentSubView === "Functionality Redesign" &&
                    functionality.type === "QUERY" &&
                    <h4 style={{color: "#666666", textAlign: "center", position: "absolute", marginTop: "2rem", marginLeft: "80%", transform: "translate(-50%,0)"}}>
                        Query Inconsistency Complexity: {selectedRedesign.metrics[MetricType.INCONSISTENCY_COMPLEXITY]}
                    </h4>
                }
                <Row style={{zIndex: 1, position: "absolute", marginTop: "2.5rem", marginLeft: "-0.15rem"}}>
                    <Col>
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
                </Row>
                {renderRedesignGraph(redesignVisGraph, optionsFunctionalityRedesign)}
            </Container>
            }
        </div>
    );
}
