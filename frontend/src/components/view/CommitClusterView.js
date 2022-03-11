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

export const commitClusterViewHelp = (<div>
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

export const CommitClusterView = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { codebaseName, dendrogramName, decompositionName } = useParams();

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
        let clusters;
        const service = new RepositoryService();

        const request = service.getCommitClusters(
            codebaseName,
            dendrogramName,
            decompositionName
        ).then(response => {
            clusters = response.data.clusters;
            setClusters(clusters);
            setShowMenu(false);
            setSelectedCluster({});
            setMergeWithCluster({});
            setTransferToCluster({});
            setClusterEntities([]);
            setOperation(operations.NONE);
            let nodes = [];
            Object.entries(clusters).forEach(([key, value]) => {
                nodes.push(convertClusterToNode(key, value))
            });
            const visGraph = {
                nodes: new DataSet(nodes)
            };

            //setClustersControllers(clustersControllers);
            setVisGraph(visGraph);
        });
    }

    function convertClusterToNode(cluster_number, cluster_files) {
        return {
            id: cluster_number,
            title: cluster_files.sort().join('<br>'),
            label: "Cluster " + cluster_number + " [" + cluster_files.length + " files]",
            value: cluster_files.length/100,
            type: types.CLUSTER
        }
        // return {
        //     id: cluster.id,
        //     title: cluster.entities.sort((a, b) => a - b).map(entityID => translateEntity(entityID)).join('<br>') + "<br>Total: " + cluster.entities.length,
        //     label: cluster.name,
        //     value: cluster.entities.length,
        //     type: types.CLUSTER,
        // };
    }


    function handleSelectCluster(nodeId) {

        if (operation === operations.NONE || operation === operations.RENAME) {
            let selectedCluster;
            Object.entries(clusters).forEach(([key, value]) => {if (key == nodeId) selectedCluster = value})
            setSelectedCluster(selectedCluster);
            setShowMenu(true);
        }
    }

    function closeErrorMessageModal() {
        setError(false);
        setErrorMessage('');
    }

    function handleDeselectNode(nodeId) { }

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

                <span>
                    <div style={{height: '700px'}}>
                        <VisNetwork
                            visGraph={visGraph}
                            options={options}
                            onSelection={handleSelectCluster}
                            onDeselection={handleDeselectNode}
                            view={views.COMMIT_CLUSTERS}/>
                    </div>
                </span>
        </>
    );
}