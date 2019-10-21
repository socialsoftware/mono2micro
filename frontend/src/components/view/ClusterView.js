import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ClusterOperationsMenu, operations } from './ClusterOperationsMenu';
import { VisNetwork } from '../util/VisNetwork';
import { ModalMessage } from '../util/ModalMessage';
import { DataSet } from 'vis';
import { views, types } from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import { Button, ButtonGroup } from 'react-bootstrap';

export const clusterViewHelp = (<div>
    Hover or double click cluster to see entities inside.<br />
    Hover or double click edge to see controllers in common.<br />
    Select cluster or edge for highlight and to open operation menu.
    </div>);

const options = {
    height: "700",
    layout: {
        hierarchical: false
    },
    edges: {
        smooth: false,
        width: 0.5,
        arrows: {
          from: {
            enabled: false,
            scaleFactor: 0.5
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
        hierarchicalRepulsion: {
            centralGravity: 0.0,
            springLength: 500,
            springConstant: 0.01,
            nodeDistance: 100,
            damping: 0.09
        },
        solver: 'hierarchicalRepulsion'
    },
};

export class ClusterView extends React.Component {
    constructor(props) {
        super(props);

         this.state = {
            visGraph: {},
            clusters: [],
            clusterControllers: {},
            showMenu: false,
            selectedCluster: {},
            mergeWithCluster: {},
            transferToCluster: {},
            clusterEntities: [],
            error: false,
            errorMessage: '',
            operation: operations.NONE,
            currentSubView: 'Graph'
        };

        this.setClusterEntities = this.setClusterEntities.bind(this);
        this.handleSelectOperation = this.handleSelectOperation.bind(this);
        this.handleSelectCluster = this.handleSelectCluster.bind(this);
        this.handleSelectEntities = this.handleSelectEntities.bind(this);
        this.handleOperationSubmit = this.handleOperationSubmit.bind(this);
        this.handleOperationCancel = this.handleOperationCancel.bind(this);
        this.closeErrorMessageModal = this.closeErrorMessageModal.bind(this);
    }

    componentDidMount() {
        this.loadGraph();
    }

    loadGraph() {
        const service = new RepositoryService();

        const firstRequest = service.getClusterControllers(this.props.codebaseName, this.props.dendrogramName, this.props.graphName).then(response => {
            this.setState({
                clusterControllers: response.data
            });
        });

        const secondRequest = service.getGraph(this.props.codebaseName, this.props.dendrogramName, this.props.graphName).then(response => {
            this.setState({
                clusters: response.data.clusters,
                showMenu: false,
                selectedCluster: {},
                mergeWithCluster: {},
                transferToCluster: {},
                clusterEntities: [],
                operation: operations.NONE
            });
        });

        Promise.all([firstRequest, secondRequest]).then(() => {
            const visGraph = {
                nodes: new DataSet(this.state.clusters.map(cluster => this.convertClusterToNode(cluster))),
                edges: new DataSet(this.createEdges())
            };

            this.setState({
                visGraph: visGraph
            });
        });
    }

    convertClusterToNode(cluster) {
        return {id: cluster.name, title: cluster.entities.map(e => e.name).sort().join('<br>') + "<br>Total: " + cluster.entities.length, label: cluster.name, value: cluster.entities.length, type: types.CLUSTER};
    };

    createEdges() {
        let edges = [];
        let edgeLengthFactor = 1000;

        for (var i = 0; i < this.state.clusters.length; i++) {
            let cluster1 = this.state.clusters[i];
            let cluster1Controllers = this.state.clusterControllers[cluster1.name].map(c => c.name);
            for (var j = i+1; j < this.state.clusters.length; j++) {
                let cluster2 = this.state.clusters[j];
                let cluster2Controllers = this.state.clusterControllers[cluster2.name].map(c => c.name);
                let controllersInCommon = cluster1Controllers.filter(value => -1 !== cluster2Controllers.indexOf(value))

                let couplingC1C2 = cluster1.couplingDependencies[cluster2.name] === undefined ? 0 : cluster1.couplingDependencies[cluster2.name].length;
                let couplingC2C1 = cluster2.couplingDependencies[cluster1.name] === undefined ? 0 : cluster2.couplingDependencies[cluster1.name].length;


                let edgeTitle = cluster1.name + " -> " + cluster2.name + " , Coupling: " + couplingC1C2 + "<br>";
                edgeTitle += cluster2.name + " -> " + cluster1.name + " , Coupling: " + couplingC2C1 + "<br>";
                edgeTitle += "Controllers in common:<br>"

                let edgeLength = (1/controllersInCommon.length)*edgeLengthFactor;
                if (edgeLength < 100) edgeLength = 300;
                else if (edgeLength > 500) edgeLength = 500;
                
                if (controllersInCommon.length > 0)
                    edges.push({from: cluster1.name, to: cluster2.name, length: edgeLength, value: controllersInCommon.length, label: controllersInCommon.length.toString(), title: edgeTitle + controllersInCommon.join('<br>')});
            }
        }
        return edges;
    }

    setClusterEntities(selectedCluster) {
        this.setState({
            selectedCluster: selectedCluster,
            mergeWithCluster: {},
            clusterEntities: selectedCluster.entities.sort((a, b) => (a.name > b.name) ? 1 : -1)
                                                    .map(e => ({name: e.name, value: e.name, label: e.name, active: false})),
        });
    }

    handleSelectOperation(operation) {
        if (operation === operations.SPLIT || operation === operations.TRANSFER) {
            this.setClusterEntities(this.state.selectedCluster);
            this.setState({
                operation: operation
            });
        } else {
            this.setState({ 
                mergeWithCluster: {},
                transferToCluster: {},
                clusterEntities: [],
                operation: operation 
            });
        }
    }

    handleSelectCluster(nodeId) {
        if (this.state.operation === operations.NONE ||
            this.state.operation === operations.RENAME) {
            this.setState({
                showMenu: true,
                selectedCluster: this.state.clusters.find(c => c.name === nodeId)
            });
        }

        if (this.state.operation === operations.MERGE) {
            const mergeWithCluster = this.state.clusters.find(c => c.name === nodeId);
            if (this.state.selectedCluster === mergeWithCluster) {
                this.setState({
                    error: true,
                    errorMessage: 'Cannot merge a cluster with itself'
                });
            } else {
                this.setState({
                    mergeWithCluster: mergeWithCluster
                });
            }
        }

        if (this.state.operation === operations.TRANSFER) {
            const transferToCluster = this.state.clusters.find(c => c.name === nodeId);
            if (this.state.selectedCluster === transferToCluster) {
                this.setState({
                    error: true,
                    errorMessage: 'Cannot transfer entities to the same cluster'
                });
            } else {
                this.setState({
                    transferToCluster: transferToCluster
                });
            }
        }

        if (this.state.operation === operations.SPLIT) {
            this.setClusterEntities(this.state.clusters.find(c => c.name  === nodeId));
        }
    }

    handleSelectEntities(entities) {
        if (entities === null) {
            const clusterEntities = this.state.clusterEntities.map(e => {
                return {...e, active: false};
            });
            this.setState({
                clusterEntities: clusterEntities
            });
        } else {
            const clusterEntities = this.state.clusterEntities.map(e => {
                if (entities.map(e => e.name).includes(e.name)) {
                    return {...e, active: true};
                } else {
                    return {...e, active: false};
                }
            });
            this.setState({
                clusterEntities: clusterEntities
            });
        }
    }

    handleOperationSubmit(operation, inputValue) {
        const service = new RepositoryService();
        switch (operation) {
            case operations.RENAME:
                service.renameCluster(this.props.codebaseName, this.props.dendrogramName, this.props.graphName, this.state.selectedCluster.name, inputValue)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: rename cluster failed.'
                    });
                });
                break;
            case operations.MERGE:
                service.mergeClusters(this.props.codebaseName, this.props.dendrogramName, this.props.graphName, this.state.selectedCluster.name, 
                    this.state.mergeWithCluster.name, inputValue)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: merge clusters failed.'
                    });
                });
                break;
            case operations.SPLIT:
                let activeClusterEntitiesSplit = this.state.clusterEntities.filter(e => e.active).map(e => e.name).toString();
                service.splitCluster(this.props.codebaseName, this.props.dendrogramName, this.props.graphName, this.state.selectedCluster.name, inputValue, activeClusterEntitiesSplit)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: split cluster failed.'
                    });
                });
                break;
            case operations.TRANSFER:
                let activeClusterEntitiesTransfer = this.state.clusterEntities.filter(e => e.active).map(e => e.name).toString();
                service.transferEntities(this.props.codebaseName, this.props.dendrogramName, this.props.graphName, this.state.selectedCluster.name, 
                    this.state.transferToCluster.name, activeClusterEntitiesTransfer)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: transfer entities failed.'
                    });
                });
                break;
            default:
        }
    }

    handleOperationCancel() {
        this.setState({ 
            showMenu: false,
            selectedCluster: {},
            mergeWithCluster: {},
            transferToCluster: {},
            clusterEntities: [],
            operation: operations.NONE 
        });
    }

    closeErrorMessageModal() {
        this.setState({
            error: false,
            errrorMessage: ''
        });
    }

    handleDeselectNode(nodeId) {

    }

    changeSubView(value) {
        this.setState({
            currentSubView: value
        });
    }

    render() {
        const metricsRows = this.state.clusters.map(cluster => {
            return {
                cluster: cluster.name,
                entities: cluster.entities.length,
                controllers: this.state.clusterControllers[cluster.name].length,
                cohesion: cluster.cohesion,
                coupling: cluster.coupling,
                complexity: cluster.complexity
            }
        });

        const metricsColumns = [{
            dataField: 'cluster',
            text: 'Cluster'
        }, {
            dataField: 'entities',
            text: 'Entities',
            sort: true
        }, {
            dataField: 'controllers',
            text: 'Controllers',
            sort: true
        }, {
            dataField: 'cohesion',
            text: 'Cohesion',
            sort: true
        }, {
            dataField: 'coupling',
            text: 'Coupling',
            sort: true
        }, {
            dataField: 'complexity',
            text: 'Complexity',
            sort: true
        }];

        const couplingRows = this.state.clusters.map(c1 => {
            return Object.assign({id: c1.name}, ...this.state.clusters.map(c2 => {
                return {
                    [c2.name]:  c1.name === c2.name ? "---" :
                                    c1.couplingDependencies[c2.name] === undefined ? 0 : 
                                        c1.couplingDependencies[c2.name].length
                }
            }))
        });

        const couplingColumns = [{dataField: 'id', text: '', style: {fontWeight: 'bold'}}]
            .concat(this.state.clusters.map(c => {
                return {
                    dataField: c.name,
                    text: c.name
                }
            }));

        return (
            <div>
                {this.state.error && 
                <ModalMessage
                    title='Error Message' 
                    message={this.state.errorMessage} 
                    onClose={this.closeErrorMessageModal} />}

                <ButtonGroup className="mb-2">
                    <Button disabled={this.state.currentSubView === "Graph"} onClick={() => this.changeSubView("Graph")}>Graph</Button>
                    <Button disabled={this.state.currentSubView === "Metrics"} onClick={() => this.changeSubView("Metrics")}>Metrics</Button>
                    <Button disabled={this.state.currentSubView === "Coupling Matrix"} onClick={() => this.changeSubView("Coupling Matrix")}>Coupling Matrix</Button>
                </ButtonGroup>

                {this.state.currentSubView === "Graph" &&
                    <span>
                    {this.state.showMenu &&
                    <ClusterOperationsMenu
                        selectedCluster={this.state.selectedCluster}
                        mergeWithCluster={this.state.mergeWithCluster}
                        transferToCluster={this.state.transferToCluster}
                        clusterEntities={this.state.clusterEntities}
                        handleSelectOperation={this.handleSelectOperation}
                        handleSelectEntities={this.handleSelectEntities}
                        handleSubmit={this.handleOperationSubmit}
                        handleCancel={this.handleOperationCancel}
                    />}

                    <div style={{width:'1000px' , height: '700px'}}>
                    <VisNetwork
                        visGraph={this.state.visGraph}
                        options={options}
                        onSelection={this.handleSelectCluster}
                        onDeselection={this.handleDeselectNode}
                        view={views.CLUSTERS} />
                    </div>
                    </span>
                }

                {this.state.currentSubView === "Metrics" &&
                    <BootstrapTable bootstrap4 keyField='cluster' data={ metricsRows } columns={ metricsColumns } />
                }
                
                {this.state.currentSubView === "Coupling Matrix" &&
                    <BootstrapTable bootstrap4 keyField='id' data={ couplingRows } columns={ couplingColumns } />
                }
            </div>
        );
    }
}