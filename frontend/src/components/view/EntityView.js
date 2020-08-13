import React from 'react';
import { EntityOperationsMenu } from './EntityOperationsMenu';
import { RepositoryService } from '../../services/RepositoryService';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';
import { views, types } from './Views';
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
        const {
            codebaseName,
            dendrogramName,
            graphName,
        } = this.props;

        const service = new RepositoryService();
        service.getGraph(codebaseName, dendrogramName, graphName).then(response => {
            service.getClusterControllers(codebaseName, dendrogramName, graphName).then(response2 => {
                
                this.setState({
                    graph: response.data,
                    clusters: response.data.clusters,
                    entities: response.data.clusters.map(c => Object.keys(c.entities)).flat(),
                    clusterControllers: response2.data,
                    controllers: response.data.controllers
                }, () => {
                    let amountList = {};

                    const {
                        entities,
                        clusters,
                    } = this.state;
                    
                    for (var i = 0; i < entities.length; i++) {
                        let amount = 0;
                        let entityName = entities[i];

                        let entityCluster = clusters.filter(
                            c => Object.keys(c.entities).includes(entityName)
                        )[0];
                        
                        for (var j = 0; j < clusters.length; j++) {
                            let cluster = clusters[j];
                            let commonControllers = this.getCommonControllers(entityName, cluster);
                            
                            if (cluster.name !== entityCluster.name && commonControllers.length > 0) {
                                amount += 1;
                            }
                        }
                        amountList[entityName] = amount;
                    }
                    this.setState({
                        amountList: amountList
                    });
                });
            });
        });
    }

    handleEntitySubmit(value) {
        this.setState({
            entity: value,
            entityCluster: this.state.clusters.find(c => Object.keys(c.entities).includes(value)),
            showGraph: true
        }, () => {
            this.loadGraph();
        });
    }

    //get controllers that access both an entity and another cluster
    getCommonControllers(entityName, cluster) {
        let entityControllers = this.state.controllers
                                    .filter(controller => Object.keys(controller.entities)
                                    .includes(entityName))
                                    .map(c => c.name);

        let clusterControllers = this.state.clusterControllers[cluster.name].map(c => c.name);
        return entityControllers.filter(c => clusterControllers.includes(c));
    }

    loadGraph() {
        const {
            controllers,
            entity,
            clusters,
            entityCluster,
        } = this.state;

        let nodes = [];
        let edges = [];
        let entityControllers = controllers
                                    .filter(controller => Object.keys(controller.entities)
                                    .includes(this.state.entity))
                                    .map(c => c.name + " " + c.entities[this.state.entity]);

        nodes.push({
            id: entity,
            label: entity,
            value: 1,
            level: 0,
            type: types.ENTITY,
            title: entityControllers.join('<br>')
        });
        
        for (var i = 0; i < clusters.length; i++) {
            let cluster = clusters[i];
            let clusterEntities = Object.keys(cluster.entities)

            if (cluster.name !== entityCluster.name) {
                let commonControllers = this.getCommonControllers(entity, cluster);
                
                if (commonControllers.length > 0) {
                    nodes.push({
                        id: cluster.name,
                        label: cluster.name,
                        value: clusterEntities.length,
                        level: 1,
                        type: types.CLUSTER,
                        title: clusterEntities.map(e => e).join('<br>') + "<br>Total: " + clusterEntities.length
                    });

                    edges.push({
                        from: entity,
                        to: cluster.name,
                        label: commonControllers.length.toString(),
                        title: commonControllers.join('<br>')
                    })
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

    handleSelectNode(nodeId) {}

    handleDeselectNode(nodeId) {}

    changeSubView(value) {
        this.setState({
            currentSubView: value
        });
    }

    render() {
        const {
            entities,
            currentSubView,
            amountList,
            visGraph,
        } = this.state;
        const metricsRows = entities;

        const metricsColumns = [{
            dataField: 'entity',
            text: 'Entity',
            sort: true
        }];

        return (
            <div>
                <ButtonGroup className="mb-2">
                    <Button
                        disabled={currentSubView === "Graph"}
                        onClick={() => this.changeSubView("Graph")}
                    >
                        Graph
                    </Button>
                    <Button
                        disabled={currentSubView === "Metrics"}
                        onClick={() => this.changeSubView("Metrics")}
                    >
                        Metrics
                    </Button>
                </ButtonGroup>

                {currentSubView === "Graph" &&
                    <span>
                    <EntityOperationsMenu
                        handleEntitySubmit={this.handleEntitySubmit}
                        entities={entities}
                        amountList={amountList}
                    />
                    
                    <div style={{width:'1000px' , height: '700px'}}>
                        <VisNetwork
                            visGraph={visGraph}
                            options={options}
                            onSelection={this.handleSelectNode}
                            onDeselection={this.handleDeselectNode}
                            view={views.ENTITY}
                        />
                    </div>
                    </span>
                }

                {currentSubView === "Metrics" &&
                    <div>
                        <BootstrapTable
                            bootstrap4
                            keyField='entity'
                            data={ metricsRows }
                            columns={ metricsColumns }
                        />
                    </div>
                }
            </div>
        );
    }
}