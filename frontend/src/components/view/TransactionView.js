import React from 'react';
import { TransactionOperationsMenu } from './TransactionOperationsMenu';
import { RepositoryService } from '../../services/RepositoryService';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from "vis-network/standalone";
import { views, types } from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import { FunctionalityRedesignMenu, redesignOperations } from './FunctionalityRedesignMenu';
import { ModalMessage } from '../util/ModalMessage';

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
            nodeSpacing: 100
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
            selectedOperation: redesignOperations.NONE,
            selectedLocalTransaction: null,
            newCaller: null,
            addCompensating: false,
            modifiedEntities: null,
            DCGIAvailableClusters: null,
            DCGILocalTransactionsForTheSelectedClusters: null,
            DCGISelectedLocalTransactions: [],
            graph: {
                controllers: [],
                clusters: [],
            },
        }

        this.handleControllerSubmit = this.handleControllerSubmit.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleSelectOperation = this.handleSelectOperation.bind(this);
        this.closeErrorMessageModal = this.closeErrorMessageModal.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.DCGISelectCluster = this.DCGISelectCluster.bind(this);
        this.handleDCGISelectLocalTransaction = this.handleDCGISelectLocalTransaction.bind(this);
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
        this.createRedesignGraph();
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

    handleDeselectNode(nodeId) {}

    createRedesignGraph(){
        let nodes = [];
        let edges = [];
        debugger;
        nodes.push({
            id: -1,
            title: JSON.parse(this.state.controller.functionalityRedesign[0].accessedEntities).map(e => e[0] + " " + e[1]).join('<br>') + "<br>Total: " + Object.keys(this.state.controller.entities).length,
            label: this.state.controller.name,
            level: -1,
            value: 1,
            type: types.CONTROLLER
        });

        let cluster = this.state.graph.clusters.find(cluster => cluster.name === this.state.controller.functionalityRedesign[0].cluster);
        nodes.push({
            id: this.state.controller.functionalityRedesign[0].id,
            title: cluster.entities.map(e => e.name).join('<br>') + "<br>Total: " + cluster.entities.length,
            label: this.state.controller.functionalityRedesign[0].id + ": " + this.state.controller.functionalityRedesign[0].cluster,
            level: 0,
            type: types.CLUSTER
        });

        edges.push({
            from: -1,
            to: 0,
            title: JSON.parse(this.state.controller.functionalityRedesign[0].accessedEntities).map(e => e.join(" ")).join('<br>'),
            label: JSON.parse(this.state.controller.functionalityRedesign[0].accessedEntities).length.toString()
        });

        for(let i = 0; i < nodes.length; i++){
            if(nodes[i].id >= 0) {
                let localTransaction = this.state.controller.functionalityRedesign.find(entry => entry.id === nodes[i].id.toString());
                localTransaction.remoteInvocations.forEach((id) => {
                    let lt = this.state.controller.functionalityRedesign.find(e => e.id === id.toString());
                    cluster = this.state.graph.clusters.find(c => c.name === lt.cluster);

                    nodes.push({
                        id: lt.id,
                        title: cluster.entities.map(e => e.name).join('<br>') + "<br>Total: " + cluster.entities.length,
                        label: lt.id + ": " + lt.cluster,
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

        const redesignVisGraph = {
            nodes: new DataSet(nodes),
            edges: new DataSet(edges)
        };

        this.setState({
            redesignVisGraph: redesignVisGraph
        });
    }

    identifyModifiedEntities(cluster){
        return {
            cluster: cluster.name,
            modifiedEntities: Object.entries(this.state.controller.entities)
                .filter(e => cluster.entities.map(e => e.name).includes(e[0]))
                .filter(e => e[1].includes("W"))
                .map(e => e[0])
        }
    }


    handleSelectNode(nodeId) {
        if(this.state.selectedOperation === redesignOperations.NONE) {
            this.setState({
                showMenu: true,
                selectedLocalTransaction: this.state.controller.functionalityRedesign.find(c => c.id === nodeId)
            });
        }

        console.log("NodeID: " + nodeId);
        console.log(this.state.selectedLocalTransaction);

        if(this.state.selectedOperation === redesignOperations.SQ){
            if(nodeId === this.state.selectedLocalTransaction.id){
                this.setState({
                    error: true,
                    errorMessage: "One local transaction cannot call itself"
                });
            } else if(nodeId === this.state.selectedLocalTransaction.id - 1) {
                this.setState({
                    error: true,
                    errorMessage: "The local transaction " + nodeId
                        + " is already invoking local transaction " + this.state.selectedLocalTransaction.id
                });
            } else {
                console.log(this.state.controller.functionalityRedesign);

                this.setState({
                   newCaller: this.state.controller.functionalityRedesign.find(c => c.id === nodeId.toString())
                });
            }
        }
        else if(this.state.selectedOperation === redesignOperations.DCGI){
            if(!this.state.DCGISelectedLocalTransactions.map(e => e.id).includes(nodeId)){
                const aux = this.state.DCGISelectedLocalTransactions;
                aux.push(this.state.DCGILocalTransactionsForTheSelectedClusters.find(e => e.id === nodeId));
                this.setState({
                    DCGISelectedLocalTransactions: aux
                });
            }
        }
    }

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
                DCGISelectedCluster: [this.state.selectedLocalTransaction.cluster]
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
        console.log("Teste");

        const service = new RepositoryService();
        switch (this.state.selectedOperation) {
            case redesignOperations.AC:
                service.addCompensating(this.props.codebaseName, this.props.dendrogramName, this.props.graphName,
                    this.state.controller.name, value.cluster, value.entities, this.state.selectedLocalTransaction.id)
                    .then(response => {
                        const newController = this.state.controller;
                        newController.functionalityRedesign = response.data;
                        this.setState({
                            controller: newController
                        }, () => {
                            this.createRedesignGraph();
                        });
                    }).catch((err) => {
                        this.setState({
                            error: true,
                            errorMessage: 'ERROR: Add Compensating failed.'
                        });
                    });
                break;
            case redesignOperations.SQ:
                break;
            case redesignOperations.DCGI:
                break;
            default:
                break;
        }
    }

    handleCancel(){
        this.setState({
            showMenu: true,
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
        const selectedClusters = this.state.DCGISelectedCluster;
        selectedClusters.push(value);

        const localTransactionsForTheSelectedClusters =
            this.state.controller.functionalityRedesign.filter(e => selectedClusters.includes(e.cluster));

        this.setState({
            DCGILocalTransactionsForTheSelectedClusters: localTransactionsForTheSelectedClusters
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
                .filter(e => value.map(e => e.value).includes(e.id));
            this.setState({
                DCGISelectedLocalTransactions: selectedLocalTransactions,
            });
        }
    }

    render() {

        const {
            controllerClusters,
            currentSubView,
            visGraph,
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
            return {
                controller: controller.name,
                clusters: controllerClusters[controller.name] === undefined ? 0 : controllerClusters[controller.name].length,
                complexity: controller.complexity
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
            <>
                {
                    this.state.error && (
                        <ModalMessage
                            title='Error Message'
                            message={this.state.errorMessage}
                            onClose={this.closeErrorMessageModal}
                        />
                    )
                }
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

                {currentSubView === "Graph" &&
                    <span>
                        <TransactionOperationsMenu
                            handleControllerSubmit={this.handleControllerSubmit}
                            controllerClusters={controllerClusters}
                        />
                        <div style={{ width: '1000px', height: '700px' }}>
                            <VisNetwork
                                visGraph={visGraph}
                                options={options}
                                onSelection={this.handleSelectNode}
                                onDeselection={this.handleDeselectNode}
                                view={views.TRANSACTION}
                                subView={views.GRAPH}
                            />
                        </div>
                    </span>
                }

                {currentSubView === "Sequence Graph" &&
                    <div style={{ width: '1000px', height: '700px' }}>
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

                {this.state.showGraph && this.state.currentSubView === "Functionality Redesign" &&
                    <div>
                        {this.state.showMenu &&
                            <FunctionalityRedesignMenu
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

                        <div style={{display:'none'}}>
                            {/*this div functions as a "cache". Is is used to render the graph with the optionsSeq
                            options in order to save the positions such that when the graph is generated with the
                            optionsFunctionalityRedesign options is much quicker and there is no buffering*/}
                            <VisNetwork
                                visGraph={this.state.redesignVisGraph}
                                options={optionsSeq}
                                onSelection={this.handleSelectNode}
                                onDeselection={this.handleDeselectNode}
                                view={views.TRANSACTION}
                            />
                        </div>
                        <h4>{this.state.controller.name}</h4>
                        <div style={{width:'1000px' , height: '700px'}}>
                            <VisNetwork
                                visGraph={this.state.redesignVisGraph}
                                options={optionsFunctionalityRedesign}
                                onSelection={this.handleSelectNode}
                                onDeselection={this.handleDeselectNode}
                                view={views.TRANSACTION}
                            />
                        </div>
                    </div>
                }
            </>
        );
    }
}
