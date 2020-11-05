import React from 'react';
import {RepositoryService} from '../../services/RepositoryService';
import {VisNetwork} from '../util/VisNetwork';
import { DataSet } from "vis";
import {types, views} from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import CardDeck from 'react-bootstrap/CardDeck';
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
            controllersClusters: [],
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
            decomposition: {
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
            decompositionName,
        } = this.props;

        const service = new RepositoryService();

        service.getControllersClusters(
            codebaseName,
            dendrogramName,
            decompositionName
        ).then(response => {
            this.setState({
                controllersClusters: response.data
            });
        });

        service.getDecomposition(
            codebaseName,
            dendrogramName,
            decompositionName,
            ["clusters", "controllers"]
        ).then(response => {
            this.setState({
                decomposition: {
                    controllers: Object.values(response.data.controllers),
                    clusters: Object.values(response.data.clusters),
                },
            });
        });
    }

    handleControllerSubmit(value) {
        this.setState({
            controller: this.state.decomposition.controllers.find(c => c.name === value),
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
            controllersClusters,
        } = this.state;


        const visGraph = {
            nodes: new DataSet(controllersClusters[controller.name].map(cluster => this.createNode(cluster))),
            edges: new DataSet(controllersClusters[controller.name].map(cluster => this.createEdge(cluster)))
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
        const text = []     
        
        Object.entries(this.state.controller.entities).forEach(([key, value]) => {
            if (cluster.entities.includes(Number(key)))
                text.push(key + " " + value)
        });


        return {
            from: this.state.controller.name,
            to: cluster.name,
            label: text.length.toString(),
            title: text.join('<br>')
        };
    }

    createSequenceDiagram() {

        const {
            controller,
            decomposition,
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
                clusterID,
                clusterAccesses,
            } = localTransactionsList[i];


            localTransactionIdToClusterAccesses[localTransactionId] = clusterAccesses;

            let cluster = decomposition.clusters.find(cluster => Number(cluster.name) === clusterID);
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
                let cluster = this.state.decomposition.clusters.find(cluster => cluster.name === lt.cluster);
                nodes.push({
                    id: lt.id,
                    title: cluster.entities.join('<br>') + "<br>Total: " + cluster.entities.length,
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
                    let cluster = this.state.decomposition.clusters.find(cluster => cluster.name === lt.cluster);

                    nodes.push({
                        id: lt.id,
                        title: cluster.entities.join('<br>') + "<br>Total: " + cluster.entities.length,
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
        const {
            selectedOperation,
            selectedRedesign,
            compareRedesigns,
            selectedLocalTransaction,
            DCGIAvailableClusters,
            DCGILocalTransactionsForTheSelectedClusters,
            DCGISelectedLocalTransactions,
            DCGISelectedClusters
        } = this.state;

        console.log(nodeId);
        if(nodeId === -1 && selectedOperation !== redesignOperations.SQ) return;
        if(compareRedesigns) return;

        if(selectedOperation === redesignOperations.NONE) {
            this.setState({
                showMenu: true,
                // selectedLocalTransaction: selectedRedesign.redesign.find(c => c.id === nodeId.toString())
            });
        }

        if(selectedOperation === redesignOperations.SQ){
            if(nodeId.toString() === selectedLocalTransaction.id){
                this.setState({
                    error: true,
                    errorMessage: "One local transaction cannot call itself"
                });
            } else if(
                selectedRedesign.redesign.find(
                    c => c.id === nodeId.toString()
                ).remoteInvocations.includes(parseInt(selectedLocalTransaction.id))
            ) {
                const lt = selectedRedesign.redesign.find(e => e.id === nodeId.toString());
                console.log(lt);
                this.setState({
                    error: true,
                    errorMessage: "The local transaction " + lt.name
                        + " is already invoking local transaction " + selectedLocalTransaction.name
                });
            } else if(this.checkTransitiveClosure(nodeId)){
                this.setState({
                    error: true,
                    errorMessage: "There cannot exist a cyclic dependency"
                });
            } else {
                this.setState({
                   newCaller: selectedRedesign.redesign.find(c => c.id === nodeId.toString())
                });
            }
        }
        else if(selectedOperation === redesignOperations.DCGI &&
                    DCGILocalTransactionsForTheSelectedClusters !== null){
            const localTransaction = selectedRedesign.redesign.find(c => c.id === nodeId.toString());

            console.log(DCGIAvailableClusters);
            console.log(localTransaction);

            if(!DCGISelectedClusters.includes(localTransaction.cluster))
                return

            if(!DCGISelectedLocalTransactions.map(e => e.id).includes(nodeId)){
                const aux = DCGISelectedLocalTransactions;
                aux.push(DCGILocalTransactionsForTheSelectedClusters.find(e => e.id === nodeId.toString()));
                this.setState({
                    DCGISelectedLocalTransactions: aux
                });
            }
        }
    }

    checkTransitiveClosure(nodeId){
        let transitiveClosure = this.state.selectedLocalTransaction.remoteInvocations;
        
        for(let i = 0; i < transitiveClosure.length; i++) {
            if(transitiveClosure[i] === parseInt(nodeId))
                return true;
            
            transitiveClosure = transitiveClosure.concat(
                this.state.selectedRedesign.redesign.find(
                    e => e.id === transitiveClosure[i].toString()
                ).remoteInvocations
            );
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
        const {
            controllersClusters,
            selectedLocalTransaction,
            controller,
        } = this.state;


        this.setState({
            selectedOperation: value
        });

        if(value === redesignOperations.AC){
            this.setState({
                modifiedEntities: controllersClusters[controller.name]
                    .map(cluster => this.identifyModifiedEntities(cluster))
                    .filter(e => e.modifiedEntities.length > 0 && e.cluster !== selectedLocalTransaction.cluster)
            });
        } else if(value === redesignOperations.DCGI) {
            this.setState({
                DCGIAvailableClusters: controllersClusters[controller.name]
                    .filter(e => e.name !== selectedLocalTransaction.cluster)
                    .map(e => e.name),
                DCGISelectedClusters: [selectedLocalTransaction.cluster]
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
        const {
            codebaseName,
            dendrogramName,
            decompositionName,
        } = this.props;

        const {
            selectedLocalTransaction,
            selectedRedesign,
            controller,
            selectedOperation,
            newCaller,
            DCGISelectedClusters,
            DCGISelectedLocalTransactions,
        } = this.state;

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
                        this.rebuildRedesignGraph(response);
                    }).catch(() => {
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: Add Compensating failed.'
                        });
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
                        this.rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.error(err);
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: Sequence Change failed.'
                        });
                });

                break;

            case redesignOperations.DCGI:
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
                        this.rebuildRedesignGraph(response);
                    }).catch((err) => {
                        console.error(err);
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: DCGI failed.'
                        });
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
                    selectedRedesign.pivotTransaction === "" ? value : null
                )
                    .then(response => {
                        console.log(response);
                        this.handlePivotTransactionSubmit(response);

                    }).catch(error => {
                        console.error(error.response)
                        
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
                        this.rebuildRedesignGraph(response);
                    })
                    .catch(() => {
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
        const controllers = this.state.decomposition.controllers;
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
        const controllers = this.state.decomposition.controllers;
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
                        <Card.Text>
                            Functionality Complexity: {fr.functionalityComplexity}< br/>
                            System Complexity: {fr.systemComplexity}
                        </Card.Text>
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
        service.setUseForMetrics(this.props.codebaseName, this.props.dendrogramName, this.props.decompositionName,
            this.state.controller.name, value.name)
            .then(response => {
                const controllers = this.state.decomposition.controllers;
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
        console.log(value);
        this.setState({
            selectedRedesign: value,
            redesignVisGraph: this.createRedesignGraph(value)
        });
    }

    handleDeleteRedesign(value){
        console.log(value);
        const service = new RepositoryService();
        service.deleteRedesign(this.props.codebaseName, this.props.dendrogramName, this.props.decompositionName,
            this.state.controller.name, value.name)
            .then(response => {
                const controllers = this.state.decomposition.controllers;
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
            controllersClusters,
            currentSubView,
            visGraph,
            visGraphSeq,
            localTransactionsSequence,
            showGraph,
            controller,
            error,
            errorMessage,
            DCGIAvailableClusters,
            DCGILocalTransactionsForTheSelectedClusters,
            DCGISelectedLocalTransactions,
            compareRedesigns,
            modifiedEntities,
            newCaller,
            redesignVisGraph,
            selectedLocalTransaction,
            selectedRedesign,
            selectedRedesignsToCompare,
            showMenu,
            decomposition: {
                controllers,
                clusters,
            },
        } = this.state;

        const metricsRows = controllers.map(controller => {
            return {
                controller: controller.name,
                clusters: controllersClusters[controller.name] === undefined ? 0 : controllersClusters[controller.name].length,
                complexity: controller.complexity,
                functionalityComplexity: controller.functionalityComplexity,
                systemComplexity: controller.systemComplexity,
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
            dataField: 'functionalityComplexity',
            text: 'Functionality Complexity',
            sort: true
        }, {
            dataField: 'systemComplexity',
            text: 'System Complexity',
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
                            onClose={this.closeErrorMessageModal}
                        />
                    )
                }
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
                                {/* <Button
                                    disabled={this.state.currentSubView === "Functionality Redesign"}
                                    onClick={() => this.changeSubView("Functionality Redesign")}
                                >
                                    Functionality Redesign
                                </Button> */}
                            </ButtonGroup>
                        </Col>
                        {
                            selectedRedesign !== null && (
                                <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                    {
                                        showGraph && currentSubView === "Functionality Redesign" && (
                                            <h4 style={{color: "#666666", textAlign: "center"}}>
                                                Functionality Complexity: {selectedRedesign.functionalityComplexity} - System Complexity: {selectedRedesign.systemComplexity}
                                            </h4>
                                        )
                                    }
                                </Col>
                            )
                        }
                    </Row>
                </Container>

                {
                    currentSubView === "Graph" && (
                        <span>
                            <TransactionOperationsMenu
                                handleControllerSubmit={this.handleControllerSubmit}
                                controllersClusters={controllersClusters}
                            />
                            <div style={{height: '700px'}}>
                                <VisNetwork
                                    visGraph={visGraph}
                                    options={options}
                                    onSelection={this.handleSelectNode}
                                    onDeselection={this.handleDeselectNode}
                                    view={views.TRANSACTION} />
                            </div>
                        </span>
                    )
                }
                {
                    currentSubView === "Sequence Graph" && (
                        <div style={{height: '700px'}}>
                            <VisNetwork
                                visGraph={visGraphSeq}
                                options={optionsSeq}
                                onSelection={this.handleSelectNode}
                                onDeselection={this.handleDeselectNode}
                                view={views.TRANSACTION}
                            />
                        </div>
                    )
                }
                {
                    currentSubView === "Metrics" && (
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
                    )
                }
                {
                    showGraph && currentSubView === "Sequence Table" && (
                        <>
                            <h4>{controller.name}</h4>
                            <BootstrapTable
                                bootstrap4
                                keyField='id'
                                data={localTransactionsSequence}
                                columns={seqColumns}
                            />
                        </>
                    )
                }
                {
                    showGraph && currentSubView === "Functionality Redesign" && selectedRedesign === null && !compareRedesigns && (
                        <div>
                            <br/>

                            <h4 style={{color: "#666666"}}>Redesigns</h4>

                            {
                                controller.functionalityRedesigns.length >= 2 & (
                                    <ButtonGroup className="mb-2">
                                        <Button>Compare Two Redesigns</Button>
                                        <DropdownButton as={ButtonGroup}
                                                        title={selectedRedesignsToCompare[0]}>
                                            {
                                                controller.functionalityRedesigns.map(e =>
                                                    <Dropdown.Item
                                                        key={e.name}
                                                        onSelect={() => this.setComparingRedesign(0, e.name)}>{e.name}
                                                    </Dropdown.Item>
                                                )
                                            }
                                        </DropdownButton>
                                        <DropdownButton as={ButtonGroup}
                                                        title={selectedRedesignsToCompare[1]}>
                                            {
                                                controller.functionalityRedesigns.filter(
                                                    e => selectedRedesignsToCompare[0] !== e.name
                                                ).map(e =>
                                                    <Dropdown.Item
                                                        key={e.name}
                                                        onSelect={() => this.setComparingRedesign(1, e.name)}>{e.name}
                                                    </Dropdown.Item>
                                                )
                                            }
                                        </DropdownButton>
                                        <Button onClick={() => this.setState({compareRedesigns: true})}>Submit</Button>
                                    </ButtonGroup>
                                )
                            }
                            <br/>
                            <br/>
                            {this.renderFunctionalityRedesigns()}
                        </div>
                    )
                }
                {
                    showGraph && currentSubView === "Functionality Redesign" && selectedRedesign === null && compareRedesigns && (
                        <div>
                            <Button className="mb-2"
                                    onClick={() => this.setState({compareRedesigns: false, selectedRedesignsToCompare: ["Select a Redesign", "Select a Redesign"]})}>
                                Back
                            </Button>
                            <Container fluid>
                                <Row>
                                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                        <h4 style={{color: "#666666", textAlign: "center"}}>
                                            {selectedRedesignsToCompare[0]}
                                        </h4>
                                        <h4 style={{color: "#666666", textAlign: "center"}}>
                                            Functionality Complexity: {controller.functionalityRedesigns.find(
                                                e => e.name === selectedRedesignsToCompare[0]
                                            ).functionalityComplexity}
                                            - System Complexity: {controller.functionalityRedesigns.find(
                                                e => e.name === selectedRedesignsToCompare[0]
                                            ).systemComplexity}
                                        </h4>
                                        {
                                            this.renderRedesignGraph(
                                                this.createRedesignGraph(controller.functionalityRedesigns.find(
                                                    e => e.name === selectedRedesignsToCompare[0])
                                                )
                                            )
                                        }
                                    </Col>
                                    <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                        <h4 style={{color: "#666666", textAlign: "center"}}>
                                            {selectedRedesignsToCompare[1]}
                                        </h4>
                                        <h4 style={{color: "#666666", textAlign: "center"}}>
                                            Functionality Complexity: {controller.functionalityRedesigns.find(
                                                e => e.name === selectedRedesignsToCompare[1]
                                            ).functionalityComplexity}
                                            - System Complexity: {controller.functionalityRedesigns.find(
                                                e => e.name === selectedRedesignsToCompare[1]
                                            ).systemComplexity}
                                        </h4>
                                        {
                                            this.renderRedesignGraph(
                                                this.createRedesignGraph(controller.functionalityRedesigns.find(
                                                    e => e.name === selectedRedesignsToCompare[1]
                                                ))
                                            )
                                        }
                                    </Col>
                                </Row>
                            </Container>
                        </div>
                    )
                }
                {
                    showGraph && currentSubView === "Functionality Redesign" && selectedRedesign !== null && (
                        <Container fluid>
                            <Row>
                                <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                    <Button className="mb-2"
                                            onClick={() => this.setState({selectedRedesign: null}, () => this.handleCancel())}>
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
                                            handleSelectOperation = {this.handleSelectOperation}
                                            handleCancel = {this.handleCancel}
                                            handleSubmit = {this.handleSubmit}
                                            DCGISelectCluser = {this.DCGISelectCluster}
                                            handleDCGISelectLocalTransaction = {this.handleDCGISelectLocalTransaction}
                                        />
                                    }
                                </Col>
                                <Col style={{paddingLeft:"0px", paddingRight:"0px"}}>
                                    {this.renderRedesignGraph(redesignVisGraph)}
                                </Col>
                            </Row>
                        </Container>
                    )
                }
            </div>
        );
    }
}
