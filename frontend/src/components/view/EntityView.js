import React, {useContext, useEffect, useState} from 'react';
import { EntityOperationsMenu } from './EntityOperationsMenu';
import { RepositoryService } from '../../services/RepositoryService';
import { VisNetwork } from '../util/VisNetwork';
import { DataSet } from 'vis';
import { views, types } from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import AppContext from "./../AppContext";
import {useParams} from "react-router-dom";

export const entityViewHelp = (<div>
    Hover entity to see functionalities that access it.< br />
    Hover edge to see functionalities that access the entity and the cluster.< br />
    Hover cluster to see entities inside.< br />
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

export const EntityView = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [state, changeState] = useState({
        visGraph: {},
        entity: "",
        entities: [],
        clusters: [],
        functionalities: [],
        clustersFunctionalities: {},
        amountList: [],
        currentSubView: "Graph"
    });

    //Executed on mount
    useEffect(() => {
        const service = new RepositoryService();

        service.getDecomposition(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            service.getClustersFunctionalities(
                codebaseName,
                strategyName,
                decompositionName
            ).then(response2 => {
                let entities = Object.values(response.data.clusters).map(c => c.entities).flat();
                let clusters = Object.values(response.data.clusters);
                let functionalities = Object.values(response.data.functionalities);
                let clustersFunctionalities = response2.data;

                let amountListTemp = {};

                for (let i = 0; i < entities.length; i++) {
                    let amount = 0;
                    let entityName = entities[i];
                    let entityCluster = clusters.find(c => c.entities.includes(entityName));

                    for (let j = 0; j < clusters.length; j++) {
                        let cluster = clusters[j];
                        let commonFunctionalities = getCommonFunctionalities(entityName, cluster, functionalities, clustersFunctionalities);

                        if (cluster.id !== entityCluster.id && commonFunctionalities.length > 0) {
                            amount += 1;
                        }
                    }
                    amountListTemp[entityName] = amount;
                }
                changeState(state => ({...state,
                    clusters: clusters,
                    entities: entities,
                    functionalities: functionalities,
                    clustersFunctionalities: clustersFunctionalities,
                    amountList: amountListTemp
                }));
            });
        });
    }, []);

    function handleEntitySubmit(value) {
        let entity = value;
        let entityCluster = state.clusters.find(c => c.entities.includes(value));
        let nodes = [];
        let edges = [];

        let entityFunctionalities = [];

        state.functionalities.forEach(functionality => {
            if (functionality.entities[entity])
                entityFunctionalities.push(functionality.name + " " + functionality.entities[entity]);
        })

        nodes.push({
            id: translateEntity(entity),
            label: translateEntity(entity),
            value: 1,
            level: 0,
            type: types.ENTITY,
            title: entityFunctionalities.join('<br>')
        });

        for (let i = 0; i < state.clusters.length; i++) {
            let cluster = state.clusters[i];
            let clusterEntities = cluster.entities;

            if (cluster.id !== entityCluster.id) {
                let commonFunctionalities = getCommonFunctionalities(entity, cluster, state.functionalities, state.clustersFunctionalities);

                if (commonFunctionalities.length > 0) {
                    nodes.push({
                        id: cluster.id,
                        label: cluster.name,
                        value: clusterEntities.length,
                        level: 1,
                        type: types.CLUSTER,
                        title: clusterEntities.map(entityID => translateEntity(entityID)).join('<br>') + "<br>Total: " + clusterEntities.length
                    });

                    edges.push({
                        from: translateEntity(entity),
                        to: cluster.id,
                        label: commonFunctionalities.length.toString(),
                        title: commonFunctionalities.join('<br>')
                    })
                }
            }
        }

        changeState(state => ({...state,
            entity: entity,
            entityCluster: entityCluster,
            visGraph:{
                nodes: new DataSet(nodes),
                edges: new DataSet(edges)
        }}));
    }

    //get functionalities that access both an entity and another cluster
    function getCommonFunctionalities(entityName, cluster, functionalities, clustersFunctionalities) {
        let entityFunctionalities = [];
        let clusterFunctionalities = clustersFunctionalities[cluster.id].map(c => c.name);

        functionalities.forEach(functionality => {
            if (functionality.entities[entityName] && clusterFunctionalities.includes(functionality.name))
                entityFunctionalities.push(functionality.name);
        })

        return entityFunctionalities;
    }

    function handleSelectNode(nodeId) { }

    function handleDeselectNode(nodeId) { }

    function changeSubView(value) {
        changeState(state => ({...state, currentSubView: value }));
    }

    const metricsRows = state.entities.sort((a, b) => a - b).map(entityID => {
        return { entity: translateEntity(entityID) };
    });

    const metricsColumns = [{
        dataField: 'entity',
        text: 'Entity',
        sort: true
    }];

    return (
        <div>
            <ButtonGroup className="mb-2">
                <Button
                    disabled={state.currentSubView === "Graph"}
                    onClick={() => changeSubView("Graph")}
                >
                    Graph
                </Button>
                <Button
                    disabled={state.currentSubView === "Metrics"}
                    onClick={() => changeSubView("Metrics")}
                >
                    Metrics
                </Button>
            </ButtonGroup>

            {state.currentSubView === "Graph" &&
                <span>
                    <EntityOperationsMenu
                        handleEntitySubmit={handleEntitySubmit}
                        entities={state.entities}
                        amountList={state.amountList}
                    />

                    <div style={{height: '700px'}}>
                        <VisNetwork
                            visGraph={state.visGraph}
                            options={options}
                            onSelection={handleSelectNode}
                            onDeselection={handleDeselectNode}
                            view={views.ENTITY} />
                    </div>
                </span>
            }

            {state.currentSubView === "Metrics" &&
                <BootstrapTable
                    bootstrap4
                    keyField='entity'
                    data={metricsRows}
                    columns={metricsColumns}
                />
            }
        </div>
    );
}