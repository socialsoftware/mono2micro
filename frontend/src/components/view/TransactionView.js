import React from 'react';
import {RepositoryService} from '../../services/RepositoryService';
import {VisNetwork} from '../util/VisNetwork';
import { DataSet } from "vis";
import {types, views} from './Views';
import BootstrapTable from 'react-bootstrap-table-next';

import {Button, ButtonGroup, Card, Col, Container, Dropdown, DropdownButton, Row, CardDeck} from 'react-bootstrap';

import {FunctionalityRedesignMenu, redesignOperations} from './FunctionalityRedesignMenu';
import {ModalMessage} from "../util/ModalMessage";
import {DEFAULT_REDESIGN_NAME} from "../../constants/constants";
import {TransactionOperationsMenu} from "./TransactionOperationsMenu";

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


export class TransactionView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            visGraph: {},
            visGraphSeq: {},
            redesignVisGraph: {},
            controller: {},
            controllerClusters: [],
            showGraph: false,
            localTransactionsSequence: [],
            currentSubView: "Graph",
            clusterSequence: [],
            showMenu: false,
            error: false,
            selectedRedesign: null,
            selectedOperation: redesignOperations.NONE,
            selectedLocalTransaction: null,
            newCaller: null,
            addCompensating: false,
            modifiedEntities: null,
            DCGIAvailableClusters: null,
            DCGILocalTransactionsForTheSelectedClusters: null,
            DCGISelectedLocalTransactions: [],
            selectedRedesignsToCompare: ["Select a Redesign", "Select a Redesign"],
            compareRedesigns: false,
            graph: {
                controllers: [],
                clusters: [],
            }
        };

        this.handleControllerSubmit = this.handleControllerSubmit.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleSelectOperation = this.handleSelectOperation.bind(this);
        this.closeErrorMessageModal = this.closeErrorMessageModal.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.DCGISelectCluster = this.DCGISelectCluster.bind(this);
        this.handleDCGISelectLocalTransaction = this.handleDCGISelectLocalTransaction.bind(this);
        this.rebuildRedesignGraph = this.rebuildRedesignGraph.bind(this);
        this.handleSelectRedesign = this.handleSelectRedesign.bind(this);
        this.handlePivotTransactionSubmit = this.handlePivotTransactionSubmit.bind(this);
        this.handleDeleteRedesign = this.handleDeleteRedesign.bind(this);
        this.setComparingRedesign = this.setComparingRedesign.bind(this);
        this.handleCompareRedesignSubmit = this.handleCompareRedesignSubmit(this);
        this.handleUseForMetrics = this.handleUseForMetrics.bind(this);
    }

    componentDidMount() {
        const {
            codebaseName,
            dendrogramName,
            graphName,
        } = this.props;

        const service = new RepositoryService();

        service.getControllerClusters(
            codebaseName,
            dendrogramName,
            graphName
        ).then(response => {
            console.log(response);
            this.setState({
                controllerClusters: response.data
            });
        });

        service.getGraph(
            codebaseName,
            dendrogramName,
            graphName,
            ["clusters", "controllers"]
        ).then(response => {
            console.log(response);
            this.setState({
                graph: response.data,
            });
        });
    }

    handleControllerSubmit(value) {
        this.setState({
            controller: this.state.graph.controllers.find(c => c.name === value),
        }, () => {
            this.loadGraph();
        });
    }

    loadGraph() {
        this.createTransactionDiagram();
        this.createSequenceDiagram();
        this.setState({
            showGraph: true
        });
    }

    createTransactionDiagram() {
        const {
            controller,
            controllerClusters,
        } = this.state;


        const visGraph = {
            nodes: new DataSet(controllerClusters[controller.name].map(cluster => this.createNode(cluster))),
            edges: new DataSet(controllerClusters[controller.name].map(cluster => this.createEdge(cluster)))
        };
        visGraph.nodes.add({
            id: controller.name,
            title: Object.entries(controller.entities).map(e => e[0] + " " + e[1]).join('<br>') + "<br>Total: " + Object.keys(controller.entities).length,
            label: controller.name,
            level: 0,
            value: 1,
            type: types.CONTROLLER
        });

        this.setState({
            visGraph: visGraph
        });
    }

    createNode(cluster) {
        return {
            id: cluster.name,
            title: cluster.entities.join('<br>') + "<br>Total: " + cluster.entities.length,
            label: cluster.name,
            value: cluster.entities,
            level: 1,
            type: types.CLUSTER
        };
    }

    createEdge(cluster) {
        let entitiesTouched = Object.entries(this.state.controller.entities)
            .filter(e => cluster.entities.includes(e[0]))
            .map(e => e[0] + " " + e[1]);

        return {
            from: this.state.controller.name,
            to: cluster.name,
            label: entitiesTouched.length.toString(),
            title: entitiesTouched.join('<br>')
        };
    }

    createSequenceDiagram() {

        const {
            controller,
            graph,
        } = this.state;

        let nodes = [];
        let edges = [];
        let localTransactionsSequence = [];
        const localTransactionIdToClusterAccesses = {};

        nodes.push({
            id: 0,
            label: controller.name,
            level: 0,
            value: 1,
            type: types.CONTROLLER,
            title: Object.entries(controller.entities)
                .map(e => e[0] + " " + e[1])
                .join('<br>') + "<br>Total: " + Object.keys(controller.entities).length,
        });

        localTransactionIdToClusterAccesses[0] = [];

        let {
            nodes: localTransactionsList,
            links: linksList,
        } = controller.localTransactionsGraph;


        for (var i = 1; i < localTransactionsList.length; i++) {

            let {
                id: localTransactionId,
                clusterName,
                clusterAccesses,
            } = localTransactionsList[i];

            localTransactionIdToClusterAccesses[localTransactionId] = clusterAccesses;

            let cluster = graph.clusters.find(cluster => cluster.name === clusterName);
            const clusterEntityNames = cluster.entities;

            nodes.push({
                id: localTransactionId,
                title: clusterEntityNames.join('<br>') + "<br>Total: " + clusterEntityNames.length,
                label: cluster.name,
                value: clusterEntityNames.length,
                level: 1,
                type: types.CLUSTER
            });

            localTransactionsSequence.push({
                id: localTransactionId,
                cluster: cluster.name,
                entities: <pre>{clusterAccesses.map(acc => acc.join(" ")).join('\n')}</pre>
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
                title: clusterAccesses.map(acc => acc.join(" ")).join('<br>'),
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

        this.setState({
            visGraphSeq,
            localTransactionsSequence,
        });
    }

    createRedesignGraph(functionalityRedesign){
        let nodes = [];
        let edges = [];

        nodes.push({
            id: -1,
            title: Object.entries(this.state.controller.entities).map(e => e[0] + " " + e[1]).join('<br>') + "<br>Total: " + Object.keys(this.state.controller.entities).length,
            label: this.state.controller.name,
            level: -1,
            value: 1,
            type: types.CONTROLLER
        });

        functionalityRedesign.redesign.find(e => e.id === (-1).toString())
            .remoteInvocations.forEach((id) => {
                const lt = functionalityRedesign.redesign.find(e => e.id === id.toString());
                let cluster = this.state.graph.clusters.find(cluster => cluster.name === lt.cluster);
                nodes.push({
                    id: lt.id,
                    title: lt.type,
                    label: lt.name,
                    level: 0,
                    type: types.CLUSTER
                });

                edges.push({
                    from: -1,
                    to: lt.id,
                    title: JSON.parse(lt.accessedEntities).map(e => e.join(" ")).join('<br>'),
                    label: JSON.parse(lt.accessedEntities).length.toString()
                });
            });

        for(let i = 0; i < nodes.length; i++){
            if(nodes[i].id >= 0) {
                let localTransaction = functionalityRedesign.redesign.find(entry => entry.id === nodes[i].id.toString());
                localTransaction.remoteInvocations.forEach((id) => {
                    let lt = functionalityRedesign.redesign.find(e => e.id === id.toString());
                    let cluster = this.state.graph.clusters.find(cluster => cluster.name === lt.cluster);

                    nodes.push({
                        id: lt.id,
                        title: lt.type,
                        label: lt.name,
                        level: nodes[i].level + 1,
                        type: types.CLUSTER
                    });

                    let entitiesTouched = JSON.parse(lt.accessedEntities);
                    edges.push({
                        from: nodes[i].id,
                        to: id,
                        title: Object.values(entitiesTouched).map(e => e.join(" ")).join('<br>'),
                        label: entitiesTouched.length.toString()
                    });
                });
            }
        }

        return {
            nodes: new DataSet(nodes),
            edges: new DataSet(edges)
        };
    }

    identifyModifiedEntities(cluster){
        console.log(Object.entries(this.state.controller.entities));
        return {
            cluster: cluster.name,
            modifiedEntities: Object.entries(this.state.controller.entities)
                .filter(e => cluster.entities.includes(e[0]) && e[1].includes("W"))
                .map(e => e[0])
        }
    }


    handleSelectNode(nodeId) {
        console.log(nodeId);
        if(nodeId === -1 && this.state.selectedOperation !== redesignOperations.SQ) return;
        if(this.state.compareRedesigns) return;

        if(this.state.selectedOperation === redesignOperations.NONE) {
            this.setState({
                showMenu: true,
                selectedLocalTransaction: this.state.selectedRedesign.redesign.find(c => c.id === nodeId.toString())
            });
        }

        if(this.state.selectedOperation === redesignOperations.SQ){
            if(nodeId.toString() === this.state.selectedLocalTransaction.id){
                this.setState({
                    error: true,
                    errorMessage: "One local transaction cannot call itself"
                });
            } else if(this.state.selectedRedesign.redesign.find(c => c.id === nodeId.toString()).remoteInvocations
                .includes(parseInt(this.state.selectedLocalTransaction.id))) {
                const lt = this.state.selectedRedesign.redesign.find(e => e.id === nodeId.toString());
                console.log(lt);
                this.setState({
                    error: true,
                    errorMessage: "The local transaction " + lt.name
                        + " is already invoking local transaction " + this.state.selectedLocalTransaction.name
                });
            } else if(this.checkTransitiveClosure(nodeId)){
                this.setState({
                    error: true,
                    errorMessage: "There cannot exist a cyclic dependency"
                });
            } else {
                this.setState({
                   newCaller: this.state.selectedRedesign.redesign.find(c => c.id === nodeId.toString())
                });
            }
        }
        else if(this.state.selectedOperation === redesignOperations.DCGI &&
                    this.state.DCGILocalTransactionsForTheSelectedClusters !== null){
            const localTransaction = this.state.selectedRedesign.redesign.find(c => c.id === nodeId.toString());

            console.log(this.state.DCGIAvailableClusters);
            console.log(localTransaction);

            if(!this.state.DCGISelectedClusters.includes(localTransaction.cluster))
                return

            if(!this.state.DCGISelectedLocalTransactions.map(e => e.id).includes(nodeId)){
                const aux = this.state.DCGISelectedLocalTransactions;
                aux.push(this.state.DCGILocalTransactionsForTheSelectedClusters.find(e => e.id === nodeId.toString()));
                this.setState({
                    DCGISelectedLocalTransactions: aux
                });
            }
        }
    }

    checkTransitiveClosure(nodeId){
        let transitiveClosure = this.state.selectedLocalTransaction.remoteInvocations;
        for(let i = 0; i < transitiveClosure.length; i++){
            if(transitiveClosure[i] === parseInt(nodeId))
                return true;
            transitiveClosure = transitiveClosure.concat(this.state.selectedRedesign.redesign
                .find(e => e.id === transitiveClosure[i].toString()).remoteInvocations);
        }
        return false;
    }

    handleDeselectNode(nodeId) {}

    changeSubView(value) {
        this.setState({
            currentSubView: value
        });
    }

    handleSelectOperation(value){
        this.setState({
            selectedOperation: value
        });

        if(value === redesignOperations.AC){
            this.setState({
                modifiedEntities: this.state.controllerClusters[this.state.controller.name]
                    .map(cluster => this.identifyModifiedEntities(cluster))
                    .filter(e => e.modifiedEntities.length > 0)
                    .filter(e => e.cluster !== this.state.selectedLocalTransaction.cluster)
            });
        } else if(value === redesignOperations.DCGI) {
            this.setState({
                DCGIAvailableClusters: this.state.controllerClusters[this.state.controller.name]
                    .filter(e => e.name !== this.state.selectedLocalTransaction.cluster)
                    .map(e => e.name),
                DCGISelectedClusters: [this.state.selectedLocalTransaction.cluster]
            });
        }
    }

    closeErrorMessageModal() {
        this.setState({
            error: false,
            errorMessage: ''
        });
    }

    handleSubmit(value){
        const service = new RepositoryService();
        switch (this.state.selectedOperation) {
            case redesignOperations.AC:
                service.addCompensating(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, this.state.selectedRedesign.name, value.cluster, value.entities, this.state.selectedLocalTransaction.id)
                    .then(response => {
                        this.rebuildRedesignGraph(response);
                    }).catch(() => {
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: Add Compensating failed.'
                        });
                    });
                break;
            case redesignOperations.SQ:
                service.sequenceChange(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, this.state.selectedRedesign.name, this.state.selectedLocalTransaction.id, this.state.newCaller.id)
                    .then(response => {
                        this.rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.log(err);
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: Sequence Change failed.'
                        });
                });
                break;
            case redesignOperations.DCGI:
                service.dcgi(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, this.state.selectedRedesign.name, this.state.DCGISelectedClusters[0], this.state.DCGISelectedClusters[1],
                    JSON.stringify(this.state.DCGISelectedLocalTransactions.map(e => e.id)))
                    .then(response => {
                        this.rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.log(err);
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: DCGI failed.'
                        });
                });
                break;

            case redesignOperations.PIVOT:
                service.selectPivotTransaction(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, this.state.selectedRedesign.name, this.state.selectedLocalTransaction.id,
                    this.state.selectedRedesign.pivotTransaction === "" ? value : null)
                    .then(response => {
                        console.log(response);
                        this.handlePivotTransactionSubmit(response);

                    }).catch( error => {
                        console.log(error.response)
                        if(error.response !== undefined && error.response.status === HttpStatus.FORBIDDEN){
                            this.setState({
                                error: true,
                                errorMessage: 'Pivot selection failed - ' + error.response.data
                            });
                        } else {
                            this.setState({
                                error: true,
                                errorMessage: 'Pivot selection failed.'
                            });
                        }
                    });
                break;


            case redesignOperations.RENAME:
                service.changeLTName(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, this.state.selectedRedesign.name, this.state.selectedLocalTransaction.id, value)
                    .then(response => {
                        this.rebuildRedesignGraph(response);
                    }).catch(() => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: Pivot selection failed.'
                    });
                });
                break;

            default:
                break;
        }
    }

    rebuildRedesignGraph(value){
        console.log(value);
        const controllers = this.state.graph.controllers;
        const index = controllers.indexOf(this.state.controller);
        controllers[index] = value.data;
        const redesign = value.data.functionalityRedesigns.find(e => e.name === this.state.selectedRedesign.name);
        this.setState({
            controllers: controllers,
            controller: value.data,
            selectedRedesign: redesign,
            redesignVisGraph: this.createRedesignGraph(redesign)
        }, () => {
            this.handleCancel();
        });
    }

    handlePivotTransactionSubmit(value){
        console.log(value);
        const controllers = this.state.graph.controllers;
        const index = controllers.indexOf(this.state.controller);
        controllers[index] = value.data;
        this.setState({
            controllers: controllers,
            controller: value.data,
            selectedRedesign: null
        }, () => {
            this.handleCancel();
        });
    }

    handleCancel(){
        this.setState({
            showMenu: false,
            selectedLocalTransaction: null,
            selectedOperation: redesignOperations.NONE,
            newCaller: null,
            addCompensating: false,
            modifiedEntities: null,
            DCGIAvailableClusters: null,
            DCGILocalTransactionsForTheSelectedClusters: null,
            DCGISelectedLocalTransactions: []
        });
    }


    DCGISelectCluster(value){
        const selectedClusters = this.state.DCGISelectedClusters;
        selectedClusters.push(value);

        const localTransactionsForTheSelectedClusters =
            this.state.selectedRedesign.redesign.filter(e => selectedClusters.includes(e.cluster));

        this.setState({
            DCGILocalTransactionsForTheSelectedClusters: localTransactionsForTheSelectedClusters,
            DCGISelectedLocalTransactions: [this.state.selectedLocalTransaction],
            DCGISelectedClusters: selectedClusters
        });
    }

    handleDCGISelectLocalTransaction(value){
        console.log(value);
        if(value === null || value.length === 0){
            this.setState({
                DCGISelectedLocalTransactions: [],
            });
        } else {
            let selectedLocalTransactions = this.state.DCGILocalTransactionsForTheSelectedClusters
                .filter(e => value.map(entry => entry.value).includes(e.id));
            this.setState({
                DCGISelectedLocalTransactions: selectedLocalTransactions,
            });
        }
    }

    renderFunctionalityRedesigns(){
        return <CardDeck style={{ width: "fit-content" }}>
            {this.state.controller.functionalityRedesigns.map(fr =>
                <Card className="mb-4" key={fr.name} style={{ width: "30rem" }}>
                    <Card.Body>
                        {fr.usedForMetrics ? <Card.Title>
                            {fr.name + " (Used For Metrics)"}
                        </Card.Title> :
                            <Card.Title>
                                {fr.name}
                            </Card.Title>
                        }
                        {this.state.controller.type === "QUERY" ?
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
                        <Button onClick={() => this.handleSelectRedesign(fr)} className="mr-2">
                            {fr.name === DEFAULT_REDESIGN_NAME ? "Create a new Redesign" : "Go to Redesign"}
                        </Button>
                        <Button onClick={() => this.handleUseForMetrics(fr)} className="mr-2" disabled={fr.usedForMetrics}>
                            Use For Metrics
                        </Button>
                        <Button onClick={() => this.handleDeleteRedesign(fr)} variant="danger" className="mr-2" disabled={fr.name === DEFAULT_REDESIGN_NAME}>
                            Delete
                        </Button>
                    </Card.Body>
                </Card>
            )}
        </CardDeck>
    }

    handleUseForMetrics(value){
        console.log(value);
        const service = new RepositoryService();
        service.setUseForMetrics(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
            this.state.controller.name, value.name)
            .then(response => {
                const controllers = this.state.graph.controllers;
                const index = controllers.indexOf(this.state.controller);
                controllers[index] = response.data;
                this.setState({
                    controllers: controllers,
                    controller: response.data,
                });
            }).catch(() => {
                this.setState({
                    error: true,
                    errorMessage: 'ERROR: Change Functionality Used for Metrics failed.'
                });
            }
        );
    }

    handleSelectRedesign(value){
        this.setState({
            selectedRedesign: value,
            redesignVisGraph: this.createRedesignGraph(value)
        });
    }

    handleDeleteRedesign(value){
        console.log(value);
        const service = new RepositoryService();
        service.deleteRedesign(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
            this.state.controller.name, value.name)
            .then(response => {
                const controllers = this.state.graph.controllers;
                const index = controllers.indexOf(this.state.controller);
                controllers[index] = response.data;
                this.setState({
                    controllers: controllers,
                    controller: response.data,
                });
            }).catch(() => {
                this.setState({
                    error: true,
                    errorMessage: 'ERROR: Delete Redesign failed.'
                });
            }
        );
    }

    renderRedesignGraph(graph){
        return <div>
            <div style={{display:'none'}}>
                {/*this div functions as a "cache". Is is used to render the graph with the optionsSeq
                                     options in order to save the positions such that when the graph is generated with the
                                     optionsFunctionalityRedesign options is much quicker and there is no buffering*/}
                <VisNetwork
                    visGraph={graph}
                    options={optionsSeq}
                    onSelection={this.handleSelectNode}
                    onDeselection={this.handleDeselectNode}
                    view={views.TRANSACTION} />
            </div>
            <div style={{height: '700px'}}>
                <VisNetwork
                    visGraph={graph}
                    options={optionsFunctionalityRedesign}
                    onSelection={this.handleSelectNode}
                    onDeselection={this.handleDeselectNode}
                    view={views.TRANSACTION} />
            </div>
        </div>
    }

    setComparingRedesign(index, name){
        const selectedRedesigns = this.state.selectedRedesignsToCompare;
        selectedRedesigns[index] = name;
        this.setState({
            selectedRedesignsToCompare: selectedRedesigns
        });
    }

    handleCompareRedesignSubmit(){
        this.setState({
            compareRedesigns: true
        });
    }

    render() {

        const {
            controllerClusters,
            currentSubView,
            visGraphSeq,
            localTransactionsSequence,
            showGraph,
            controller,
            graph: {
                controllers,
                clusters,
            },
        } = this.state;

        const metricsRows = controllers.map(controller => {
            return controller.type === "QUERY" ?
            {
                controller: controller.name,
                clusters: this.state.controllerClusters[controller.name] === undefined ? 0 : this.state.controllerClusters[controller.name].length,
                type: controller.type,
                complexity: controller.complexity,
                inconsistencyComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).inconsistencyComplexity

            }
            :
            {
                controller: controller.name,
                clusters: this.state.controllerClusters[controller.name] === undefined ? 0 : this.state.controllerClusters[controller.name].length,
                type: controller.type,
                complexity: controller.complexity,
                functionalityComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).functionalityComplexity,
                systemComplexity: controller.functionalityRedesigns.find(fr => fr.usedForMetrics).systemComplexity
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

        let controllerClustersAmount = Object.keys(controllerClusters).map(controller => controllerClusters[controller].length);
        let averageClustersAccessed = controllerClustersAmount.reduce((a, b) => a + b, 0) / controllerClustersAmount.length;

        return (
            <div>
                {this.state.error &&
                <ModalMessage
                    title='Error Message'
                    message={this.state.errorMessage}
                    onClose={this.closeErrorMessageModal} />}
                <Container fluid>
                    <Row>
                        <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                            <ButtonGroup className="mb-2">
                                <Button
                                    disabled={currentSubView === "Graph"}
                                    onClick={() => this.changeSubView("Graph")}
                                >
                                    Graph
                                </Button>
                                <Button
                                    disabled={currentSubView === "Sequence Graph"}
                                    onClick={() => this.changeSubView("Sequence Graph")}
                                >
                                    Sequence Graph
                                </Button>
                                <Button
                                    disabled={currentSubView === "Metrics"}
                                    onClick={() => this.changeSubView("Metrics")}
                                >
                                    Metrics
                                </Button>
                                <Button
                                    disabled={currentSubView === "Sequence Table"}
                                    onClick={() => this.changeSubView("Sequence Table")}
                                >
                                    Sequence Table
                                </Button>
                                <Button
                                    disabled={this.state.currentSubView === "Functionality Redesign"}
                                    onClick={() => this.changeSubView("Functionality Redesign")}
                                >
                                    Functionality Redesign
                                </Button>
                            </ButtonGroup>
                        </Col>
                        {this.state.selectedRedesign !== null &&
                            <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                {this.state.showGraph && this.state.currentSubView === "Functionality Redesign" &&
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Functionality Complexity: {this.state.selectedRedesign.functionalityComplexity} - System Complexity: {this.state.selectedRedesign.systemComplexity}
                                </h4>
                                }
                            </Col>
                        }
                    </Row>
                </Container>

                {this.state.currentSubView === "Graph" &&
                    <span>
                        <TransactionOperationsMenu
                            handleControllerSubmit={this.handleControllerSubmit}
                            controllerClusters={this.state.controllerClusters}
                        />
                        <div style={{height: '700px'}}>
                            <VisNetwork
                                visGraph={this.state.visGraph}
                                options={options}
                                onSelection={this.handleSelectNode}
                                onDeselection={this.handleDeselectNode}
                                view={views.TRANSACTION} />
                        </div>
                    </span>
                }

                {this.state.currentSubView === "Sequence Graph" &&
                    <div style={{height: '700px'}}>
                        <VisNetwork
                            visGraph={visGraphSeq}
                            options={optionsSeq}
                            onSelection={this.handleSelectNode}
                            onDeselection={this.handleDeselectNode}
                            view={views.TRANSACTION}
                        />
                    </div>
                }

                {currentSubView === "Metrics" &&
                    <div>
                        Number of Clusters : {clusters.length}
                        < br />
                        Number of Controllers that access a single Cluster : {Object.keys(controllerClusters).filter(key => controllerClusters[key].length === 1).length}
                        < br />
                        Maximum number of Clusters accessed by a single Controller : {Math.max(...Object.keys(controllerClusters).map(key => controllerClusters[key].length))}
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

                {this.state.showGraph && this.state.currentSubView === "Functionality Redesign" && this.state.selectedRedesign === null &&
                !this.state.compareRedesigns &&
                    <div>
                        <br/>

                        <h4 style={{color: "#666666"}}>Redesigns</h4>

                        {this.state.controller.functionalityRedesigns.length >= 2 &&
                        <ButtonGroup className="mb-2">
                            <Button>Compare Two Redesigns</Button>
                            <DropdownButton as={ButtonGroup}
                                            title={this.state.selectedRedesignsToCompare[0]}>
                                {this.state.controller.functionalityRedesigns.map(e =>
                                    <Dropdown.Item
                                        key={e.name}
                                        onSelect={() => this.setComparingRedesign(0, e.name)}>{e.name}
                                    </Dropdown.Item>)}
                            </DropdownButton>
                            <DropdownButton as={ButtonGroup}
                                            title={this.state.selectedRedesignsToCompare[1]}>
                                {this.state.controller.functionalityRedesigns.filter(e => this.state.selectedRedesignsToCompare[0] !== e.name).map(e =>
                                    <Dropdown.Item
                                        key={e.name}
                                        onSelect={() => this.setComparingRedesign(1, e.name)}>{e.name}
                                    </Dropdown.Item>)}
                            </DropdownButton>
                            <Button onClick={() => this.setState({compareRedesigns: true})}>Submit</Button>
                        </ButtonGroup>
                        }
                        <br/>
                        <br/>
                        {this.renderFunctionalityRedesigns()}
                    </div>
                }
                {this.state.showGraph && this.state.currentSubView === "Functionality Redesign" && this.state.selectedRedesign === null &&
                this.state.compareRedesigns &&
                <div>
                    <Button className="mb-2"
                            onClick={() => this.setState({compareRedesigns: false, selectedRedesignsToCompare: ["Select a Redesign", "Select a Redesign"]})}>
                        Back
                    </Button>
                    <Container fluid>
                        <Row>
                            <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    {this.state.selectedRedesignsToCompare[0]}
                                </h4>
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Functionality Complexity: {this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[0])[0].functionalityComplexity} - System Complexity: {this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[0])[0].systemComplexity}
                                </h4>
                                {this.renderRedesignGraph(this.createRedesignGraph(this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[0])[0]))}
                            </Col>
                            <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    {this.state.selectedRedesignsToCompare[1]}
                                </h4>
                                <h4 style={{color: "#666666", textAlign: "center"}}>
                                    Functionality Complexity: {this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[1])[0].functionalityComplexity} - System Complexity: {this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[1])[0].systemComplexity}
                                </h4>
                                {this.renderRedesignGraph(this.createRedesignGraph(this.state.controller.functionalityRedesigns.filter(e => e.name === this.state.selectedRedesignsToCompare[1])[0]))}
                            </Col>
                        </Row>
                    </Container>
                </div>
                }

                {this.state.showGraph && this.state.currentSubView === "Functionality Redesign" && this.state.selectedRedesign !== null &&
                    <Container fluid>
                        <Row>
                            <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                <Button className="mb-2"
                                        onClick={() => this.setState({selectedRedesign: null}, () => this.handleCancel())}>
                                    Back
                                </Button>
                                {this.state.showMenu &&
                                    <FunctionalityRedesignMenu
                                        selectedRedesign = {this.state.selectedRedesign}
                                        selectedLocalTransaction = {this.state.selectedLocalTransaction}
                                        newCaller = {this.state.newCaller}
                                        modifiedEntities = {this.state.modifiedEntities}
                                        DCGIAvailableClusters = {this.state.DCGIAvailableClusters}
                                        DCGILocalTransactionsForTheSelectedClusters = {this.state.DCGILocalTransactionsForTheSelectedClusters}
                                        DCGISelectedLocalTransactions = {this.state.DCGISelectedLocalTransactions}
                                        handleSelectOperation = {this.handleSelectOperation}
                                        handleCancel = {this.handleCancel}
                                        handleSubmit = {this.handleSubmit}
                                        DCGISelectCluser = {this.DCGISelectCluster}
                                        handleDCGISelectLocalTransaction = {this.handleDCGISelectLocalTransaction}
                                    />
                                }
                            </Col>
                            <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                {this.renderRedesignGraph(this.state.redesignVisGraph)}
                            </Col>
                        </Row>
                    </Container>
                }
            </div>
        );
    }
}
