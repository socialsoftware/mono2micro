import React, {createRef, useEffect, useState} from "react";
import {RepositoryService} from "../../../../services/RepositoryService";
import {toast, ToastContainer} from "react-toastify";
import HttpStatus from "http-status-codes";
import {useParams} from "react-router-dom";
import {CancelPresentation, Redo, Search, TableView, Timeline, Undo} from "@mui/icons-material";
import {DataSet, Network} from "vis-network/standalone";
import {types, views} from "../Views";
import {EDGE_LENGTH, OPERATION} from "../../../../constants/constants";
import {ClusterViewRightClickMenu} from "./ClusterViewRightClickMenu";
import {ClusterViewDialogs, DIALOG_TYPE} from "./ClusterViewDialogs";
import {ClusterViewModal} from "./ClusterViewModal";
import {ClusterViewMetricTable} from "./ClusterViewMetricTable";
import {searchType} from "../ViewSearchBar";

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
    Also undo/redo operations on the speed dial<br />
    To select multiple nodes, leave the mouse
    button pressed or use "Ctrl+click" to add nodes.
    Then right click to see available operations.<br />
</div>);

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
        //stabilization: {
        //    enabled: false // this greatly improves loading time, at the expense of a malformed initial graph
        //},
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

export const ClusterViewGraph = ({setNow, outdated, setOutdated, searchedItem, setSearchedItem, changeToFunctionalities, setOpenSearch, setActions, view}) => {
    const appRef = createRef();
    let { decompositionName } = useParams();
    const [operations, setOperations] = useState([]);
    const [clusters, setClusters] = useState(undefined);

    const [clustersFunctionalities, setClustersFunctionalities] = useState({});
    const [edgeWeights, setEdgeWeights] = useState([]);
    const [graphPositions, setGraphPositions] = useState(undefined);
    const [stabilizationProgress, setStabilizationProgress] = useState(undefined);

    const [updateGraphVis, setUpdateGraphVis] = useState({});
    const [visGraph, setVisGraph] = useState({});
    const [network, setNetwork] = useState({});
    const [showModal, setShowModal] = useState(false);
    const [showTable, setShowTable] = useState(false);
    const [reload, setReload] = useState(undefined);

    const [menuCoordinates, setMenuCoordinates] = useState(undefined);
    const [clickedComponent, setClickedComponent] = useState(undefined);
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

    const speedDialActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <Undo/>, name: 'Undo', handler: handleUndo },
        { icon: <Redo/>, name: 'Redo', handler: handleRedo },
        { icon: <CancelPresentation/>, name: 'Go to Top', handler: handleScrollTop },
        { icon: <TableView/>, name: 'Go to Metrics' , handler: handleScrollBottom },
        { icon: <Timeline/>, name: 'Go to Functionalities', handler: changeToFunctionalities },
    ];

    function handleScrollTop() { changeSpeedDial(false); setShowTable(false); }

    function handleScrollBottom() { changeSpeedDial(true); setShowTable(true); }

    function handleUndo() {
        const service = new RepositoryService();
        service.undoOperation(decompositionName).then(response => {
            setClusters(Object.values(response.data));
            setOutdated(true);
            setReload({});
        });
    }

    function handleRedo() {
        const service = new RepositoryService();
        service.redoOperation(decompositionName).then(response => {
            setClusters(Object.values(response.data));
            setOutdated(true);
            setReload({});
        });
    }

    useEffect(() => {
        if (reload !== undefined) {
            getGraphPositions(decompositionName);
            changeSpeedDial(showTable);
            setReload(undefined);
        }
    }, [reload]);

    function getGraphPositions(decompositionName) {
        let toastId = toast.loading("Constructing graph...", {type: toast.TYPE.INFO});
        const service = new RepositoryService();
        service.getGraphPositions(decompositionName).then(response => {
            restoreGraph(response.data.nodes, response.data.edges);
            setGraphPositions(response.data);
            toast.dismiss(toastId);
        }).catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                const nodes = clusters.flatMap(cluster => createCluster(cluster));
                const edges = generateAllEdges(nodes);
                restoreGraph(nodes, edges);
                toast.dismiss(toastId);
            } else toast.update(toastId, {type: toast.TYPE.ERROR, render: "Failed Graph Construction", isLoading: false});
        });
    }

    function restoreGraph(nodes, edges) {
        visGraph.nodes.remove(visGraph.nodes.get().map(node => node.id));
        visGraph.edges.remove(visGraph.edges.get().map(edge => edge.id));
        visGraph.nodes.add(nodes);
        visGraph.edges.add(edges);
    }

    useEffect(() => {
        if (showTable) window.scrollTo(0, document.body.scrollHeight);
        else window.scrollTo(0, 0);
    }, [showTable]);

    useEffect(() => {
        if (view === views.CLUSTERS)
            changeSpeedDial(showTable);
    }, [view]);

    function changeSpeedDial(showTable) {
        setActions(speedDialActions);
        const service = new RepositoryService();
        service.canUndoRedo(decompositionName).then(response => {
            setActions(actions => [...actions.filter(action => action.name !== "Undo" && action.name !== "Redo")]);
            if (response.data["undo"])
                setActions(actions => [...actions, {icon: <Undo/>, name: 'Undo', handler: handleUndo}]);
            if (response.data["redo"])
                setActions(actions => [...actions, {icon: <Redo/>, name: 'Redo', handler: handleRedo}]);
        });

        setActions(actions => [...actions.filter(action => action.name !== "Go to Top" && action.name !== "Go to Metrics")]);
        if (showTable === true)
            setActions(actions => [...actions, { icon: <CancelPresentation/>, name: 'Go to Top', handler: handleScrollTop }]);
        else setActions(actions => [...actions, { icon: <TableView/>, name: 'Go to Metrics' , handler: handleScrollBottom }]);
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

    function loadClustersAndClustersFunctionalities() {
        const service = new RepositoryService();
        service.getClustersAndClustersFunctionalities(decompositionName).then(response => {
            setClusters(Object.values(response.data.clusters).sort((a, b) => a.name - b.name));
            setClustersFunctionalities(response.data.clustersFunctionalities);
            setOutdated(false);
        });
    }

    useEffect(() => {
        if (outdated) {
            setClustersFunctionalities({});

            if (view === views.CLUSTERS && showTable) // Avoid information mismatch in metric table
                loadClustersAndClustersFunctionalities();
            else setShowTable(false);
        }
    }, [outdated]);

    useEffect(() => {
        if ((outdated || Object.keys(clustersFunctionalities).length === 0) && (showModal || showTable))
            loadClustersAndClustersFunctionalities();
    }, [showModal, showTable]);

    // This will only be executed once variable clusters is set and only runs once during the entire execution
    useEffect(() => {
        const service = new RepositoryService();

        // Loads decomposition's properties
        service.getClustersAndClustersFunctionalities(decompositionName).then(response => {
            setNow(prev => prev + 20);
            setClusters(Object.values(response.data.clusters).sort((a, b) => a.name - b.name));
            setClustersFunctionalities(response.data.clustersFunctionalities);
            setOutdated(false);

            // Entities
            const first = service.getEdgeWeights(decompositionName).then(response => {
                setNow(prev => prev + 10);
                setEdgeWeights(response.data);
            });

            // Entity and cluster positions
            const second = service.getGraphPositions(decompositionName).then(response => {
                setNow(prev => prev + 10);
                setGraphPositions(response.data);
            }).catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND)
                    toast.info("No previous positions saved.");
                else toast.error("Loading previous positions failed.");
                setGraphPositions(undefined)});

            // Waits for both requests to be fulfilled
            Promise.all([first, second]).then(() => setUpdateGraphVis({}));
        }).catch(error =>
            console.error("Error during decomposition update.", error)
        );
    }, []);

    useEffect(() => {
        if (clusters !== undefined && edgeWeights.length !== 0 && Object.keys(visGraph).length === 0) // Only sets up one time
            updateNetwork();
    }, [updateGraphVis]);

    useEffect(() => {
        if (dialogResponse === undefined) return;

        const service = new RepositoryService();
        let promise, toastId, response;

        setNow(prev => prev + 10);
        switch(dialogResponse.type) {
            case DIALOG_TYPE.RENAME:
                promise = service.renameCluster(decompositionName, clickedComponent.node.id, dialogResponse.newName);
                toastId = toast.promise(promise, {
                    pending: "Renaming Cluster...",
                    success: {render: "Successfully changed name!", autoClose: 3000},
                    error: { render({data}) {return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed Renaming."; }, autoClose: 5000}
                })
                toastId.then((response) => setClusters(Object.values(response.data)));
                response = handleRename();
                break;
            case DIALOG_TYPE.TRANSFER:
                promise = service.transferEntities(decompositionName, clickedComponent.node.id, clickedComponent.toNode.id, dialogResponse.elements.toString());
                toastId = toast.promise(promise, {
                    pending: "Transferring entities from cluster " + clickedComponent.node.label + " to cluster " + clickedComponent.toNode.label + "...",
                    success: {render: "Successfully transferred entities!", autoClose: 3000},
                    error: {render: "Failed transferring entities.", autoClose: 5000}
                })
                toastId.then((response) => {setClusters(Object.values(response.data)); setOutdated(true);});
                response = handleTransfer();
                break;
            case DIALOG_TYPE.MERGE:
                promise = service.mergeClusters(decompositionName, clickedComponent.node.id, clickedComponent.toNode.id, dialogResponse.newName);
                toastId = toast.promise(promise, {
                    pending: "Merging clusters to create cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully merged clusters!", autoClose: 3000},
                    error: {render: "Failed merging clusters.", autoClose: 5000}
                })
                toastId.then((response) => {setClusters(Object.values(response.data)); setOutdated(true);});
                response = handleMerge();
                break;
            case DIALOG_TYPE.SPLIT:
                promise = service.splitCluster(decompositionName, clickedComponent.node.id, dialogResponse.newName, dialogResponse.elements.toString());
                toastId = toast.promise(promise, {
                    pending: "Splitting cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully split cluster!", autoClose: 3000},
                    error: {render({data}) { return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed splitting.";}, autoClose: 5000}
                });
                toastId.then((response) => {setClusters(Object.values(response.data)); setOutdated(true);});
                response = handleSplit();
                break;
            case DIALOG_TYPE.FORM_CLUSTER:
                promise = service.formCluster(decompositionName, dialogResponse.newName, dialogResponse.elements);
                toastId = toast.promise(promise, {
                    pending: "Forming new cluster " + dialogResponse.newName + "...",
                    success: {render: "Successfully formed new cluster!", autoClose: 3000},
                    error: {render({data}) {return data.response.status === HttpStatus.UNAUTHORIZED? "This name is already being used by a cluster." : "Failed forming new cluster.";}, autoClose: 5000}
                });
                toastId.then((response) => {setClusters(Object.values(response.data)); setOutdated(true);});
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
            changeSpeedDial(showTable);
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


function updateNetwork() {
        let toastId;
        try {
            let visGraph, corruptedSave = false;

            // Check for the existence of a corrupted save
            if (graphPositions !== undefined) {
                graphPositions.nodes.forEach(node => {
                    if (corruptedSave) return;
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
                const edges = generateAllEdges(nodes);

                visGraph = { nodes: new DataSet(nodes), edges: new DataSet(edges) };
                setGraphPositions(undefined);
            }
            else visGraph = {nodes: new DataSet(graphPositions.nodes), edges: new DataSet(graphPositions.edges)};

            setVisGraph(visGraph);
            changeSpeedDial(false);

            toastId = toast.loading( "Creating graph...");

            let newNetwork = new Network(appRef.current, visGraph, options);

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
        else if (doubleClickEvent.edges.length > 0) // Clicked edge
            setClickedComponent({...visGraph.edges.get(doubleClickEvent.edges[0]), operation: "doubleClickEvent"});
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
        if (clusterNode === undefined)
            clusterNode = clickedComponent.node;
        const position = network.getPosition(clusterNode.id);
        const relatedNodesAndEdges = getRelatedNodesAndEdges([clusterNode.id]);
        visGraph.nodes.remove(clusterNode.id);
        visGraph.edges.remove(relatedNodesAndEdges.edges.map(edge => edge.id));

        const nodes = clusterNode.elements.map(entity => createEntity(entity, clusterNode.id, position));
        visGraph.nodes.add(nodes);

        const newGraphEdges = generateNewAndAffectedEdges(nodes, relatedNodesAndEdges.nodes);
        visGraph.edges.add(newGraphEdges);
        clearMenu();
    }

    function handleExpandAll() {
        let clusterNodes = [], newNodes = [], existingNodes = [];
        visGraph.nodes.get().forEach(node => {
            if (node.type === types.CLUSTER) {
                const position = network.getPosition(node.id);
                newNodes.push(...node.elements.map(entity => createEntity(entity, node.id, position)));
                clusterNodes.push(node);
            }
            else existingNodes.push(node);
        });

        visGraph.nodes.remove(clusterNodes.map(cluster => cluster.id));
        visGraph.edges.remove(visGraph.edges.get().filter(edge => edge.type !== types.BETWEEN_ENTITIES).map(edge => edge.id));
        visGraph.nodes.add(newNodes);

        const newGraphEdges = generateNewAndAffectedEdges(newNodes, existingNodes);

        visGraph.edges.add(newGraphEdges);
        clearMenu();
    }

    function handleCollapseCluster(clusterId = undefined) {
        if (clusterId === undefined)
            clusterId = clickedComponent.node.group;
        const entityNodes = visGraph.nodes.get().filter(node => node.group === clusterId);
        const clusterEntitiesIDs = entityNodes.map(node => node.id);
        const relatedNodesAndEdges = getRelatedNodesAndEdges(clusterEntitiesIDs);
        const newClusterNode = createCluster({name: entityNodes[0].group, elements: entityNodes.map(node => {return {id: node.id, name: node.label}})});

        const newGraphEdges = generateNewAndAffectedEdges([newClusterNode], relatedNodesAndEdges.nodes);

        visGraph.nodes.remove(clusterEntitiesIDs);
        visGraph.edges.remove(relatedNodesAndEdges.edges.map(edge => edge.id));

        visGraph.nodes.add(newClusterNode);
        visGraph.edges.add(newGraphEdges);
        clearMenu();
    }

    function handleCollapseAll() {
        let existingClusterNodes = [], clusterNodeInformation = {};
        const entityNodes = visGraph.nodes.get().filter(node => {
            if (node.type === types.CLUSTER) {
                existingClusterNodes.push(node);
                return false;
            }
            else {
                let info = clusterNodeInformation[node.group];
                if (info === undefined)
                    clusterNodeInformation[node.group] = {name: node.group, elements: [{id: node.id, name: node.label}]}
                else info.elements.push({id: node.id, name: node.label});
                return true;
            }
        });
        visGraph.nodes.remove(entityNodes.map(entity => entity.id));
        visGraph.edges.remove(visGraph.edges.get().filter(edge => edge.type !== types.BETWEEN_CLUSTERS).map(edge => edge.id));

        const newClusterNodes = Object.values(clusterNodeInformation).map(cluster => createCluster(cluster));
        const newGraphEdges = generateNewAndAffectedEdges(newClusterNodes, existingClusterNodes);

        visGraph.nodes.add(newClusterNodes);
        visGraph.edges.add(newGraphEdges);
        clearMenu();
    }

    function handleOnlyNeighbours(entityNode = undefined) {
        if (operations.includes(OPERATION.SHOW_ALL)) // Needs to reset the visible nodes and edges before hiding them again
            handleShowAll("showAll");

        if (entityNode === undefined)
            entityNode = clickedComponent.node;
        const relatedNodesAndEdges = getRelatedNodesAndEdges([entityNode.id]);
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
        const relatedNodesAndEdges = getRelatedNodesAndEdges([clickedComponent.node.id]);
        let removableEdges = relatedNodesAndEdges.edges.map(edge => edge.id);
        generateNewAndAffectedEdges([newNode], relatedNodesAndEdges.nodes, graphEdges, removableEdges);
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

        const fromNodeRelated = getRelatedNodesAndEdges([fromNode.id]);
        const toNodeRelated = getRelatedNodesAndEdges([toNode.id]);

        let graphEdges = [], removableEdges = [];
        let affectedNodes = [...new Set([...fromNodeRelated.nodes, ...toNodeRelated.nodes])].filter(node => node.id !== fromNode.id && node.id !== toNode.id);

        generateNewAndAffectedEdges([fromNode, toNode], affectedNodes, graphEdges, removableEdges);
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
        const relatedNodesAndEdges = getRelatedNodesAndEdges(clusterIds)
        const newClusterNode = createCluster({name: dialogResponse.newName, elements: [...clickedComponent.node.elements, ...clickedComponent.toNode.elements]});
        const newGraphEdges = generateNewAndAffectedEdges([newClusterNode], relatedNodesAndEdges.nodes);

        return {removableNodes: clusterIds, removableEdges: relatedNodesAndEdges.edges.map(edge => edge.id), nodes: [newClusterNode], edges: newGraphEdges};
    }

    function handleSplitRequest() {
        setRequestDialog({ type: DIALOG_TYPE.SPLIT, cluster: clickedComponent.node.label, elements: [...clickedComponent.node.elements] });
        setOperations([OPERATION.CANCEL]);
        setMenuCoordinates(undefined);
    }

    function handleSplit() {
        const relatedNodesAndEdges = getRelatedNodesAndEdges([clickedComponent.node.id]);
        const newClusterNode = createCluster({
            name: dialogResponse.newName,
            elements: [...clickedComponent.node.elements.filter(e => dialogResponse.elements.includes(e.id))]
        });
        let toUpdateCluster = {...clickedComponent.node};
        toUpdateCluster.elements = toUpdateCluster.elements.filter(entity => !dialogResponse.elements.find(e => e === entity.id));
        toUpdateCluster.value = toUpdateCluster.elements.length;

        let edges = [], removableEdges = [];
        generateNewAndAffectedEdges([newClusterNode, toUpdateCluster], relatedNodesAndEdges.nodes, edges, removableEdges);
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

        let relatedNodesAndEdges = getRelatedNodesAndEdges([...removableNodes, ...nodes.map(node => node.id)]);
        removableEdges.push(...relatedNodesAndEdges.edges.map(edge => edge.id));
        nodes.push(newClusterNode);
        generateNewAndAffectedEdges(nodes, relatedNodesAndEdges.nodes, edges, removableEdges);

        return ({nodes, edges, removableNodes, removableEdges});
    }

    function handleSave() {
        const graphPositions = buildGraphPositions();

        const service = new RepositoryService();

        const promise = service.saveGraphPositions(decompositionName, graphPositions);
        toast.promise(promise, {
            pending: "Saving Graph...",
            success: {render: "Successfully Saved Graph Positions!", autoClose: 2000},
            error: {render: "Error while Saving Graph Positions", autoClose: 5000}
        }).then(() => setGraphPositions(graphPositions));

        clearMenu();
    }

    function handleSnapshot() {
        const service = new RepositoryService();

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

    function getRelatedNodesAndEdges(nodeIds) {
        const relatedEdges = visGraph.edges.get().filter(edge => nodeIds.includes(edge.from) || nodeIds.includes(edge.to));
        const relatedNodes = [...new Set(relatedEdges.map(edge => nodeIds.includes(edge.from)? visGraph.nodes.get(edge.to) : visGraph.nodes.get(edge.from)))]
            .filter(node => !nodeIds.includes(node.id));
        return ({nodes: relatedNodes, edges: relatedEdges});
    }

    const createEntity = (entity, clusterName, position = undefined) => {
        let node = {
            id: entity.id,
            group: clusterName,
            type: types.ENTITY,
            label: entity.name,
            value: 1
        };
        if (position !== undefined) {
            node.x = position.x;
            node.y = position.y;
        }
        return node;
    }

    const createCluster = (cluster, position = undefined) => {
        let node = {
            id: cluster.name,
            group: cluster.name,
            type: types.CLUSTER,
            elements: cluster.elements,
            label: cluster.name,
            value: cluster.elements.length,
            borderWidth: 3,
            shape: "box",
            scaling: { label: { enabled: true, min: 20, max: 80 } }
        }
        if (position !== undefined) {
            node.x = position.x;
            node.y = position.y;
        }
        return node;
    }

    function generateAllEdges(nodes, graphEdges = [], removableEdges = undefined) {
        for (let i = 0; i < nodes.length; i++)
            for (let j = i + 1; j < nodes.length; j++)
                generateEdge(nodes[i], nodes[j], graphEdges, removableEdges);
        return graphEdges;
    }

    function generateNewAndAffectedEdges(newNodes, affectedNodes, graphEdges = [], removableEdges = undefined) {
        for (let i = 0; i < newNodes.length; i++) // New entities
            for (let j = 0; j < affectedNodes.length; j++) // Affected neighbours
                generateEdge(newNodes[i], affectedNodes[j], graphEdges, removableEdges);

        for (let i = 0; i < newNodes.length; i++) // Between new entities
            for (let j = i + 1; j < newNodes.length; j++)
                generateEdge(newNodes[i], newNodes[j], graphEdges, removableEdges);
        return graphEdges;
    }

    function generateEdge(node1, node2, graphEdges, removableEdges = undefined) {
        // Both Clusters edge
        if (node1.type === types.CLUSTER && node2.type === types.CLUSTER) {
            let allFunctionalitiesInCommon = [], fullLength = 0, counter = 0;
            node1.elements.map(e => e.id).forEach(entity1 => {
                node2.elements.map(e => e.id).forEach(entity2 => {
                    const edge = edgeWeights.find(edge => edge.e1ID === entity1 && edge.e2ID === entity2 || edge.e1ID === entity2 && edge.e2ID === entity1);
                    if (edge !== undefined) {
                        allFunctionalitiesInCommon.push(...edge.functionalities);
                        fullLength += edge.dist;
                        counter++;
                    }
                });
            });

            if (allFunctionalitiesInCommon.length !== 0) {
                const uniqueFunctionalitiesInCommon = [...new Set(allFunctionalitiesInCommon)];
                graphEdges.push(createEdge(node1.id, node2.id, types.BETWEEN_CLUSTERS, fullLength / counter, uniqueFunctionalitiesInCommon.length));
            }
            else if (removableEdges !== undefined)
                removableEdges.push(getEdgeId(node1.id, node2.id));
        }
        // Both Entities edge
        else if (node1.type === types.ENTITY && node2.type === types.ENTITY) {
            const edge = edgeWeights.find(edge => edge.e1ID === node1.id && edge.e2ID === node2.id || edge.e1ID === node2.id && edge.e2ID === node1.id);
            if (edge !== undefined) {
                graphEdges.push(createEdge(node1.id, node2.id, types.BETWEEN_ENTITIES, edge.dist, edge.functionalities.length));
            }
            else if (removableEdges !== undefined)
                removableEdges.push(getEdgeId(node1.id, node2.id));
        }
        // Cluster and Entity edge
        else {
            let allFunctionalitiesInCommon = [], fullLength = 0, counter = 0;
            const cluster = node1.type === types.CLUSTER ? node1 : node2, entity = cluster === node1 ? node2 : node1;
            cluster.elements.forEach(clusterEntity => {
                const edge = edgeWeights.find(edge => edge.e1ID === clusterEntity.id && edge.e2ID === entity.id || edge.e1ID === entity.id && edge.e2ID === clusterEntity.id);
                if (edge !== undefined) {
                    allFunctionalitiesInCommon.push(...edge.functionalities);
                    fullLength += edge.dist;
                    counter++;
                }
            });

            if (allFunctionalitiesInCommon.length !== 0) {
                const uniqueFunctionalitiesInCommon = [...new Set(allFunctionalitiesInCommon)]; // filters repeated values
                graphEdges.push(createEdge(cluster.id, entity.id, types.BETWEEN_CLUSTER_ENTITY, fullLength / counter, uniqueFunctionalitiesInCommon.length));
            }
            else if (removableEdges !== undefined)
                removableEdges.push(getEdgeId(node1.id, node2.id));
        }
    }

    const createEdge = (from, to, type, length, value) => {
        if (from < to)
            return { from: from, to: to, id: from + "&" + to, type, length: length * EDGE_LENGTH, value };
        return { from: to, to: from, id: to + "&" + from, type, length: length * EDGE_LENGTH, value };
    }

    const getEdgeId = (from, to) => {
        if (from < to)
            return from + "&" + to;
        return to + "&" + from;
    }

    return (
        <>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            <div ref={appRef}/>

            <ClusterViewModal
                clusters={clusters}
                edgeWeights={edgeWeights}
                outdated={outdated}
                clustersFunctionalities={clustersFunctionalities}
                visGraph={visGraph}
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

            {showTable &&
                <>
                    <div id="metricTable"></div> {/*This is used as an anchor*/}
                    <ClusterViewMetricTable
                        clusters={clusters}
                        clustersFunctionalities={clustersFunctionalities}
                        outdated={outdated}
                    />
                </>
            }
        </>
    );
}