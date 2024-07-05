import React, {useEffect, useState} from "react";
import {APIService} from "../../../services/APIService";
import {toast, ToastContainer} from "react-toastify";
import HttpStatus from "http-status-codes";
import {DataSet, Network} from "vis-network/standalone";
import {OPERATION} from "../../../constants/constants";
import {searchType} from "./ViewSearchBar";
import {
    collapseAll,
    collapseCluster,
    createCluster,
    expandAll,
    expandCluster,
    generateAllEdges, generateNewAndAffectedEdges, getRelatedNodesAndEdges,
    networkOptions,
    types
} from "./GraphUtils";
import {useParams} from "react-router-dom";
import {RightClickMenu} from "./RightClickMenu";
import {ClusterViewDialogs, DIALOG_TYPE} from "./ClusterViewDialogs";
import Container from "react-bootstrap/Container";

export const OperationTypes = {
    RENAME_OPERATION: "RenameOperation",
    MERGE_OPERATION: "MergeOperation",
    FORM_CLUSTER_OPERATION: "FormClusterOperation",
    SPLIT_OPERATION: "SplitOperation",
    TRANSFER_OPERATION: "TransferOperation",
};

export const ClusterView = (
    {
        property,
        reloadPositions,
        setReloadPositions,
        clickedComponent,
        setClickedComponent,
        clusters,
        setClusters,
        edgeWeights,
        setNow,
        setOutdated,
        searchedItem,
        setSearchedItem,
        setShowModal
}) => {
    const container = document.getElementById("network");
    let { decompositionName } = useParams();
    const [operations, setOperations] = useState([]);

    const [graphPositions, setGraphPositions] = useState(undefined);

    const [visGraph, setVisGraph] = useState({});
    const [network, setNetwork] = useState({});
    const [stabilizationProgress, setStabilizationProgress] = useState(undefined);

    const [menuCoordinates, setMenuCoordinates] = useState(undefined);
    const [selectNodeEvent, setSelectNodeEvent] = useState(undefined);
    const [rightClickEvent, setRightClickEvent] = useState(undefined);
    const [doubleClickEvent, setDoubleClickEvent] = useState(undefined);
    const [requestDialog, setRequestDialog] = useState(undefined);
    const [dialogResponse, setDialogResponse] = useState(undefined);

    function defaultOperations() {
        setOperations(() => [
            OPERATION.COLLAPSE,
            OPERATION.COLLAPSE_ALL,
            OPERATION.TRANSFER,
            OPERATION.MERGE,
            OPERATION.EXPAND,
            OPERATION.EXPAND_ALL,
            OPERATION.SPLIT,
            OPERATION.FORM_CLUSTER,
            OPERATION.RENAME,
            OPERATION.ONLY_NEIGHBOURS,
            OPERATION.TOGGLE_PHYSICS,
            OPERATION.SAVE,
            OPERATION.SNAPSHOT,
        ]);
    }

    function getGraphPositions(decompositionName) {
        let toastId = toast.loading("Constructing graph...", {type: toast.TYPE.INFO});
        const service = new APIService();
        service.getGraphPositions(decompositionName).then(response => {
            restoreGraph(response.data.nodes, response.data.edges);
            setGraphPositions(response.data);
            toast.dismiss(toastId);
        }).catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                const nodes = clusters.flatMap(cluster => createCluster(cluster));
                const edges = generateAllEdges(edgeWeights, property, nodes);
                restoreGraph(nodes, edges);
                toast.dismiss(toastId);
            } else toast.update(toastId, {type: toast.TYPE.ERROR, render: "Failed Graph Construction", isLoading: false});
        });
    }

    useEffect(() => {
        if (reloadPositions !== undefined) {
            getGraphPositions(decompositionName);
            setReloadPositions(undefined);
        }
    }, [reloadPositions]);

    function restoreGraph(nodes, edges) {
        visGraph.nodes.remove(visGraph.nodes.get().map(node => node.id));
        visGraph.edges.remove(visGraph.edges.get().map(edge => edge.id));
        visGraph.nodes.add(nodes);
        visGraph.edges.add(edges);
    }

    useEffect(() => {
        if (searchedItem === undefined)
            return;
        if (searchedItem.type === searchType.ENTITY) {
            let toastId = toast.loading("Preparing the view for the searching entity...", {type: toast.TYPE.INFO});
            setTimeout(() => {
                const entityId = Number(searchedItem.id);
                let entityNode = visGraph.nodes.get(entityId);
                if (entityNode === null) {
                    const clusterNode = visGraph.nodes.get().find(node => node.elements && node.elements.find(e => e.id === entityId));
                    handleExpandCluster(clusterNode);
                    entityNode = visGraph.nodes.get(entityId);
                }
                handleOnlyNeighbours(entityNode);
                setSearchedItem(undefined);
                toast.dismiss(toastId);
            }, 50);
        }
        else if (searchedItem.type === searchType.CLUSTER) {
            let toastId = toast.loading("Preparing the view for the searching cluster...", {type: toast.TYPE.INFO});
            setTimeout(() => {
                let clusterNode = visGraph.nodes.get(searchedItem.id);
                if (clusterNode === null) {
                    handleCollapseCluster(searchedItem.id);
                    clusterNode = visGraph.nodes.get(searchedItem.id);
                }
                handleOnlyNeighbours(clusterNode);
                setSearchedItem(undefined);
                toast.dismiss(toastId);
            }, 50);
        }
    }, [searchedItem]);

    useEffect(() => {
        if (clusters !== undefined && edgeWeights !== undefined && Object.keys(visGraph).length === 0) { // Only sets up one time
            const service = new APIService();
            service.getGraphPositions(decompositionName).then(response => {
                setNow(prev => prev + 10);
                setGraphPositions(response.data);
                updateNetwork(response.data);
            }).catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND)
                    toast.info("No previous positions saved.");
                else toast.error("Loading previous positions failed.");
                setGraphPositions(undefined);
                updateNetwork();
            });
        }
    }, [clusters, edgeWeights]);

    useEffect(() => {
        if (dialogResponse === undefined) return;

        const service = new APIService();
        let promise, toastId, response;

        setNow(prev => prev + 10);
        switch(dialogResponse.type) {
            case DIALOG_TYPE.RENAME:
                promise = service.renameCluster(decompositionName, {
                    type: OperationTypes.RENAME_OPERATION,
                    clusterName: clickedComponent.node.id,
                    newClusterName: dialogResponse.newName});
                toastId = toast.promise(promise, {
                    pending: "Renaming Cluster...",
                    success: {render: "Successfully changed name!", autoClose: 3000},
                    error: { render({data}) {return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed Renaming."; }, autoClose: 5000}
                })
                toastId.then(() => service.getClusters(decompositionName).then(response => setClusters(Object.values(response.data))));
                response = handleRename();
                break;
            case DIALOG_TYPE.TRANSFER:
                promise = service.transferEntities(decompositionName, {
                    type: OperationTypes.TRANSFER_OPERATION,
                    fromCluster: clickedComponent.node.id,
                    toCluster: clickedComponent.toNode.id,
                    entities: dialogResponse.elements.toString()});
                toastId = toast.promise(promise, {
                    pending: "Transferring entities from cluster " + clickedComponent.node.label + " to cluster " + clickedComponent.toNode.label + "...",
                    success: {render: "Successfully transferred entities!", autoClose: 3000},
                    error: {render: "Failed transferring entities.", autoClose: 5000}
                })
                toastId.then(() => service.getClusters(decompositionName).then(response => {setClusters(Object.values(response.data)); setOutdated(true);}));
                response = handleTransfer();
                break;
            case DIALOG_TYPE.MERGE:
                promise = service.mergeClusters(decompositionName, {
                    type: OperationTypes.MERGE_OPERATION,
                    cluster1Name: clickedComponent.node.id,
                    cluster2Name: clickedComponent.toNode.id,
                    newName: dialogResponse.newName});
                toastId = toast.promise(promise, {
                    pending: "Merging clusters to create cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully merged clusters!", autoClose: 3000},
                    error: {render: "Failed merging clusters.", autoClose: 5000}
                })
                toastId.then(() => service.getClusters(decompositionName).then(response => {setClusters(Object.values(response.data)); setOutdated(true);}));
                response = handleMerge();
                break;
            case DIALOG_TYPE.SPLIT:
                promise = service.splitCluster(decompositionName, {
                    type: OperationTypes.SPLIT_OPERATION,
                    originalCluster: clickedComponent.node.id,
                    newCluster: dialogResponse.newName,
                    entities: dialogResponse.elements.toString()});
                toastId = toast.promise(promise, {
                    pending: "Splitting cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully split cluster!", autoClose: 3000},
                    error: {render({data}) { return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed splitting.";}, autoClose: 5000}
                });
                toastId.then(() => service.getClusters(decompositionName).then(response => {setClusters(Object.values(response.data)); setOutdated(true);}));
                response = handleSplit();
                break;
            case DIALOG_TYPE.FORM_CLUSTER:
                promise = service.formCluster(decompositionName, {
                    type: OperationTypes.FORM_CLUSTER_OPERATION,
                    newCluster: dialogResponse.newName,
                    entities: dialogResponse.elements});
                toastId = toast.promise(promise, {
                    pending: "Forming new cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully formed new cluster!", autoClose: 3000},
                    error: {render({data}) {return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed forming new cluster.";}, autoClose: 5000}
                });
                toastId.then(() => service.getClusters(decompositionName).then(response => {setClusters(Object.values(response.data)); setOutdated(true);}));
                response = handleFormCluster();
                break;
        }
        toastId.then(() => {
            if (response.removableNodes) visGraph.nodes.remove(response.removableNodes);
            if (response.removableEdges) visGraph.edges.remove(response.removableEdges);
            if (response.nodes) visGraph.nodes.update(response.nodes);
            if (response.edges) visGraph.edges.update(response.edges);
            setGraphPositions(undefined);
            setStabilizationProgress("stabilizing");
            setNow(0);
            defaultOperations();
        }).catch(error => {
            if (error.response.status === HttpStatus.UNAUTHORIZED) {
                setNow(0);
                defaultOperations();
            }
            else console.error(error);
        });

        setOperations([]); // While waiting for the response, remove the ability to make other operations
        setClickedComponent(undefined);
        setRequestDialog(undefined);
        setDialogResponse(undefined);
    }, [dialogResponse]);


    function updateNetwork(graphPositions = undefined) {
        let toastId;
        try {
            let newVisGraph, corruptedSave = false;

            // Check for the existence of a corrupted save
            if (graphPositions !== undefined && graphPositions.property === property) {
                graphPositions.nodes.forEach(node => {
                    if (corruptedSave) {return;}
                    if (node.type === types.CLUSTER) {
                        const cluster = clusters.find(cluster => cluster.name === node.id);
                        if (cluster === undefined || node.elements === undefined || node.elements.length !== cluster.elements.length ||
                            !node.elements.reduce((prev, current) => prev && cluster.elements.find(e => e.id === current.id), true))
                            corruptedSave = true;
                    }
                    else {
                        const cluster = clusters.find(cluster => cluster.elements.find(e => e.id === node.id));
                        if (cluster === undefined || node.group !== cluster.name)
                            corruptedSave = true;
                    }
                });
            }

            if (graphPositions === undefined || corruptedSave === true) {
                const nodes = clusters.flatMap(cluster => createCluster(cluster));
                const edges = generateAllEdges(edgeWeights, property, nodes);
                newVisGraph = { nodes: new DataSet(nodes), edges: new DataSet(edges) };
                setGraphPositions(undefined);
            }
            else newVisGraph = {nodes: new DataSet(graphPositions.nodes), edges: new DataSet(graphPositions.edges)};
            setVisGraph(newVisGraph);
            toastId = toast.loading( "Creating graph...");

            let newNetwork = new Network(container, newVisGraph, networkOptions);

            // Whenever there is a click outside the contextMenu, it disappears
            newNetwork.on("click", () => setMenuCoordinates(undefined));

            // Whenever a click in a node is done
            newNetwork.on("selectNode", (event) => setSelectNodeEvent(event));

            // Whenever a right click is made
            newNetwork.on("oncontext", (event) => setRightClickEvent(event));

            // Whenever a double click is made
            newNetwork.on("doubleClick", (event) => setDoubleClickEvent(event));

            // Updates graph loading progress
            newNetwork.on("stabilizationProgress", function (params) {
                setNow(50 + (params.iterations * 50) / params.total);
            });

            // Remove progress bar once graph is loaded
            newNetwork.once("stabilizationIterationsDone", function () {
                setNow(0);
                setStabilizationProgress("graphReady");
            });

            // Stabilization is over (This stabilization actuates during the entire usage)
            newNetwork.on("stabilized", () => setStabilizationProgress(prev => prev === "stabilizing"? "stabilized" : prev));

            if (clusters.reduce((prev, current) => prev + current.elements.length, 0) > 100)
                newNetwork.setOptions({interaction : {hideEdgesOnDrag: true}});

            setNetwork(newNetwork);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Completed Graph Creation", isLoading: false});
            setTimeout(() => toast.dismiss(toastId), 2000);
            defaultOperations();
        } catch (error) {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Failed Graph Creation", isLoading: false});
            console.error(error);
        }
    }

    useEffect(() => {
        if (rightClickEvent === undefined) return;
        rightClickEvent.event.preventDefault(); // Avoids popping up the default right click window

        // Check if cancel operation is available
        if (operations.includes(OPERATION.CANCEL)) { setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY}); return; }

        // Multiple selection for nodes
        let selectedNodes = network.getSelectedNodes();
        if (selectedNodes.length > 1) {
            const rightClickNode = network.getNodeAt(rightClickEvent.pointer.DOM);
            if (rightClickNode !== undefined && !selectedNodes.includes(rightClickNode)) // Add right clicked node, if necessary
                selectedNodes.push(rightClickNode);

            setClickedComponent({nodes: selectedNodes.map(nodeId => visGraph.nodes.get(nodeId))});
            setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY, type: types.MULTIPLE});
            return;
        }

        // Single selection for nodes
        if (network.getNodeAt(rightClickEvent.pointer.DOM) !== undefined) {
            let node = visGraph.nodes.get(network.getNodeAt(rightClickEvent.pointer.DOM));
            setClickedComponent({node: node});

            if (node.type === types.CLUSTER) {
                setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY, type: types.CLUSTER});
            }
            else setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY, type: types.ENTITY});
        }
        else { // No differentiation was done between background or edges, but if needed, use network.getEdgeAt(rightClickEvent.pointer.DOM) to identify the clicked edge
            let newOperations;
            if (graphPositions === undefined)
                newOperations = operations.filter(op => op !== OPERATION.RESTORE);
            else newOperations = [...operations, OPERATION.RESTORE];

            if (!newOperations.includes(OPERATION.SHOW_ALL) && visGraph.nodes.get().find(node => node.type === types.CLUSTER))
                newOperations.push(OPERATION.EXPAND_ALL);
            else newOperations = newOperations.filter(op => op !== OPERATION.EXPAND_ALL);

            if (!newOperations.includes(OPERATION.SHOW_ALL) && visGraph.nodes.get().find(node => node.type !== types.CLUSTER))
                newOperations.push(OPERATION.COLLAPSE_ALL);
            else newOperations = newOperations.filter(op => op !== OPERATION.COLLAPSE_ALL);

            setOperations(newOperations);
            setClickedComponent(undefined);
            setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY, type: types.NONE});
        }
        setRightClickEvent(undefined);
    }, [rightClickEvent]);

    useEffect(() => {
        if (selectNodeEvent === undefined || clickedComponent === undefined) return;

        const selectedNode = visGraph.nodes.get(selectNodeEvent.nodes[0]);

        if (clickedComponent.operation === OPERATION.TRANSFER && selectedNode.type === types.CLUSTER) {
            if (clickedComponent.node.id === selectedNode.id) {
                toast.warning("You must click in a different cluster.", {autoClose: 5000});
                return;
            }
            toast.dismiss();
            setRequestDialog({type: DIALOG_TYPE.TRANSFER, fromCluster: clickedComponent.node.label, toCluster: selectedNode.label, elements: clickedComponent.node.elements});
            setClickedComponent(prev => {prev.toNode = selectedNode; return prev;});
        }
        else if (clickedComponent.operation === OPERATION.MERGE && selectedNode.type === types.CLUSTER) {
            if (clickedComponent.node.id === selectedNode.id) {
                toast.warning("You must click in a different cluster.", {autoClose: 5000});
                return;
            }
            toast.dismiss();
            setRequestDialog({type: DIALOG_TYPE.MERGE, fromCluster: clickedComponent.node.label, toCluster: selectedNode.label});
            setClickedComponent(prev => {prev.toNode = selectedNode; return prev;});
        }
        setSelectNodeEvent(undefined);
    }, [selectNodeEvent]);

    useEffect(() => {
        if (doubleClickEvent === undefined || dialogResponse !== undefined) // Block opening modal while backend update is being made
            return;
        if (doubleClickEvent.nodes.length > 0) // Clicked cluster or entity
            setClickedComponent({...visGraph.nodes.get(doubleClickEvent.nodes[0]), operation: "doubleClickEvent"});
        else if (doubleClickEvent.edges.length > 0) { // Clicked edge
            let edge = visGraph.edges.get(doubleClickEvent.edges[0]);
            setClickedComponent({...edge, fromNode: visGraph.nodes.get(edge.from), toNode: visGraph.nodes.get(edge.to), operation: "doubleClickEvent" });
        }
        else return; // Clicked in the background
        setShowModal(true);
        setDoubleClickEvent(undefined);
    }, [doubleClickEvent]);

    useEffect(() => {
        if ((stabilizationProgress === "stabilized" || stabilizationProgress === "graphReady") && Object.keys(visGraph).length > 0) {
            handleSave();
            setStabilizationProgress(undefined);
        }
    }, [stabilizationProgress]);

    function handleExpandCluster(clusterNode = undefined) {
        expandCluster(clickedComponent, property, network, visGraph, edgeWeights, clusterNode);
        clearMenu();
    }

    function handleExpandAll() {
        expandAll(property, network, visGraph, edgeWeights);
        clearMenu();
    }

    function handleCollapseCluster(clusterId = undefined) {
        collapseCluster(clickedComponent, property, visGraph, edgeWeights, clusterId);
        clearMenu();
    }

    function handleCollapseAll() {
        collapseAll(property, visGraph, edgeWeights);
        clearMenu();
    }

    function handleOnlyNeighbours(entityNode = undefined) {
        if (operations.includes(OPERATION.SHOW_ALL)) // Needs to reset the visible nodes and edges before hiding them again
            handleShowAll("showAll");

        if (entityNode === undefined)
            entityNode = clickedComponent.node;
        const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, [entityNode.id]);
        relatedNodesAndEdges.nodes.push(entityNode);
        const relatedNodes = relatedNodesAndEdges.nodes.map(node => node.id);
        const relatedEdges = relatedNodesAndEdges.edges.map(edge => edge.id);

        visGraph.nodes.update(visGraph.nodes.get().filter(node => !relatedNodes.includes(node.id)).map(node => ({id: node.id, hidden: true})));
        visGraph.edges.update(visGraph.edges.get().filter(edge => !relatedEdges.includes(edge.id)).map(edge => ({id: edge.id, hidden: true})));
        setOperations([OPERATION.SHOW_ALL, OPERATION.EXPAND, OPERATION.TOGGLE_PHYSICS]);
        clearMenu();
    }

    function handleShowAll(type) {
        if (type === "showAll") {
            visGraph.nodes.update(visGraph.nodes.get().filter(node => node.hidden).map(node => ({ id: node.id, hidden: false })));
            visGraph.edges.update(visGraph.edges.get().filter(edge => edge.hidden).map(edge => ({ id: edge.id, hidden: false })));
        }
        else if (type === "restore" && graphPositions !== undefined) {
            restoreGraph(graphPositions.nodes, graphPositions.edges);
        }
        defaultOperations();
        clearMenu();
    }

    function handleTogglePhysics() {
        setNetwork(prevNetwork => {
            prevNetwork.setOptions( {physics: !prevNetwork.physics.physicsEnabled} );
            return prevNetwork;
        })
        clearMenu();
    }

    function handleRenameRequest() {
        setRequestDialog({type: DIALOG_TYPE.RENAME, clusterName: clickedComponent.node.id});
        setMenuCoordinates(undefined);
    }

    function handleRename() {
        let newNode = {...clickedComponent.node, id: dialogResponse.newName, group: dialogResponse.newName, label: dialogResponse.newName};
        let graphEdges = [];
        const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, [clickedComponent.node.id]);
        let removableEdges = relatedNodesAndEdges.edges.map(edge => edge.id);
        generateNewAndAffectedEdges(edgeWeights, property, [newNode], relatedNodesAndEdges.nodes, graphEdges, removableEdges);
        return {nodes: [newNode], removableNodes: [clickedComponent.node.id], edges: graphEdges, removableEdges};
    }

    function handleTransferRequest() {
        toast.info("Click in the cluster where the entities will be transferred to.\nRight click to cancel.", {autoClose: false});

        setClickedComponent(prev => { return {...prev, operation: OPERATION.TRANSFER}; });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleTransfer() {
        let fromNode = {...clickedComponent.node};
        let toNode = {...clickedComponent.toNode};

        toNode.elements.push(...fromNode.elements.filter(entity => dialogResponse.elements.find(e => e === entity.id))); toNode.value = toNode.elements.length;
        fromNode.elements = fromNode.elements.filter(entity => !dialogResponse.elements.find(e => e === entity.id)); fromNode.value = fromNode.elements.length;

        const fromNodeRelated = getRelatedNodesAndEdges(visGraph, [fromNode.id]);
        const toNodeRelated = getRelatedNodesAndEdges(visGraph, [toNode.id]);

        let graphEdges = [], removableEdges = [];
        let affectedNodes = [...new Set([...fromNodeRelated.nodes, ...toNodeRelated.nodes])].filter(node => node.id !== fromNode.id && node.id !== toNode.id);

        generateNewAndAffectedEdges(edgeWeights, property, [fromNode, toNode], affectedNodes, graphEdges, removableEdges);
        return {nodes: [fromNode, toNode], edges: graphEdges, removableEdges};
    }

    function handleMergeRequest() {
        toast.info("Click in the cluster to be merged to.\nRight click to cancel.", {autoClose: false});

        setClickedComponent(prev => { return {...prev, operation: OPERATION.MERGE}; });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleMerge() {
        const clusterIds = [clickedComponent.node.id, clickedComponent.toNode.id];
        const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, clusterIds)
        const newClusterNode = createCluster({name: dialogResponse.newName, elements: [...clickedComponent.node.elements, ...clickedComponent.toNode.elements]});
        const newGraphEdges = generateNewAndAffectedEdges(edgeWeights, property, [newClusterNode], relatedNodesAndEdges.nodes);

        return {removableNodes: clusterIds, removableEdges: relatedNodesAndEdges.edges.map(edge => edge.id), nodes: [newClusterNode], edges: newGraphEdges};
    }

    function handleSplitRequest() {
        setRequestDialog({ type: DIALOG_TYPE.SPLIT, cluster: clickedComponent.node.label, elements: [...clickedComponent.node.elements] });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleSplit() {
        const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, [clickedComponent.node.id]);
        const newClusterNode = createCluster({
            name: dialogResponse.newName,
            elements: [...clickedComponent.node.elements.filter(e => dialogResponse.elements.includes(e.id))]
        });
        let toUpdateCluster = {...clickedComponent.node};
        toUpdateCluster.elements = toUpdateCluster.elements.filter(entity => !dialogResponse.elements.find(e => e === entity.id));
        toUpdateCluster.value = toUpdateCluster.elements.length;

        let edges = [], removableEdges = [];
        generateNewAndAffectedEdges(edgeWeights, property, [newClusterNode, toUpdateCluster], relatedNodesAndEdges.nodes, edges, removableEdges);
        return {nodes: [newClusterNode, toUpdateCluster], edges, removableEdges};
    }

    function handleFormClusterRequest() {
        const nodes = clickedComponent.nodes? clickedComponent.nodes : [clickedComponent.node];
        setRequestDialog({type: DIALOG_TYPE.FORM_CLUSTER,
            elements: nodes.flatMap(node => {
                if (node.type === types.CLUSTER)
                    return node.elements.map(entity => ({id: entity.id, name: entity.name, cluster: node.label}));
                return [{id: node.id, name: node.label, cluster: node.group}]; })
        });
        setMenuCoordinates(undefined);
    }

    function handleFormCluster() {
        let nodes = [], edges = [], removableNodes = [], removableEdges = [];
        let newClusterNode = createCluster({name: dialogResponse.newName, elements: []});

        Object.entries(dialogResponse.elements).forEach(([clusterName, elements]) => {
            const allGraphNodes = visGraph.nodes.get();
            let clusterNode = allGraphNodes.find(node => node.id === clusterName);
            if (clusterNode !== undefined) {
                if (clusterNode.elements.length === elements.length) { // The entire cluster was selected to be added into the new cluster
                    newClusterNode.elements.push(...clusterNode.elements);
                    removableNodes.push(clusterNode.id);
                } else {
                    newClusterNode.elements.push(...clusterNode.elements.filter(entity => elements.find(e => e === entity.id)));
                    clusterNode.elements = clusterNode.elements.filter(entity => !elements.find(e => e === entity.id));
                    clusterNode.value = clusterNode.elements.length;
                    nodes.push(clusterNode);
                }
                newClusterNode.value = newClusterNode.elements.length;
            }
            else { // Entities were singularly added or filtered from cluster
                removableNodes.push(...elements);
                for (let element of elements) {
                    let entityNode = allGraphNodes.find(e => e.id === element);
                    newClusterNode.elements.push({id: entityNode.id, name: entityNode.label});
                }
                newClusterNode.value = newClusterNode.elements.length;
            }
        });

        let relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, [...removableNodes, ...nodes.map(node => node.id)]);
        removableEdges.push(...relatedNodesAndEdges.edges.map(edge => edge.id));
        nodes.push(newClusterNode);
        generateNewAndAffectedEdges(edgeWeights, property, nodes, relatedNodesAndEdges.nodes, edges, removableEdges);

        return ({nodes, edges, removableNodes, removableEdges});
    }

    function handleSave() {
        const graphPositions = buildGraphPositions();

        const service = new APIService();

        const promise = service.saveGraphPositions(decompositionName, graphPositions);
        toast.promise(promise, {
            pending: "Saving Graph...",
            success: {render: "Successfully Saved Graph Positions!", autoClose: 2000},
            error: {render: "Error while Saving Graph Positions", autoClose: 5000}
        }).then(() => setGraphPositions(graphPositions));

        clearMenu();
    }

    function handleSnapshot() {
        const service = new APIService();

        const promise = service.snapshotDecomposition(decompositionName);
        toast.promise(promise, {
            pending: "Snapshotting decomposition...",
            success: {render: "Successfully snapshot decomposition!", autoClose: 2000},
            error: {render: "Error while snapshotting decomposition", autoClose: 5000}
        });

        clearMenu();
    }

    function buildGraphPositions() {
        let graphPositions = {nodes: visGraph.nodes.get(), edges: visGraph.edges.get()};

        graphPositions.property = property;

        // Updates positions of nodes and clears hidden
        graphPositions.nodes.forEach(node => {
            const position = network.getPosition(node.id);
            node.x = position.x;
            node.y = position.y;
            delete node["hidden"];
        });
        graphPositions.edges.forEach(edge => delete edge["hidden"]);

        return graphPositions;
    }

    function handleCancel() {
        toast.dismiss();
        setRequestDialog(undefined);
        setMenuCoordinates(undefined);
        setClickedComponent(undefined);
        defaultOperations();
    }

    function clearMenu() {
        setClickedComponent(undefined);
        setMenuCoordinates(undefined);
    }

    return (
        <Container fluid>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            <div id="network"></div>

            <RightClickMenu
                menuCoordinates={menuCoordinates}
                operations={operations}
                handleExpandCluster={handleExpandCluster}
                handleExpandAll={handleExpandAll}
                handleRenameRequest={handleRenameRequest}
                handleOnlyNeighbours={handleOnlyNeighbours}
                handleCollapseCluster={handleCollapseCluster}
                handleCollapseAll={handleCollapseAll}
                handleShowAll={handleShowAll}
                handleTogglePhysics={handleTogglePhysics}
                handleTransferRequest={handleTransferRequest}
                handleMergeRequest={handleMergeRequest}
                handleSplitRequest={handleSplitRequest}
                handleFormClusterRequest={handleFormClusterRequest}
                handleSnapshot={handleSnapshot}
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
        </Container>
    );
}