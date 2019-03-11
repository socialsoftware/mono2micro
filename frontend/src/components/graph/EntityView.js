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
        hierarchical: false
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
        barnesHut: {
            gravitationalConstant: -2000,
            centralGravity: 0.3,
            springLength: 95,
            springConstant: 0.04,
            damping: 0.09,
            avoidOverlap: 1
          },
    }
};

export class EntityView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graphName: this.props.name,
            graph: {},
            entity: {},
            entities: [],
            showGraph: false
        }

        this.handleEntitySubmit = this.handleEntitySubmit.bind(this);
        this.loadGraph = this.loadGraph.bind(this);
        this.createNode = this.createNode.bind(this);
        this.createEdge = this.createEdge.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.getEntities().then(response => {
            this.setState({
                entities: response.data
            });
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
        const graph = {
            nodes: new DataSet(this.state.entity.controllers.map(c => this.createNode(c))),
            edges: new DataSet(this.state.entity.controllers.map(c => this.createEdge(c)))
        };
        graph.nodes.add({id: this.state.entity.name, label: this.state.entity.name, value: 2, type: types.ENTITY});

        this.setState({
            graph: graph
        });
    }

    createNode(controller) {
        return {id: controller, label: controller, value: 1, type: types.CONTROLLER};
    }


    createEdge(controller) {
        return {from: controller, to: this.state.entity.name};
    }

    handleSelectNode(nodeId) {

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
                        graph={this.state.graph}
                        options={options}
                        onSelection={this.handleSelectNode}
                        view={views.ENTITY} />
                </div>
            </div>
        );
    }
}