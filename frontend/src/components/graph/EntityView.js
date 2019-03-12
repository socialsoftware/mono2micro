import React from 'react';
import { EntityOperationsMenu } from './EntityOperationsMenu';
import { RepositoryService } from './../../services/RepositoryService';
import { Tooltip } from 'react-bootstrap';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';
import { views, types } from './ViewsMenu';

export const entity_tooltip = (
    <Tooltip id="entity_tooltip">
      Hover or double click cluster to see entities inside.<br />
      Hover or double click edge to see entities accessed.<br />
      Select cluster or edge for highlight.
    </Tooltip>
);

const options = {
    height: "700",
    layout: {
        hierarchical: {
            direction: 'LR',
            nodeSpacing: 30
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

export class EntityView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graph: {},
            visGraph: {},
            entity: {},
            entities: [],
            controllers: [],
            controllerClusters: {},
            showGraph: false
        }

        this.handleEntitySubmit = this.handleEntitySubmit.bind(this);
        this.loadGraph = this.loadGraph.bind(this);
        this.createNode = this.createNode.bind(this);
        this.createEdge = this.createEdge.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleDeselectNode = this.handleDeselectNode.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.loadDendrogram().then(response => {
            this.setState({
                entities: response.data.entities,
                controllers: response.data.controllers,
                graph: response.data.graphs.filter(g => g.name === this.props.name)[0]
            });
        });

        service.getControllerClusters(this.props.name).then(response => {
            this.setState({
                controllerClusters: response.data
            });
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            graph: {...this.state.graph, name: nextProps.name}
        });
    }

    handleEntitySubmit(value) {
        this.setState({
            entity: this.state.entities.filter(e => e.name === value)[0],
            showGraph: true
        }, () => {
            this.loadGraph();
            }
        );
    }

    loadGraph() {
        const visGraph = {
            nodes: new DataSet(this.state.entity.controllers.map(c => this.createNode(c))),
            edges: new DataSet(this.state.entity.controllers.map(c => this.createEdge(c)))
        };
        
        visGraph.nodes.add({id: this.state.entity.name, label: this.state.entity.name, value: 2, level: 0, type: types.ENTITY});

        visGraph.nodes.add(this.state.graph.clusters.map(cluster => this.createClusterNode(cluster.name)));
        
        for (var i = 0; i < this.state.controllers.length; i++) {
            visGraph.edges.add(this.state.controllerClusters[this.state.controllers[i].name].map(cluster => this.createClusterEdge(this.state.controllers[i].name, cluster.name)));
        }
        /*this.state.controllers.forEach(function(controller){
            visGraph.edges.add(this.state.controllerClusters[controller.name].map(cluster => this.createClusterEdge(controller.name, cluster.name)));
        });*/
        
        this.setState({
            visGraph: visGraph
        });
    }

    createNode(controller) {
        return {id: controller, label: controller, value: 1, level: 2, type: types.CONTROLLER};
    }

    createClusterNode(cluster) {
        return {id: cluster, label: cluster, value: 1, level: 4, type: types.CLUSTER, hidden: true};
    }


    createEdge(controller) {
        return {from: controller, to: this.state.entity.name};
    }

    createClusterEdge(nodeId, cluster) {
        return {from: nodeId, to: cluster, hidden: true};
    }

    handleSelectNode(nodeId) {
        let node = this.state.visGraph.nodes.get(nodeId);
        if (node.type === types.CONTROLLER) {
            this.state.visGraph.nodes.update(this.state.controllerClusters[node.id].map(c => ({id: c.name, hidden: false})));
            this.state.visGraph.edges.update(this.state.visGraph.edges.map(e=>e).filter(e => e.from === nodeId && !this.state.entities.map(e => e.name).includes(e.to)).map(e => ({id: e.id, hidden: false})));
        }
    }

    handleDeselectNode(nodeId) {
        let node = this.state.visGraph.nodes.get(nodeId);
        if (node.type === types.CONTROLLER) {
            this.state.visGraph.nodes.update(this.state.controllerClusters[node.id].map(c => ({id: c.name, hidden: true})));
            this.state.visGraph.edges.update(this.state.visGraph.edges.map(e=>e).filter(e => e.from === nodeId && !this.state.entities.map(e => e.name).includes(e.to)).map(e => ({id: e.id, hidden: true})));
        }
    }

    render() {
        return (
            <div>
                <EntityOperationsMenu
                    handleEntitySubmit={this.handleEntitySubmit}
                    entities={this.state.entities}
                />
                
                <div style={{width:'1000px' , height: '700px'}}>
                    <VisNetwork 
                        graph={this.state.visGraph}
                        options={options}
                        onSelection={this.handleSelectNode}
                        onDeselection={this.handleDeselectNode}
                        view={views.ENTITY} />
                </div>
            </div>
        );
    }
}