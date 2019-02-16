import React from 'react';
import { TransactionOperationsMenu } from './TransactionOperationsMenu';
import { RepositoryService } from './../../services/RepositoryService';
import { Tooltip, OverlayTrigger } from 'react-bootstrap';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';

const tooltip = (
    <Tooltip id="tooltip">
      Hover or double click cluster to see entities inside.<br />
      Hover or double click edge to see entities accessed.<br />
      Select cluster or edge for highlight.
    </Tooltip>
);

const options = {
    height: "700",
    layout: {
        hierarchical: {
            direction: 'UD'
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
        hierarchicalRepulsion: {
            springLength: 70,
            nodeDistance: 110
        },
    }
};

export class TransactionView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graphName: this.props.name,
            graph: {},
            controller: '',
            controllers: [],
            controllerClusters: [],
            controllerEntities: [],
            showGraph: false
        }

        this.handleControllerSubmit = this.handleControllerSubmit.bind(this);
        this.loadGraph = this.loadGraph.bind(this);
        this.createNode = this.createNode.bind(this);
        this.createEdge = this.createEdge.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.getControllers().then(response => {
            this.setState({
                controllers: response.data
            })
        });
    }

    handleControllerSubmit(value) {
        this.setState({
            controller: value
        });
        const service = new RepositoryService();
        service.getControllerClusters(this.state.graphName, value).then(response => {
            this.setState({
                controllerClusters: response.data,
                showGraph: true
            });
            this.loadGraph();
        });
    }

    loadGraph() {
        const graph = {
            nodes: new DataSet(this.state.controllerClusters.map(c => this.createNode(c))),
            edges: new DataSet(this.state.controllerClusters.map(c => this.createEdge(c)))
        };

        graph.nodes.add({id: this.state.controller, label: this.state.controller, level: 0, value: 1});

        this.setState({
            graph: graph
        });
    }

    createNode(cluster) {
        return {id: cluster.name, title: cluster.entities.map(e => e.name).join('<br>'), label: cluster.name, value: cluster.entities.length, level: 1};
    }


    createEdge(cluster) {
        return {from: this.state.controller, to: cluster.name};
    }

    handleSelectNode(nodeId) {

    }

    render() {
        return (
            <div>
                <OverlayTrigger placement="bottom" overlay={tooltip}>
                    <h3>{this.state.graphName}</h3>
                </OverlayTrigger>
                <TransactionOperationsMenu
                    handleControllerSubmit={this.handleControllerSubmit}
                    controllers={this.state.controllers}
                />
                <div style={{width:'1000px' , height: '700px'}}>
                    <VisNetwork 
                        graph={this.state.graph}
                        options={options}
                        onSelection={this.handleSelectNode} />
                </div>
            </div>
        );
    }
}