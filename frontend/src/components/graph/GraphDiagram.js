import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { OperationsMenu, operations } from './OperationsMenu';
import { VisNetwork } from '../util/VisNetwork';
import { ModalMessage } from '../util/ModalMessage';
import { Tooltip, OverlayTrigger } from 'react-bootstrap';
import { DataSet } from 'vis';

const tooltip = (
    <Tooltip id="tooltip">
      Select node for conditions and<br /> double click them to apply an operation
    </Tooltip>
);

const options = {
    height: "700",
    layout: {
        hierarchical: false
    },
    edges: {
        smooth: false,
        color: '#000000',
        width: 0.5,
        arrows: {
          from: {
            enabled: true,
            scaleFactor: 0.5
          }
        }
    },
    nodes: {
        shape: 'ellipse'
    },
    interaction: {
        hover: true
    }
};

export class GraphDiagram extends React.Component {
    constructor(props) {
        super(props);

         this.state = {
            graph: {},
            clusters: [],
            showMenu: false,
            selectedCluster: {},
            mergeWithCluster: {},
            clusterEntities: [],
            error: false,
            errorMessage: '',
            operation: operations.NONE
        };

        this.loadGraph = this.loadGraph.bind(this);
        this.convertClusterToNode = this.convertClusterToNode.bind(this);
        this.setClusterEntities = this.setClusterEntities.bind(this);
        this.handleSelectOperation = this.handleSelectOperation.bind(this);
        this.handleSelectCluster = this.handleSelectCluster.bind(this);
        this.handleSelectEntity = this.handleSelectEntity.bind(this);
        this.handleOperationSubmit = this.handleOperationSubmit.bind(this);
        this.handleOperationCancel = this.handleOperationCancel.bind(this);
        this.closeErrorMessageModal = this.closeErrorMessageModal.bind(this);
    }

    loadGraph() {
        const service = new RepositoryService();
        service.getGraph(this.props.name).then(response => {

            const graph = {
              nodes: new DataSet(response.data.clusters.map(cluster => this.convertClusterToNode(cluster))),
              edges: new DataSet([])
            };

            this.setState({
                graph: graph,
                clusters: response.data.clusters,
                showMenu: false,
                selectedCluster: {},
                mergeWithCluster: {},
                clusterEntities: [],
                operation: operations.NONE
            });
        });

    }

    convertClusterToNode(cluster) {
        return {id: cluster.name, label: cluster.name};
    };

    setClusterEntities(selectedCluster) {
        this.setState({
            selectedCluster: selectedCluster,
            mergeWithCluster: {},
            clusterEntities: selectedCluster.entities.map(e => ({name: e, active: false})),
            operation: operations.SPLIT
        });
    }

    componentDidMount() {
        this.loadGraph();
    }

    handleSelectOperation(operation) {
        if (operation === operations.SPLIT) {
            this.setClusterEntities(this.state.selectedCluster);
        } else {
            this.setState({ 
                mergeWithCluster: {},
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
                service.renameCluster(this.props.name, this.state.selectedCluster.name, inputValue)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: '+ err.response.data.type + ' - ' + err.response.data.value
                    });
                });
                break;
            case operations.MERGE:
                service.mergeClusters(this.props.name, this.state.selectedCluster.name, 
                    this.state.mergeWithCluster.name, inputValue)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: '+ err.response.data.type + ' - ' + err.response.data.value
                    });
                });
                break;
            case operations.SPLIT:
                const activeClusterEntities = this.state.clusterEntities.filter(e => e.active).map(e => e.name).toString();
                service.splitCluster(this.props.name, this.state.selectedCluster.name, inputValue, activeClusterEntities)
                .then(() => {
                    this.loadGraph();        
                }).catch((err) => {
                    this.setState({
                        error: true,
                        errorMessage: 'ERROR: '+ err.response.data.type + ' - ' + err.response.data.value
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
                    <h3>{this.props.name}</h3>
                </OverlayTrigger><br /><br />
                
                {this.state.error && 
                <ModalMessage 
                    title='Error Message' 
                    message={this.state.errorMessage} 
                    onClose={this.closeErrorMessageModal} />}

                {this.state.showMenu &&
                <OperationsMenu
                    selectedCluster={this.state.selectedCluster}
                    mergeWithCluster={this.state.mergeWithCluster}
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
                        onSelection={this.handleSelectCluster} />
                </div>
            </div>
        );
    }
}