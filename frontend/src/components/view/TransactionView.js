import React from 'react';
import { TransactionOperationsMenu } from './TransactionOperationsMenu';
import { RepositoryService } from '../../services/RepositoryService';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from "vis-network/standalone";
import { views, types } from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';

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

export class TransactionView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            visGraph: {},
            visGraphSeq: {},
            controller: {},
            controllerClusters: [],
            showGraph: false,
            localTransactionsSequence: [],
            currentSubView: "Graph",
            graph: {
                controllers: [],
                clusters: [],
            },
        }

        this.handleControllerSubmit = this.handleControllerSubmit.bind(this);
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

    handleSelectNode(nodeId) {}

    handleDeselectNode(nodeId) {}

    changeSubView(value) {
        this.setState({
            currentSubView: value
        });
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
            </>
        );
    }
}