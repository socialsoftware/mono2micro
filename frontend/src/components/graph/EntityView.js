import React from 'react';
import { EntityOperationsMenu } from './EntityOperationsMenu';
import { RepositoryService } from './../../services/RepositoryService';
import { Tooltip } from 'react-bootstrap';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';
import { views, types } from './ViewsMenu';

export const entityViewHelp = (<div>
    Hover or double click cluster to see entities inside.<br />
    Hover or double click edge to see entities accessed.<br />
    Select cluster or edge for highlight.
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
            enabled: false,
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
            showGraph: false,
            amountList: [],
            excludeControllerList: []
        }

        this.handleEntitySubmit = this.handleEntitySubmit.bind(this);
        this.loadGraph = this.loadGraph.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleDeselectNode = this.handleDeselectNode.bind(this);
        this.handleExcludeController = this.handleExcludeController.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.loadDendrogram().then(response => {
            this.setState({
                entities: response.data.entities,
                controllers: response.data.controllers,
                graph: response.data.graphs.filter(g => g.name === this.props.name)[0]
            }, () => {
                let amountList = {};
                for (var j = 0; j < this.state.entities.length; j++) {
                    let amount = 0;
                    let entity = this.state.entities[j];
                    let entityCluster = this.state.graph.clusters.filter(c => c.entities.map(e => e.name).includes(entity.name))[0];
                    for (var i = 0; i < this.state.graph.clusters.length; i++) {
                        let cluster = this.state.graph.clusters[i];
                        if (cluster.name !== entityCluster.name) {
                            let entityControllers = entity.controllers;
                            let clusterControllers = [...new Set(cluster.entities.map(e => e.controllers).flat())];
                            let commonControllers = entityControllers.filter(value => clusterControllers.includes(value));
                            
                            if (commonControllers.length > 0) {
                                amount += 1;
                            }
                        }
                    }
                    amountList[this.state.entities[j].name] = amount;
                }
                this.setState({
                    amountList: amountList
                });
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
            entityCluster: this.state.graph.clusters.filter(c => c.entities.map(e => e.name).includes(value))[0],
            showGraph: true
        }, () => {
            this.loadGraph();
            }
        );
    }

    loadGraph() {
        let nodes = [];
        let edges = [];

        nodes.push({id: this.state.entity.name, label: this.state.entity.name, value: 1, level: 0, type: types.ENTITY, title: this.state.entity.controllers.join('<br>')});
        
        for (var i = 0; i < this.state.graph.clusters.length; i++) {
            let cluster = this.state.graph.clusters[i];
            if (cluster.name !== this.state.entityCluster.name) {
                let entityControllers = this.state.entity.controllers;
                let clusterControllers = [...new Set(cluster.entities.map(e => e.controllers).flat())];
                let commonControllers = entityControllers.filter(value => clusterControllers.includes(value));
                
                commonControllers = commonControllers.filter(c => !this.state.excludeControllerList.includes(c));
                
                if (commonControllers.length > 0) {
                    nodes.push({id: cluster.name, label: cluster.name, value: cluster.entities.length, level: 1, type: types.CLUSTER, title: cluster.entities.map(e => e.name).join('<br>')});
                    edges.push({from: this.state.entity.name, to: cluster.name, label: commonControllers.length.toString(), title: commonControllers.join('<br>')})
                }
            }
        }

        const visGraph = {
            nodes: new DataSet(nodes),
            edges: new DataSet(edges)
        };
        
        this.setState({
            visGraph: visGraph
        });
    }

    handleSelectNode(nodeId) {

    }

    handleDeselectNode(nodeId) {

    }

    handleExcludeController(controller) {
        this.state.excludeControllerList.push(controller)
        this.setState({
          excludeControllerList: this.state.excludeControllerList
        });
    }

    render() {
        return (
            <div>
                <EntityOperationsMenu
                    handleEntitySubmit={this.handleEntitySubmit}
                    handleExcludeController={this.handleExcludeController}
                    entities={this.state.entities}
                    amountList={this.state.amountList}
                    controllers={this.state.controllers}
                    excludeControllerList={this.state.excludeControllerList}
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