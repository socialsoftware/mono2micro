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
    nodes: {
        shape: 'square',
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
        console.log("Setting value to: " + cluster_files['entityFiles'].length + cluster_files['otherFiles'].length)
        return {
            id: cluster_number,
            title:  `${cluster_files['entityFiles'].length} entity files, ${cluster_files['otherFiles'].length} others`,
            label: cluster_number,
            type: types.CLUSTER,
            color: cluster_files['entityFiles'].length > 0 ? "#36cc0c" : "#D2E5FF",
            value: cluster_files['entityFiles'].length,
            entities: cluster_files['entityFiles'],
            others: cluster_files['otherFiles'],
            shape: 'square'
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