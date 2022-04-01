import React, {useContext, useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ClusterOperationsMenu, operations } from './ClusterOperationsMenu';
import { VisNetwork } from '../util/VisNetwork';
import { ModalMessage } from '../util/ModalMessage';
import { DataSet } from "vis";
import { views, types } from './Views';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import AppContext from "./../AppContext";
import {useParams} from "react-router-dom";

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

export const ClusterView = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [visGraph, setVisGraph] = useState({});
    const [clusters, setClusters] = useState([]);
    const [clustersControllers, setClustersControllers] = useState({});
    const [showMenu, setShowMenu] = useState(false);
    const [selectedCluster, setSelectedCluster] = useState({});
    const [mergeWithCluster, setMergeWithCluster] = useState({});
    const [transferToCluster, setTransferToCluster] = useState({});
    const [clusterEntities, setClusterEntities] = useState([]);
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [operation, setOperation] = useState(operations.NONE);
    const [currentSubView, setCurrentSubView] = useState('Graph');

    //Executed on mount
    useEffect(() => loadDecomposition(), []);

    function loadDecomposition() {
        let clusters, clustersControllers;
        const service = new RepositoryService();

        const firstRequest = service.getClustersControllers(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            clustersControllers = response.data;
        });

        const secondRequest = service.getDecomposition(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            clusters = Object.values(response.data.clusters)
            clusters = clusters.sort((a, b) => a.name - b.name);
            setClusters(clusters);
            setShowMenu(false);
            setSelectedCluster({});
            setMergeWithCluster({});
            setTransferToCluster({});
            setClusterEntities([]);
            setOperation(operations.NONE);
        });

        Promise.all([firstRequest, secondRequest]).then(() => {
            const visGraph = {
                nodes: new DataSet(clusters.map(cluster => convertClusterToNode(cluster))),
                edges: new DataSet(createEdges(clusters, clustersControllers))
            };

            setClustersControllers(clustersControllers);
            setVisGraph(visGraph);
        });
    }

    function convertClusterToNode(cluster) {

        return {
            id: cluster.id,
            title: cluster.entities.sort((a, b) => a - b).map(entityID => translateEntity(entityID)).join('<br>') + "<br>Total: " + cluster.entities.length,
            label: cluster.name,
            value: cluster.entities.length,
            type: types.CLUSTER,
        };
    }

    function createEdges(clusters, clustersControllers) {
        let edges = [];
        let edgeLengthFactor = 1000;

        for (let i = 0; i < clusters.length; i++) {
            let cluster1 = clusters[i];
            let cluster1Controllers = clustersControllers[cluster1.id].map(c => c.name);

            for (let j = i + 1; j < clusters.length; j++) {
                let cluster2 = clusters[j];
                let cluster2Controllers = clustersControllers[cluster2.id].map(c => c.name);

                let controllersInCommon = cluster1Controllers.filter(controllerName => cluster2Controllers.includes(controllerName))

                let couplingC1C2 = cluster1.couplingDependencies[cluster2.id] === undefined ? 0 : cluster1.couplingDependencies[cluster2.id].length;
                let couplingC2C1 = cluster2.couplingDependencies[cluster1.id] === undefined ? 0 : cluster2.couplingDependencies[cluster1.id].length;

                let edgeTitle = cluster1.name + " -> " + cluster2.name + " , Coupling: " + couplingC1C2 + "<br>";
                edgeTitle += cluster2.name + " -> " + cluster1.name + " , Coupling: " + couplingC2C1 + "<br>";
                edgeTitle += "Controllers in common:<br>"

                let edgeLength = (1 / controllersInCommon.length) * edgeLengthFactor;
                if (edgeLength < 100) edgeLength = 300;
                else if (edgeLength > 500) edgeLength = 500;

                controllersInCommon.sort()
                if (controllersInCommon.length > 0)
                    edges.push({
                        from: cluster1.id,
                        to: cluster2.id,
                        length: edgeLength,
                        value: controllersInCommon.length,
                        label: controllersInCommon.length.toString(),
                        title: edgeTitle + controllersInCommon.join('<br>')
                    });
            }
        }
        return edges;
    }

    function selectClusterEntities(selectedCluster) {
        setSelectedCluster(selectedCluster);
        setMergeWithCluster({});
        setClusterEntities(selectedCluster.entities.sort((a, b) => (a > b) ? 1 : -1)
            .map(e => ({
                name: e,
                value: e,
                label: e,
                active: false
            })));
    }

    function handleSelectOperation(operation) {
        if (operation === operations.SPLIT || operation === operations.TRANSFER) {
            selectClusterEntities(selectedCluster);
            setOperation(operation);
        } else {
            setMergeWithCluster({});
            setTransferToCluster({});
            setClusterEntities([]);
            setOperation(operation);
        }
    }

    function handleSelectCluster(nodeId) {

        if (operation === operations.NONE || operation === operations.RENAME) {
            setSelectedCluster(clusters.find(c => c.id === nodeId));
            setShowMenu(true);
        }
        else if (operation === operations.MERGE) {
            const mergeWithCluster = clusters.find(c => c.id === nodeId);
            if (selectedCluster === mergeWithCluster) {
                setError(true);
                setErrorMessage('Cannot merge a cluster with itself');
            } else {
                setMergeWithCluster(mergeWithCluster);
            }
        }
        else if (operation === operations.TRANSFER) {
            const transferToCluster = clusters.find(c => c.id === nodeId);
            if (selectedCluster === transferToCluster) {
                setError(true);
                setErrorMessage('Cannot transfer entities to the same cluster');
            } else {
                setTransferToCluster(transferToCluster);
            }
        }
        else if (operation === operations.SPLIT) {
            selectClusterEntities(clusters.find(c => c.id === nodeId));
        }
    }

    function handleSelectEntities(entities) {
        if (entities === null) {
            setClusterEntities(clusterEntities.map( e => { return { ...e, active: false }} ));
        } else {
            setClusterEntities(clusterEntities.map(e => {
                if (entities.map(e => e.name).includes(e.name)) {
                    return { ...e, active: true };
                } else {
                    return { ...e, active: false };
                }
            }));
        }
    }

    function handleOperationSubmit(operation, inputValue) {
        const service = new RepositoryService();

        switch (operation) {
            case operations.RENAME:
                service.renameCluster(
                    codebaseName,
                    strategyName,
                    decompositionName,
                    selectedCluster.id,
                    inputValue
                ).then(() => {
                    loadDecomposition();
                }).catch(() => {
                    setError(true);
                    setErrorMessage('ERROR: rename cluster failed.');
                });

                break;

            case operations.MERGE:
                service.mergeClusters(
                    codebaseName,
                    strategyName,
                    decompositionName,
                    selectedCluster.id,
                    mergeWithCluster.id,
                    inputValue
                ).then(() => {
                    loadDecomposition();
                }).catch(() => {
                    setError(true);
                    setErrorMessage('ERROR: merge clusters failed.');
                });

                break;

            case operations.SPLIT:
                let activeClusterEntitiesSplit = clusterEntities.filter(e => e.active).map(e => e.name).toString();
                
                service.splitCluster(
                    codebaseName,
                    strategyName,
                    decompositionName,
                    selectedCluster.id,
                    inputValue,
                    activeClusterEntitiesSplit
                ).then(() => {
                    loadDecomposition();
                }).catch(() => {
                    setError(true);
                    setErrorMessage('ERROR: split cluster failed.');
                });

                break;

            case operations.TRANSFER:
                let activeClusterEntitiesTransfer = clusterEntities.filter(e => e.active).map(e => e.name).toString();

                service.transferEntities(
                    codebaseName,
                    strategyName,
                    decompositionName,
                    selectedCluster.id,
                    transferToCluster.id,
                    activeClusterEntitiesTransfer
                ).then(() => {
                    loadDecomposition();
                }).catch(() => {
                    setError(true);
                    setErrorMessage('ERROR: transfer entities failed.');
                });
                break;

            default:
        }
    }

    function handleOperationCancel() {
        setShowMenu(false);
        setSelectedCluster({});
        setMergeWithCluster({});
        setTransferToCluster({});
        setClusterEntities([]);
        setOperation(operations.NONE);
    }

    function closeErrorMessageModal() {
        setError(false);
        setErrorMessage('');
    }

    function handleDeselectNode(nodeId) { }

    function changeSubView(value) {
        setCurrentSubView(value);
    }

    const metricsRows = clusters.map(({ name, id, entities, cohesion, coupling, complexity }) => {
        return {
            cluster: name,
            entities: entities.length,
            controllers: clustersControllers[id] === undefined ? 0 : clustersControllers[id].length,
            cohesion: cohesion,
            coupling: coupling,
            complexity: complexity
        }
    });

    const metricsColumns = [{
        dataField: 'cluster',
        text: 'Cluster',
        sort: true
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

    const couplingRows = clusters.map(c1 => {
        return Object.assign({id: c1.name}, ...clusters.map(c2 => {
            return {
                [c2.name]: c1.id === c2.id ? "---" :
                    c1.couplingDependencies[c2.id] === undefined ? 0 :
                        parseFloat(c1.couplingDependencies[c2.id].length / Object.keys(c2.entities).length).toFixed(2)
            }
        }))
    });

    const couplingColumns = [{ dataField: 'id', text: '', style: { fontWeight: 'bold' } }]
    .concat(clusters.map(c => {
        return {
            dataField: c.name,
            text: c.name
        }
    }));

    return (
        <>
            {
                error &&
                <ModalMessage
                    title='Error Message'
                    message={errorMessage}
                    onClose={closeErrorMessageModal}
                />
            }
            <ButtonGroup className="mb-2">
                <Button
                    disabled={currentSubView === "Graph"}
                    onClick={() => changeSubView("Graph")}
                >
                    Graph
                </Button>
                <Button
                    disabled={currentSubView === "Metrics"}
                    onClick={() => changeSubView("Metrics")}
                >
                    Metrics
                </Button>
                <Button
                    disabled={currentSubView === "Coupling Matrix"}
                    onClick={() => changeSubView("Coupling Matrix")}
                >
                    Coupling Matrix
                </Button>
            </ButtonGroup>

            {currentSubView === "Graph" &&
                <span>
                    {showMenu &&
                    <ClusterOperationsMenu
                        selectedCluster={selectedCluster}
                        mergeWithCluster={mergeWithCluster}
                        transferToCluster={transferToCluster}
                        clusterEntities={clusterEntities}
                        handleSelectOperation={handleSelectOperation}
                        handleSelectEntities={handleSelectEntities}
                        handleSubmit={handleOperationSubmit}
                        handleCancel={handleOperationCancel}
                    />}

                    <div style={{height: '700px'}}>
                        <VisNetwork
                            visGraph={visGraph}
                            options={options}
                            onSelection={handleSelectCluster}
                            onDeselection={handleDeselectNode}
                            view={views.CLUSTERS}/>
                    </div>
                </span>
            }


            {
                currentSubView === "Metrics" &&
                <BootstrapTable
                    bootstrap4
                    keyField='cluster'
                    data={metricsRows}
                    columns={metricsColumns}
                />
            }

            {
                currentSubView === "Coupling Matrix" &&
                <BootstrapTable
                    bootstrap4
                    keyField='id'
                    data={couplingRows}
                    columns={couplingColumns}
                />
            }
        </>
    );
}