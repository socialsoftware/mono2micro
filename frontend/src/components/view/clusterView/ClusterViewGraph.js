import React, {createRef, useEffect, useState} from "react";
import {DataSet, Network} from "vis-network/standalone";
import {RepositoryService} from "../../../services/RepositoryService";
import {useParams} from "react-router-dom";
import {OPERATION} from "../../../constants/constants";
import {types, views} from "../Views";
import {toast, ToastContainer} from "react-toastify";
import HttpStatus from "http-status-codes";
import {ClusterViewDialogs, DIALOG_TYPE} from "./ClusterViewDialogs";
import {ClusterViewRightClickMenu} from "./ClusterViewRightClickMenu";
import {ClusterViewModal} from "./ClusterViewModal";
import {ClusterViewMetricTable} from "./ClusterViewMetricTable";
import {searchType} from "../ViewSearchBar";
import {KeyboardArrowUp, Search, TableView, Timeline} from "@mui/icons-material";

const options = {
    height: "940",
    edges: {
        smooth: false,
        width: 0.5,
        arrows: {
            from: {
                enabled: false,
                scaleFactor: 0.5
            }
        },
    },
    interaction: {
        hover: true,
        multiselect: true,
    },
    physics: {
        enabled: true,
        barnesHut: {
            springLength: 500,
            springConstant: 0.001
        },
        solver: 'barnesHut'
    },
    layout: {
        improvedLayout: false
    }
};

export const clusterViewHelp = (<div>
    Double click a node to see its properties.<br />
    Double click an edge to see functionalities and dependencies.<br />
    Right click in a node to begin a modification.<br />
    Other operations are also available when<br />
    right-clicking the background or the edges.<br />
    Check cluster metrics by clicking in the<br />
    speed dial (bottom right).<br />
    Search specific entity/cluster/functionality<br />
    in the speed dial.<br />
    Jump directly to functionalities in the<br />
    speed dial.<br />
    To select multiple nodes, leave the mouse
    button pressed or use "Ctrl+click" to add nodes.
    Then right click to see available operations.<br />
</div>);

export const ClusterViewGraph = ({setNow, toastId, translateEntity, clusters, setReloadProperties, searchedItem, setSearchedItem, changeToFunctionalities, setOpenSearch, setActions, view}) => {
    const appRef = createRef();
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [visGraph, setVisGraph] = useState({});
    const [updateGraphVis, setUpdateGraphVis] = useState({});
    const [clustersFunctionalities, setClustersFunctionalities] = useState({});
    const [edgeWeights, setEdgeWeights] = useState({});
    const [network, setNetwork] = useState({});
    const [showModal, setShowModal] = useState(false);
    const [clickedComponent, setClickedComponent] = useState(undefined);
    const [menuCoordinates, setMenuCoordinates] = useState(undefined);
    const [operations, setOperations] = useState([]);
    const [requestDialog, setRequestDialog] = useState(undefined);
    const [dialogResponse, setDialogResponse] = useState(undefined);
    const [graphPositions, setGraphPositions] = useState(undefined);
    const [graphPositionsBeforeNeighbours, setGraphPositionsBeforeNeighbours] = useState(undefined);
    const [pendingSearch, setPendingSearch] = useState(undefined);

    useEffect(() => {
        if (view === views.CLUSTERS)
            setActions(clustersActions);
    }, [view]);

    useEffect(() => {
        const service = new RepositoryService();

        // Clusters Functionalities
        const firstRequest = service.getClustersFunctionalities(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            toast.update(toastId.current, {render: "Loaded Cluster/Functionality Relations"});
            setNow(prev => prev + 10);
            setClustersFunctionalities(response.data)});

        // Entities
        const secondRequest = service.getEdgeWeights(
            codebaseName,
            strategyName,
            decompositionName,
        ).then(response => {
            toast.update(toastId.current, {render: "Loaded Entities"});
            setNow(prev => prev + 15);
            setEdgeWeights(response.data);});

        // Entity and cluster positions
        const thirdRequest = service.getGraphPositions(
            codebaseName,
            strategyName,
            decompositionName,
        ).then(response => {
            toast.update(toastId.current, {render: "Loaded Graph Positions"});
            setNow(prev => prev + 15);
            setGraphPositions(response.data);
        }).catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND)
                toast.update(toastId.current, {render: "No previous positions saved"});
            else toast.update(toastId.current, {type: toast.TYPE.ERROR, render: "Loading positions failed"});
            setGraphPositions(undefined)});

        Promise.all([firstRequest, secondRequest, thirdRequest]).then(() => {
            toast.update(toastId.current, {render: "Creating graph..."});
            setUpdateGraphVis({});
            setActions(clustersActions);
        });
    }, [clusters]);

    useEffect(() => {
        if (dialogResponse === undefined) return;

        const service = new RepositoryService();
        let promise;
        let fromCluster, toCluster;

        // Make sure no conflict is going to happen with previous position data
        deleteGraphPositions().then(() => {
            switch(dialogResponse.type) {
                case DIALOG_TYPE.RENAME:
                    promise = service.renameCluster(
                        codebaseName,
                        strategyName,
                        decompositionName,
                        clickedComponent.id.substring(1),
                        dialogResponse.newName
                    );
                    toast.promise(promise, {
                        pending: "Renaming Cluster...",
                        success: {render: "Successfully changed name!", autoClose: 3000},
                        error: {
                            render({data}) { return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed Renaming."; }
                            , autoClose: 5000}
                    }).then(() => setReloadProperties({}));
                    break;
                case DIALOG_TYPE.TRANSFER:
                    fromCluster = clusters.find(c => c.id === clickedComponent.fromCluster);
                    toCluster = clusters.find(c => c.id === clickedComponent.toCluster);

                    // All entities included equals to the merge of clusters
                    if (!fromCluster.entities.find(entity => !dialogResponse.entities.includes(entity))) {
                        promise = service.mergeClusters(
                            codebaseName,
                            strategyName,
                            decompositionName,
                            fromCluster.id,
                            toCluster.id,
                            toCluster.name
                        );
                        toast.promise(promise, {
                            pending: "Transferring all entities to cluster " + toCluster.name + "...",
                            success: {render: "Successfully transferred all entities!", autoClose: 3000},
                            error: {render: "Failed transferring entities.", autoClose: 5000}
                        }).then(() => setReloadProperties({}));
                    }
                    else {
                        promise = service.transferEntities(
                            codebaseName,
                            strategyName,
                            decompositionName,
                            fromCluster.id,
                            toCluster.id,
                            dialogResponse.entities.toString()
                        );
                        toast.promise(promise, {
                            pending: "Transferring entities from cluster " + fromCluster.name + " to cluster " + toCluster.name + "...",
                            success: {render: "Successfully transferred entities!", autoClose: 3000},
                            error: {render: "Failed transferring entities.", autoClose: 5000}
                        }).then(() => setReloadProperties({}));
                    }
                    break;
                case DIALOG_TYPE.TRANSFER_ENTITY:
                    fromCluster = clusters.find(c => c.entities.includes(clickedComponent.entityId));
                    toCluster = clusters.find(c => c.id === clickedComponent.toCluster);

                    // All entities included equals to the merge of clusters
                    promise = service.transferEntities(
                        codebaseName,
                        strategyName,
                        decompositionName,
                        fromCluster.id,
                        toCluster.id,
                        [clickedComponent.entityId].toString()
                    );
                    toast.promise(promise, {
                        pending: "Transferring entity " + translateEntity(clickedComponent.entityId) + " to cluster " + toCluster.name + "...",
                        success: {render: "Successfully transferred entity " + translateEntity(clickedComponent.entityId) + "!", autoClose: 3000},
                        error: {render: "Failed transferring entity " + translateEntity(clickedComponent.entityId) + ".", autoClose: 5000}
                    }).then(() => setReloadProperties({}));
                    break;
                case DIALOG_TYPE.MERGE:
                    fromCluster = clusters.find(c => c.id === clickedComponent.fromCluster);
                    toCluster = clusters.find(c => c.id === clickedComponent.toCluster);

                    promise = service.mergeClusters(
                        codebaseName,
                        strategyName,
                        decompositionName,
                        fromCluster.id,
                        toCluster.id,
                        dialogResponse.newName
                    );
                    toast.promise(promise, {
                        pending: "Merging clusters to create cluster " + toCluster.newName + "...",
                        success: {render: "Successfully merged clusters!", autoClose: 3000},
                        error: {render: "Failed merging clusters.", autoClose: 5000}
                    }).then(() => setReloadProperties({}));
                    break;
                case DIALOG_TYPE.SPLIT:
                    // All entities included equals merge of clusters
                    fromCluster = clusters.find(c => c.id === clickedComponent.cluster);

                    // Splitting all entities into a new cluster is equivalent to renaming it
                    if (!fromCluster.entities.find(entity => !dialogResponse.entities.includes(entity)))
                        promise = service.renameCluster(
                            codebaseName,
                            strategyName,
                            decompositionName,
                            clickedComponent.id.substring(1),
                            dialogResponse.newName);
                    else promise = service.splitCluster(
                            codebaseName,
                            strategyName,
                            decompositionName,
                            clickedComponent.id.substring(1),
                            dialogResponse.newName,
                            dialogResponse.entities.toString());

                    toast.promise(promise, {
                        pending: "Splitting cluster " + dialogResponse.newName + "...",
                        success: {render: "Successfully split cluster!", autoClose: 3000},
                        error: {
                            render({data}) { return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed splitting."; }
                            , autoClose: 5000}
                    }).then(() => setReloadProperties({}));
                    break;
                case DIALOG_TYPE.FORM_CLUSTER:
                    promise = service.formCluster(
                        codebaseName,
                        strategyName,
                        decompositionName,
                        dialogResponse.newName,
                        dialogResponse.entities.toString());

                    toast.promise(promise, {
                        pending: "Forming new cluster " + dialogResponse.newName + "...",
                        success: {render: "Successfully formed new cluster!", autoClose: 3000},
                        error: {
                            render({data}) { return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed forming new cluster."; }
                            , autoClose: 5000}
                    }).then(() => setReloadProperties({}));
                    break;
            }
        });
        defaultOperations();
        setClickedComponent(undefined);
        setRequestDialog(undefined);
        setDialogResponse(undefined);
    }, [dialogResponse]);

    function handleScrollTop() { document.getElementById("metricTable").scrollIntoView({behavior: 'smooth', block: 'start'}); }

    function handleScrollBottom() { window.scrollTo(0, 0); }

    const clustersActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <TableView/>, name: 'Go to Tables' , handler: handleScrollTop },
        { icon: <KeyboardArrowUp/>, name: 'Go to Top', handler: handleScrollBottom },
        { icon: <Timeline/>, name: 'Go to Functionalities', handler: changeToFunctionalities },
    ];

    useEffect(() => {
        if (searchedItem !== undefined && (searchedItem.type === searchType.ENTITY || searchedItem.type === searchType.CLUSTER)) {
            if (operations.includes(OPERATION.SHOW_ALL))
                handleShowAll("restore");
            else handleSearch();
        }
    }, [searchedItem]);

    // This hook is used so that when searching a sequence of nodes, the graph resets to avoid missing edges/nodes bug
    useEffect(() => {
        if (searchedItem !== undefined && searchedItem.type !== searchType.FUNCTIONALITY)
            handleSearch();
    }, [pendingSearch]);

    function handleSearch() {
        if (searchedItem.type === searchType.ENTITY) {
            if (network.body.nodeIndices.includes("c" + searchedItem.clusterId)) // Open entity's cluster if necessary
                setNetwork(prevNetwork => {prevNetwork.openCluster("c" + searchedItem.clusterId); return prevNetwork;});
            setClickedComponent({id: "e" + searchedItem.id});
        }
        else if (searchedItem.type === searchType.CLUSTER) {
            if (!network.body.nodeIndices.includes("c" + searchedItem.id)) { // Create cluster if necessary
                const cluster = clusters.find(cluster => cluster.id === searchedItem.id);
                setNetwork(prevNetwork => {
                    clusterEntities(cluster, clusterNodeProperties(cluster), prevNetwork);
                    return prevNetwork;
                });
            }
            setClickedComponent({id: "c" + searchedItem.id});
        }
        handleOnlyNeighbours();
        setSearchedItem(undefined);
    }

    function deleteGraphPositions() {
        setGraphPositions(undefined);
        const service = new RepositoryService();
        const promise = service.deleteGraphPositions(codebaseName, strategyName, decompositionName);
        return toast.promise(promise, {
            pending: "Deleting Previous Positions...",
            success: {render: "Successfully Deleted Positions!", autoClose: 2000},
            error: {render: "Error while Deleting Positions", autoClose: 5000}
        });
    }

    useEffect(() => {
        if (clusters.length === 0 || edgeWeights.length === 0 || Object.keys(clustersFunctionalities).length === 0)
            return;
        createGraphVis();
    }, [translateEntity, updateGraphVis]);

    useEffect(() => {
        if (clusters.length === 0 || edgeWeights.length === 0 || Object.keys(clustersFunctionalities).length === 0)
            return;
        updateNetwork();
    }, [visGraph]);

    function defaultOperations() {
        setOperations(() => [
            OPERATION.COLLAPSE,
            OPERATION.TRANSFER,
            OPERATION.TRANSFER_ENTITY,
            OPERATION.MERGE,
            OPERATION.EXPAND,
            OPERATION.EXPAND_ALL,
            OPERATION.SPLIT,
            OPERATION.FORM_CLUSTER,
            OPERATION.RENAME,
            OPERATION.ONLY_NEIGHBOURS,
            OPERATION.TOGGLE_PHYSICS,
            OPERATION.SAVE,
        ]);
    }

    function createGraphVis() {
        let visGraph;

        // Previous positions are recovered
        if (graphPositionsBeforeNeighbours !== undefined) {
            visGraph = { nodes: new DataSet(graphPositionsBeforeNeighbours.entities), edges: new DataSet(graphPositionsBeforeNeighbours.edges) };
        }
        else if (graphPositions !== undefined) {
            visGraph = { nodes: new DataSet(graphPositions.entities), edges: new DataSet(graphPositions.edges) };
            setGraphPositionsBeforeNeighbours(undefined);
        }
        // No previous information saved about positions or positions became outdated (merge, split, etc. happened)
        else {
            const nodes = clusters.flatMap(cluster => cluster.entities.map(entity => {return createEntity(entity, cluster);}))
            const edges = createEntitiesEdges();

            visGraph = { nodes: new DataSet(nodes), edges: new DataSet(edges) };
        }

        setVisGraph(visGraph);
    }

    function updateNetwork() {
        try {
            if (toastId.current === null)
                toastId.current = toast.loading( "Creating graph...");

            let newNetwork = new Network(appRef.current, visGraph, options);
            //newNetwork.storePositions(); // Should not be needed since positions are imported

            // Whenever there is a click outside the contextMenu, it disappears
            newNetwork.on("click", function () { setMenuCoordinates(undefined); });

            // Whenever a click in a node is done
            newNetwork.on("selectNode", (event) => handleSelectNode(event));

            // Whenever a right click is made
            newNetwork.on("oncontext", (event) => handleRightClick(event));

            // Whenever a double click is made
            newNetwork.on("doubleClick", (event) => handleDoubleClick(event));

            // Updates graph loading progress
            newNetwork.on("stabilizationProgress", function (params) {
                setNow(40 + (params.iterations * 60) / params.total);
            });

            //Remove progress bar once graph is loaded
            newNetwork.once("stabilizationIterationsDone", function () { setNow(0); });

            // Create clusters and cluster edges
            const positions = graphPositionsBeforeNeighbours !== undefined? graphPositionsBeforeNeighbours : graphPositions;
            if (positions === undefined) {
                //NOTE: In case it is preferable to start with clusters instead of entities, uncomment the next two lines of code
                // keep in mind that creating clusters increases loading times
                //clusters.forEach(cluster => clusterEntities(cluster, clusterNodeProperties(cluster), newNetwork));
                //newNetwork.body.edgeIndices.forEach(edgeId => updateClusterEdge(newNetwork.body.edges[edgeId], newNetwork));
            }
            else {
                try {
                    Object.entries(positions.clusters).forEach(([id, nodeProperties]) => {
                        const cluster = clusters.find(c => "c" + c.id === id);
                        clusterEntities(cluster, nodeProperties, newNetwork);
                        newNetwork.body.edgeIndices.forEach(edgeId => {
                            if (edgeId.startsWith("cluster")) {
                                const edge = newNetwork.body.edges[edgeId];
                                if (newNetwork.isCluster(edge.fromId) && newNetwork.isCluster(edge.toId)) // Between clusters
                                    updateClusterEdge(edge, newNetwork);
                                else if (!newNetwork.isCluster(edge.fromId) || !newNetwork.isCluster(edge.toId)) // Between cluster and entity
                                    updateClusterEntityEdge(edge, newNetwork);
                            }
                        });
                    });
                } catch (e) {
                    console.log("Saved position file is defective, backup to default graph creation...");
                    deleteGraphPositions().then(() =>  setUpdateGraphVis({}));
                }
            }

            if (visGraph.nodes.length > 100)
                newNetwork.setOptions({interaction : {hideEdgesOnDrag: true}});

            setNetwork(newNetwork);
            setGraphPositionsBeforeNeighbours(undefined);
            setPendingSearch({});
            toast.update(toastId.current, {type: toast.TYPE.SUCCESS, render: "Completed Graph Creation", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId.current); toastId.current = null;}, 1000);
            defaultOperations();
        } catch (e) {
            toast.update(toastId.current, {type: toast.TYPE.ERROR, render: "Failed Graph Creation", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId.current); toastId.current = null;}, 5000);
            console.error(e);
        }
    }

    function handleSelectNode(event) {
        let network;
        setNetwork(prev => {network = prev; return prev});

        // Unfortunately, this needs to be done in order to read the clickedComponent value, otherwise it will say it is undefined
        setClickedComponent(prev => {
            if (prev !== undefined && prev.operation === OPERATION.TRANSFER && network.isCluster(event.nodes[0])) {
                const fromCluster = clusters.find(cluster => cluster.id == prev.id.substring(1));
                const toCluster = clusters.find(cluster => cluster.id == event.nodes[0].substring(1));
                toast.dismiss();
                if (fromCluster === toCluster) {
                    toast.warning("You must click in a different cluster.", {autoClose: 5000});
                    return prev;
                }
                const entities = fromCluster.entities.map(entity => {return {id: entity, name: translateEntity(entity)}});
                setRequestDialog({type: DIALOG_TYPE.TRANSFER, fromCluster: fromCluster.name, toCluster: toCluster.name, entities});
                return {...prev, fromCluster: fromCluster.id, toCluster: toCluster.id};
            }
            else if (prev !== undefined && prev.operation === OPERATION.TRANSFER_ENTITY && network.isCluster(event.nodes[0])) {
                const entityId = Number(prev.id.substring(1));
                const toCluster = clusters.find(cluster => cluster.id == event.nodes[0].substring(1));
                toast.dismiss();

                setRequestDialog({type: DIALOG_TYPE.TRANSFER_ENTITY, entityName: translateEntity(entityId), toCluster: toCluster.name});
                return {...prev, entityId, toCluster: toCluster.id};
            }
            else if (prev !== undefined && prev.operation === OPERATION.MERGE && network.isCluster(event.nodes[0])) {
                const fromCluster = clusters.find(cluster => cluster.id == prev.id.substring(1));
                const toCluster = clusters.find(cluster => cluster.id == event.nodes[0].substring(1));
                toast.dismiss();
                if (fromCluster === toCluster) {
                    toast.warning("You must click in a different cluster.", {autoClose: 5000});
                    return prev;
                }
                setRequestDialog({type: DIALOG_TYPE.MERGE, fromCluster: fromCluster.name, toCluster: toCluster.name});
                return {...prev, fromCluster: fromCluster.id, toCluster: toCluster.id};
            }
            else return prev; // Clicking in a node without a previous operation selected does not start any behaviour
        });
    }

    function handleRightClick(event) {
        event.event.preventDefault(); // Avoids popping up the default right click window
        let network;
        // Unfortunately, this needs to be done in order to read the updated network, otherwise it will fail
        // This might be a bug in react involving the states and events
        setNetwork(prev => {network = prev; return prev});

        let operations;
        setOperations(prev => {operations = prev; return prev});
        if (operations.includes(OPERATION.CANCEL)) {
            setMenuCoordinates({left: event.event.pageX, top: event.event.pageY});
            return;
        }

        // Multiple selection for nodes
        let selectedNodes = network.getSelectedNodes();
        if (selectedNodes.length > 1) {
            const rightClickNode = network.getNodeAt(event.pointer.DOM);
            if (rightClickNode !== undefined && !selectedNodes.includes(rightClickNode))
                selectedNodes.push(rightClickNode);
            let entities = [];
            selectedNodes.forEach(nodeId => {
                if (network.isCluster(nodeId)) {
                    let cluster = clusters.find(c => c.id == nodeId.substring(1));
                    cluster.entities.forEach(entity => entities.push(entity));
                } else entities.push(Number(nodeId.substring(1)));
            });
            setClickedComponent({entities});

            setMenuCoordinates({left: event.event.pageX, top: event.event.pageY, type: types.MULTIPLE});
            return;
        }

        // Single selection for nodes and edges
        if (network.getNodeAt(event.pointer.DOM) !== undefined) {
            let nodeId = network.getNodeAt(event.pointer.DOM);
            setClickedComponent({id: nodeId});

            if (network.isCluster(nodeId)) {
                setMenuCoordinates({left: event.event.pageX, top: event.event.pageY, type: types.CLUSTER});
                let cluster = clusters.find(c => c.id == nodeId.substring(1));
                let operations;
                setOperations(prev => {operations = prev; return prev});
                if (cluster.entities.length === 1)
                    setOperations(prev => prev.filter(op => op !== OPERATION.TRANSFER));
                else if (!operations.includes(OPERATION.SHOW_ALL))
                    defaultOperations();
            }
            else setMenuCoordinates({left: event.event.pageX, top: event.event.pageY, type: types.ENTITY});
        }
        else { // For now, the operations available by clicking an edge are the same as clicking in the background
            let graphPositions;
            setGraphPositions(prev => {graphPositions = prev; return prev});
            setOperations(prev => {
                if (graphPositions === undefined)
                    prev = prev.filter(op => op !== OPERATION.RESTORE);
                else prev.push(OPERATION.RESTORE);

                if (network.body.nodeIndices.find(nodeId => network.isCluster(nodeId)))
                    prev.push(OPERATION.EXPAND_ALL);
                else prev = prev.filter(op => op !== OPERATION.EXPAND_ALL);

                return prev;
            });
            if (network.getEdgeAt(event.pointer.DOM) !== undefined) {
                setClickedComponent({id: network.getEdgeAt(event.pointer.DOM)});
                setMenuCoordinates({left: event.event.pageX, top: event.event.pageY, type: types.EDGE});
            }
            else {
                setClickedComponent(undefined);
                setMenuCoordinates({left: event.event.pageX, top: event.event.pageY, type: types.NONE});
            }
        }
    }

    function handleDoubleClick(event) {
        let network;
        setNetwork(prev => {network = prev; return prev});

        if (event.nodes.length > 0) // Clicked cluster or entity
            setClickedComponent({id: event.nodes[0], eventType: network.isCluster(event.nodes[0])? "doubleClickCluster" : "doubleClickEntity"});
        else if (event.edges.length > 0)
            setClickedComponent({id: event.edges[0], eventType: "doubleClickEdge"}); // Clicked edge
        else return;
        setShowModal(true);
    }



    function handleExpandCluster() {
        setNetwork(prevNetwork => {
            const entities = prevNetwork.clustering.getNodesInCluster(clickedComponent.id);
            prevNetwork.openCluster(clickedComponent.id);

            entities.forEach(entity => {
                prevNetwork.getConnectedEdges(entity).forEach(edgeId => {
                    if (!edgeId.startsWith("clusterEdge"))
                        return;
                    const edge = prevNetwork.body.edges[edgeId];
                    if (prevNetwork.isCluster(edge.fromId) && !prevNetwork.isCluster(edge.toId) ||
                        !prevNetwork.isCluster(edge.fromId) && prevNetwork.isCluster(edge.toId)) { // Between cluster and entity
                        updateClusterEntityEdge(edge, prevNetwork);
                    }
                });
            });

            return prevNetwork;
        })
        setMenuCoordinates(undefined);
    }

    function handleExpandAll() {
        setNetwork(prevNetwork => {
            prevNetwork.body.nodeIndices.forEach(nodeId => {
                if (prevNetwork.isCluster(nodeId))
                    prevNetwork.openCluster(nodeId);
            });
            return prevNetwork;
        });
        handleCancel();
    }

    function handleRename() {
        setMenuCoordinates(undefined);
        const cluster = clusters.find(cluster => cluster.id == clickedComponent.id.substring(1));
        setRequestDialog({type: DIALOG_TYPE.RENAME, clusterName: cluster.name});
    }

    function handleCollapseCluster() {
        const clusterId = visGraph.nodes.get(clickedComponent.id).cid;
        const cluster = clusters.find(cluster => cluster.id === clusterId);
        setNetwork(prevNetwork => {
            clusterEntities(cluster, clusterNodeProperties(cluster), prevNetwork);
            prevNetwork.getConnectedEdges("c" + clusterId).forEach(edgeId => {
                const edge = prevNetwork.body.edges[edgeId];
                if (edge.fromId === "c" + clusterId && prevNetwork.isCluster(edge.toId) || prevNetwork.isCluster(edge.fromId) && edge.toId === "c" + clusterId) // Between clusters
                    updateClusterEdge(edge, prevNetwork);
                else if (edge.fromId === "c" + clusterId && !prevNetwork.isCluster(edge.toId) || !prevNetwork.isCluster(edge.fromId) && edge.toId === "c" + clusterId) // Between cluster and entity
                    updateClusterEntityEdge(edge, prevNetwork);
            });
            return prevNetwork;
        })
        setClickedComponent(undefined);
        setMenuCoordinates(undefined);
    }

    function handleOnlyNeighbours() {
        toastId.current = toast.loading("This operation might take a while...");
        setTimeout(() => {
            setNetwork(prev => {setGraphPositionsBeforeNeighbours(buildGraphPositions()); return prev;});

            let nodeId;
            setClickedComponent(prev => {nodeId = prev.id; return prev;});

            let hiddenNodes = [], hiddenEdges = [];

            for (const edge of Object.values(network.body.edgeIndices))
                if (network.body.edges[edge].fromId !== nodeId && network.body.edges[edge].toId !== nodeId)
                    hiddenEdges.push({id: edge, hidden: true});

            visGraph.edges.update(hiddenEdges);

            const connectedNodes = network.getConnectedNodes(nodeId);
            for (const node of network.body.nodeIndices)
                if (!connectedNodes.includes(node) && node !== nodeId)
                    hiddenNodes.push({id: node, hidden: true});

            visGraph.nodes.update(hiddenNodes);

            setOperations([OPERATION.SHOW_ALL, OPERATION.EXPAND, OPERATION.TOGGLE_PHYSICS]);
            setMenuCoordinates(undefined);
            toast.update(toastId.current, {type: toast.TYPE.SUCCESS, render: "View Created", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId.current); toastId.current = null;}, 1000);
        }, 100);
    }

    function handleShowAll(type) {
        if (type === "restore")
            setGraphPositionsBeforeNeighbours(undefined);
        toastId.current = toast.loading("This operation might take a while...");
        setTimeout(() => {
            createGraphVis();

            defaultOperations();
            setMenuCoordinates(undefined);
            toast.update(toastId.current, {type: toast.TYPE.SUCCESS, render: "View Restored", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId.current); toastId.current = null;}, 1000);
        }, 100);
    }

    function handleTogglePhysics() {
        setNetwork(prevNetwork => {
            prevNetwork.setOptions( {physics: !prevNetwork.physics.physicsEnabled} );
            return prevNetwork;
        })
        setMenuCoordinates(undefined);
    }

    function handleTransfer() {
        toast.info("Click in the cluster where the entities will be transferred to.\nRight click to cancel.", {autoClose: false});

        setClickedComponent(prev => {
            return {...prev, operation: OPERATION.TRANSFER};
        });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleTransferEntity() {
        toast.info("Click in the cluster where the selected entity will be transferred to.\nRight click to cancel.", {autoClose: false});

        setClickedComponent(prev => {
            return {...prev, operation: OPERATION.TRANSFER_ENTITY};
        });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleMerge() {
        toast.info("Click in the cluster to be merged to.\nRight click to cancel.", {autoClose: false});

        setClickedComponent(prev => {
            return {...prev, operation: OPERATION.MERGE};
        });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleSplit() {
        const cluster = clusters.find(cluster => cluster.id == clickedComponent.id.substring(1));
        const entities = cluster.entities.map(entity => {
            return {id: entity, name: translateEntity(entity)}
        });
        setRequestDialog({
            type: DIALOG_TYPE.SPLIT,
            cluster: cluster.name,
            entities
        });
        setClickedComponent(prev => {return {...prev, cluster: cluster.id}});
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleFormCluster() {
        if (clickedComponent.entities !== undefined)
            setRequestDialog({
                type: DIALOG_TYPE.FORM_CLUSTER,
                entities: clickedComponent.entities.map(entity => {
                    const cluster = clusters.find(cluster => cluster.entities.includes(entity));
                    return {id: entity, name: translateEntity(entity), cluster: cluster.name};
                })
            });
        else {
            const entity = Number(clickedComponent.id.substring(1));
            const cluster = clusters.find(cluster => cluster.entities.includes(entity));
            setRequestDialog({
                type: DIALOG_TYPE.FORM_CLUSTER,
                entities: [{id: entity, name: translateEntity(clickedComponent.id), cluster: cluster.name}]
            });
        }
        setMenuCoordinates(undefined);
    }

    function buildGraphPositions() {
        let graphPositions = {entities: [], clusters: {}, edges: []};

        // Add entities
        Object.values(visGraph.nodes.get()).forEach(entity => {
            graphPositions.entities.push({id: entity.id, cid: entity.cid, group: entity.group, value: entity.value, label: entity.label, title: entity.title});
        });

        // Updates positions of entities and adds positions of clusters
        Object.entries(network.getPositions()).forEach(([id, position]) => {
            if (network.isCluster(id)) {
                const cluster = clusterNodeProperties(clusters.find(cluster => "c" + cluster.id === id));
                graphPositions.clusters[id] = {...cluster, x: position.x, y: position.y};
            }
            else {
                const entity = graphPositions.entities.find(entity => entity.id === id);
                entity.x = position.x;
                entity.y = position.y;
            }
        });

        // Add edges
        Object.values(visGraph.edges.get()).forEach(edge => {
            graphPositions.edges.push({from: edge.from, to: edge.to, length: edge.length, value: edge.value, title: edge.title});
        });

        return graphPositions;
    }

    function handleSave() {
        const graphPositions = buildGraphPositions();

        const service = new RepositoryService();

        const promise = service.saveGraphPositions(codebaseName, strategyName, decompositionName, graphPositions);
        toast.promise(promise, {
            pending: "Saving Graph...",
            success: {render: "Successfully Saved Graph Positions!", autoClose: 2000},
            error: {render: "Error while Saving Graph Positions", autoClose: 5000}
        }).then(() => setGraphPositions(graphPositions));

        setMenuCoordinates(undefined);
    }

    function handleCancel() {
        toast.dismiss();
        setRequestDialog(undefined);
        setMenuCoordinates(undefined);
        setClickedComponent(undefined);
        defaultOperations();
    }

    // Only for edges between clusters
    function updateClusterEdge(edge, network) {
        const c1 = clusters.find(cluster => "c" + cluster.id === edge.fromId);
        const c2 = clusters.find(cluster => "c" + cluster.id === edge.toId);
        let cluster1Functionalities = clustersFunctionalities[c1.id].map(c => c.name);
        let cluster2Functionalities = clustersFunctionalities[c2.id].map(c => c.name);
        let functionalitiesInCommon = cluster1Functionalities.filter(functionalityName => cluster2Functionalities.includes(functionalityName))

        let couplingC1C2 = c1.couplingDependencies[c2.id] === undefined ? [] : c1.couplingDependencies[c2.id];
        let couplingC2C1 = c2.couplingDependencies[c1.id] === undefined ? [] : c2.couplingDependencies[c1.id];

        let title = couplingC1C2.length > 0? "Coupling dependencies from " + c1.name + " -> " + c2.name + ":\n" + couplingC1C2.map(entity => translateEntity(entity)).join('\n') + "\n\n" :
            "No coupling dependencies from " + c1.name + " -> " + c2.name + ".\n\n";
            title += couplingC2C1.length > 0? "Coupling dependencies from " + c2.name + " -> " + c1.name + ":\n" + couplingC2C1.map(entity => translateEntity(entity)).join('\n') + "\n\n" :
            "No coupling dependencies from " + c2.name + " -> " + c1.name + ".\n\n";
            title += "Functionalities in common:\n" + functionalitiesInCommon.length;

        const length = 1 / ((couplingC1C2.length + couplingC2C1.length) || 1) * 800;

        network.clustering.updateEdge(edge.id, {title, length, value: functionalitiesInCommon.length});
    }

    function updateClusterEntityEdge(edge, network) {
        let cluster;
        let e1ID;
        if (network.isCluster(edge.fromId)) {
            cluster = clusters.find(c => "c" + c.id === edge.fromId);
            e1ID = Number(edge.toId.substring(1));
        } else {
            cluster = clusters.find(c => "c" + c.id === edge.toId);
            e1ID = Number(edge.fromId.substring(1));
        }

        let title = "", copheneticDistanceSum = 0, counter = 0;

        edgeWeights.forEach(weight => {
            if (weight.e1ID === e1ID) {
                if (cluster.entities.includes(weight.e2ID) !== undefined) {
                    copheneticDistanceSum += weight.dist;
                    counter++;
                }
            }
            else if (weight.e2ID === e1ID) {
                if (cluster.entities.includes(weight.e1ID) !== undefined) {
                    copheneticDistanceSum += weight.dist;
                    counter++;
                }
            }
        });

        if (copheneticDistanceSum !== 0 && counter !== 0)
            title += "Cophenetic median:\n" + copheneticDistanceSum / counter + "\n\n";

        title += "Functionalities in common:\n";
        const commonFunctionalities = clustersFunctionalities[cluster.id].reduce((prev, current) => {
            if (current.entities[e1ID] !== undefined)
                return prev + 1;
            else return prev;
        }, 0);

        title += commonFunctionalities;

        network.body.edges[edge.id].title = title;
        //The function below should be used, but since it is slow, the hack above is used
        //network.clustering.updateEdge(edge.id, {title, value: commonFunctionalities});
    }

    function clusterEntities(cluster, nodeProperties, network) {
        const clusterOptionsByData = {
            joinCondition: function (childOptions) {
                return childOptions.cid === cluster.id;
            },
            //processProperties: function (clusterOptions, childNodes, childEdges) {
            //    clusterOptions.mass = childNodes.length;
            //    return clusterOptions;
            //},
            clusterNodeProperties: nodeProperties,
        };
        network.cluster(clusterOptionsByData);
    }

    const clusterNodeProperties = (cluster) => {
        return {
            id: "c" + cluster.id,
            group: cluster.id,
            title: "Total of " + cluster.entities.length + " entities:\n" + cluster.entities.map(entity => translateEntity(entity)).join('\n'),
            label: cluster.name,
            value: cluster.entities.length,
            borderWidth: 3,
            shape: "box",
            allowSingleNodeCluster: true,
            scaling: {
                label: {
                    enabled: true,
                    min: 20,
                    max: 80
                }
            }
        };
    }

    function createEntity(entity, cluster) {
        return {
            id: "e" + entity,
            group: cluster.id,
            cid: cluster.id,
            title: "Belongs to cluster: " + cluster.name,
            label: translateEntity(entity),
            value: 1
        };
    }

    function createEntitiesEdges() {
        const edges = [];

        edgeWeights.forEach(edge => {
            edges.push({
                from: "e" + edge.e1ID,
                to: "e" + edge.e2ID,
                length: edge.dist * 400,
                value: edge.functionalities.length,
                title: "Cophenetic distance:\n" + edge.dist + "\n\nFunctionalities in common:\n" + edge.functionalities.length
            });
        });
        return edges;
    }

    return (
        <>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            <ClusterViewModal
                clusters={clusters}
                edgeWeights={edgeWeights}
                clustersFunctionalities={clustersFunctionalities}
                visGraph={visGraph}
                network={network}
                showModal={showModal}
                setShowModal={setShowModal}
                clickedComponent={clickedComponent}
                setClickedComponent={setClickedComponent}
            />

            <ClusterViewRightClickMenu
                menuCoordinates={menuCoordinates}
                operations={operations}
                handleExpandCluster={handleExpandCluster}
                handleExpandAll={handleExpandAll}
                handleRename={handleRename}
                handleOnlyNeighbours={handleOnlyNeighbours}
                handleCollapseCluster={handleCollapseCluster}
                handleShowAll={handleShowAll}
                handleTogglePhysics={handleTogglePhysics}
                handleTransfer={handleTransfer}
                handleTransferEntity={handleTransferEntity}
                handleMerge={handleMerge}
                handleSplit={handleSplit}
                handleFormCluster={handleFormCluster}
                handleSave={handleSave}
                handleCancel={handleCancel}
            />

            {requestDialog !== undefined &&
                <ClusterViewDialogs
                    requestDialog={requestDialog}
                    setRequestDialog={setRequestDialog}
                    setDialogResponse={setDialogResponse}
                    setClickedComponent={setClickedComponent}
                    handleCancel={handleCancel}
                />
            }

            <div ref={appRef}/>

            <div id="metricTable"></div> {/*This is used as an anchor*/}
            <ClusterViewMetricTable
                clusters={clusters}
                clustersFunctionalities={clustersFunctionalities}
            />
        </>
    );
}