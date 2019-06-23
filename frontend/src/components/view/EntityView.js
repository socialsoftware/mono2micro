import React from 'react';
import { EntityOperationsMenu } from './EntityOperationsMenu';
import { RepositoryService } from '../../services/RepositoryService';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';
import { views, types } from './ViewsMenu';
import BootstrapTable from 'react-bootstrap-table-next';
import { Button, ButtonGroup} from 'react-bootstrap';

export const entityViewHelp = (<div>
    Hover entity to see controllers that access it.< br/>
    Hover edge to see controllers that access the entity and the cluster.< br/>
    Hover cluster to see entities inside.< br/>
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
            entity: "",
            entities: [],
            clusters: [],
            controllers: [],
            clusterControllers: {},
            showGraph: false,
            amountList: [],
            currentSubView: "Graph"
        }

        this.handleEntitySubmit = this.handleEntitySubmit.bind(this);
        this.loadGraph = this.loadGraph.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleDeselectNode = this.handleDeselectNode.bind(this);
        this.getCommonControllers = this.getCommonControllers.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.getDendrogram(this.props.codebaseName, this.props.dendrogramName).then(response => {
            service.getClusterControllers(this.props.codebaseName, this.props.dendrogramName, this.props.graphName).then(response2 => {
                this.setState({
                    graph: response.data.graphs.filter(graph => graph.name === this.props.graphName)[0],
                    clusters: response.data.graphs.filter(graph => graph.name === this.props.graphName)[0].clusters,
                    entities: response.data.entities,
                    controllers: response.data.controllers,
                    clusterControllers: response2.data
                }, () => {
                    let amountList = {};
                    for (var i = 0; i < this.state.entities.length; i++) {
                        let amount = 0;
                        let entity = this.state.entities[i];
                        let entityCluster = this.state.clusters.filter(c => c.entities.includes(entity.name))[0];
                        for (var j = 0; j < this.state.clusters.length; j++) {
                            let cluster = this.state.clusters[j];
                            let commonControllers = this.getCommonControllers(entity.name, cluster);
                            if (cluster.name !== entityCluster.name && commonControllers.length > 0) {
                                amount += 1;
                            }
                        }
                        amountList[entity.name] = amount;
                    }
                    this.setState({
                        amountList: amountList
                    });
                });
            });
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            graph: {...this.state.graph, name: nextProps.graphName}
        });
    }

    handleEntitySubmit(value) {
        this.setState({
            entity: value,
            entityCluster: this.state.clusters.filter(c => c.entities.includes(value))[0],
            showGraph: true
        }, () => {
            this.loadGraph();
        });
    }

    //get controllers that access both an entity and another cluster
    getCommonControllers(entityName, cluster) {
        let entityControllers = this.state.controllers.filter(controller => Object.keys(controller.entities).includes(entityName)).map(c => c.name);
        let clusterControllers = this.state.clusterControllers[cluster.name].map(c => c.name);
        return entityControllers.filter(c => clusterControllers.includes(c));
    }

    loadGraph() {
        let nodes = [];
        let edges = [];
        let entityControllers = this.state.controllers.filter(controller => Object.keys(controller.entities).includes(this.state.entity)).map(c => c.name + " " + c.entities[this.state.entity]);

        nodes.push({id: this.state.entity, label: this.state.entity, value: 1, level: 0, type: types.ENTITY, title: entityControllers.join('<br>')});
        
        for (var i = 0; i < this.state.clusters.length; i++) {
            let cluster = this.state.clusters[i];
            if (cluster.name !== this.state.entityCluster.name) {
                let commonControllers = this.getCommonControllers(this.state.entity, cluster);
                
                if (commonControllers.length > 0) {
                    nodes.push({id: cluster.name, label: cluster.name, value: cluster.entities.length, level: 1, type: types.CLUSTER, title: cluster.entities.join('<br>') + "<br>Total: " + cluster.entities.length});
                    edges.push({from: this.state.entity, to: cluster.name, label: commonControllers.length.toString(), title: commonControllers.join('<br>')})
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

    changeSubView(value) {
        this.setState({
            currentSubView: value
        });
    }

    render() {
        const metricsRows = this.state.entities.map(e => {
            return {
                entity: e.name,
                immutability: Number(e.immutability.toFixed(2))
            }
        });

        const metricsColumns = [{
            dataField: 'entity',
            text: 'Entity',
            sort: true
        }, {
            dataField: 'immutability',
            text: 'Immutability',
            sort: true
        }];

        return (
            <div>
                <ButtonGroup className="mb-2">
                    <Button disabled={this.state.currentSubView === "Graph"} onClick={() => this.changeSubView("Graph")}>Graph</Button>
                    <Button disabled={this.state.currentSubView === "Metrics"} onClick={() => this.changeSubView("Metrics")}>Metrics</Button>
                </ButtonGroup>

                {this.state.currentSubView === "Graph" &&
                    <span>
                    <EntityOperationsMenu
                        handleEntitySubmit={this.handleEntitySubmit}
                        entities={this.state.entities}
                        amountList={this.state.amountList}
                    />
                    
                    <div style={{width:'1000px' , height: '700px'}}>
                        <VisNetwork
                            visGraph={this.state.visGraph}
                            options={options}
                            onSelection={this.handleSelectNode}
                            onDeselection={this.handleDeselectNode}
                            view={views.ENTITY} />
                    </div>
                    </span>
                }

                {this.state.currentSubView === "Metrics" &&
                    <div>
                        <BootstrapTable bootstrap4 keyField='entity' data={ metricsRows } columns={ metricsColumns } />
                    </div>
                }
            </div>
        );
    }
}