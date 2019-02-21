import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ClusterOperationsMenu, operations } from './ClusterOperationsMenu';
import { VisNetwork } from '../util/VisNetwork';
import { ModalMessage } from '../util/ModalMessage';
import { Tooltip, OverlayTrigger } from 'react-bootstrap';
import { DataSet } from 'vis';
import { views, types } from './ViewsMenu';

const tooltip = (
    <Tooltip id="tooltip">
      Hover or double click cluster to see entities inside.<br />
      Hover or double click edge to see controllers in common.<br />
      Select cluster or edge for highlight and to open operation menu.
    </Tooltip>
);

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
            graphName: this.props.name,
            graph: {},
            clusters: [],
            showMenu: false,
            selectedCluster: {},
            mergeWithCluster: {},
            transferToCluster: {},
            clusterEntities: [],
            error: false,
            errorMessage: '',
            operation: operations.NONE
        };

        this.loadGraph = this.loadGraph.bind(this);
        this.convertClusterToNode = this.convertClusterToNode.bind(this);
        this.createEdges = this.createEdges.bind(this);
        this.setClusterEntities = this.setClusterEntities.bind(this);
        this.handleSelectOperation = this.handleSelectOperation.bind(this);
        this.handleSelectCluster = this.handleSelectCluster.bind(this);
        this.handleSelectEntity = this.handleSelectEntity.bind(this);
        this.handleOperationSubmit = this.handleOperationSubmit.bind(this);
        this.handleOperationCancel = this.handleOperationCancel.bind(this);
        this.closeErrorMessageModal = this.closeErrorMessageModal.bind(this);
    }

    loadGraph(graphName) {
        const service = new RepositoryService();
        service.getGraph(graphName).then(response => {

            const graph = {
              nodes: new DataSet(response.data.clusters.map(cluster => this.convertClusterToNode(cluster))),
              edges: new DataSet(this.createEdges(response.data.clusters))
            };

            this.setState({
                graph: graph,
                clusters: response.data.clusters,
                showMenu: false,
                selectedCluster: {},
                mergeWithCluster: {},
                transferToCluster: {},
                clusterEntities: [],
                operation: operations.NONE
            });
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({graphName: nextProps.name});
        this.loadGraph(nextProps.name);
    }

    createEdges(clusters) {
        let edges = [];
        let edgeLengthFactor = 1000;
        for (var i = 0; i < clusters.length; i++) { 
            for (var j = i+1; j < clusters.length; j++) {
                let cluster1Controllers = [...new Set(clusters[i].entities.map(e => e.controllers).flat())];
                let cluster2Controllers = [...new Set(clusters[j].entities.map(e => e.controllers).flat())];
                let ControllersInCommon = cluster1Controllers.filter(value => -1 !== cluster2Controllers.indexOf(value))
                let edgeTitle = clusters[i].name + " -- " + clusters[j].name + "<br>";
                let edgeLength = (1/ControllersInCommon.length)*edgeLengthFactor;
                if (edgeLength < 100) edgeLength = 300;
                else if (edgeLength > 500) edgeLength = 500;
                if (ControllersInCommon.length > 0)
                    //edges.push({from: clusters[i].name, to: clusters[j].name, value: ControllersInCommon.length, label: ControllersInCommon.length.toString(), title: edgeTitle + ControllersInCommon.join('<br>')});
                    edges.push({from: clusters[i].name, to: clusters[j].name, length:edgeLength, value: ControllersInCommon.length, label: ControllersInCommon.length.toString(), title: edgeTitle + ControllersInCommon.join('<br>')});
                //else
                    //edges.push({from: clusters[i].name, to: clusters[j].name, length:(1/0.5)*edgeLengthFactor, hidden: true});
            }
        }
        return edges;
    }

    convertClusterToNode(cluster) {
        return {id: cluster.name, title: cluster.entities.map(e => e.name).join('<br>'), label: cluster.name, value: cluster.entities.length, type: types.CLUSTER};
    };

    setClusterEntities(selectedCluster) {
        this.setState({
            selectedCluster: selectedCluster,
            mergeWithCluster: {},
            clusterEntities: selectedCluster.entities.map(e => ({name: e.name, active: false})),
        });
    }

    componentDidMount() {
        this.loadGraph(this.state.graphName);
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

    handleSelectEntity(entityName) {
        const clusterEntities = this.state.clusterEntities.map(e => {
            if (e.name === entityName) {
                return {...e, active: !e.active};
            } else {
                return e;
            }
        });
        this.setState({
            clusterEntities: clusterEntities
        });
    }

    handleOperationSubmit(operation, inputValue) {
        const service = new RepositoryService();
        switch (operation) {
            case operations.RENAME:
                service.renameCluster(this.state.graphName, this.state.selectedCluster.name, inputValue)
                .then(() => {
                    this.loadGraph(this.state.graphName);        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: rename cluster failed.'
                    });
                });
                break;
            case operations.MERGE:
                service.mergeClusters(this.state.graphName, this.state.selectedCluster.name, 
                    this.state.mergeWithCluster.name, inputValue)
                .then(() => {
                    this.loadGraph(this.state.graphName);        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: merge clusters failed.'
                    });
                });
                break;
            case operations.SPLIT:
                let activeClusterEntitiesSplit = this.state.clusterEntities.filter(e => e.active).map(e => e.name).toString();
                service.splitCluster(this.state.graphName, this.state.selectedCluster.name, inputValue, activeClusterEntitiesSplit)
                .then(() => {
                    this.loadGraph(this.state.graphName);        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: split cluster failed.'
                    });
                });
                break;
            case operations.TRANSFER:
                let activeClusterEntitiesTransfer = this.state.clusterEntities.filter(e => e.active).map(e => e.name).toString();
                service.transferEntities(this.state.graphName, this.state.selectedCluster.name, 
                    this.state.transferToCluster.name, activeClusterEntitiesTransfer)
                .then(() => {
                    this.loadGraph(this.state.graphName);        
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

    render() {
        return (
            <div>
                <OverlayTrigger placement="bottom" overlay={tooltip}>
                    <h3>{this.state.graphName}</h3>
                </OverlayTrigger><br />
                
                {this.state.error && 
                <ModalMessage
                    title='Error Message' 
                    message={this.state.errorMessage} 
                    onClose={this.closeErrorMessageModal} />}

                {this.state.showMenu &&
                <ClusterOperationsMenu
                    selectedCluster={this.state.selectedCluster}
                    mergeWithCluster={this.state.mergeWithCluster}
                    transferToCluster={this.state.transferToCluster}
                    clusterEntities={this.state.clusterEntities}
                    handleSelectOperation={this.handleSelectOperation}
                    handleSelectEntity={this.handleSelectEntity}
                    handleSubmit={this.handleOperationSubmit}
                    handleCancel={this.handleOperationCancel}
                />}

                <div style={{width:'1000px' , height: '700px'}}>
                    <VisNetwork 
                        graph={this.state.graph} 
                        clusters={this.state.clusters} 
                        options={options} 
                        onSelection={this.handleSelectCluster} 
                        view={views.CLUSTERS} />
                </div>
            </div>
        );
    }
}