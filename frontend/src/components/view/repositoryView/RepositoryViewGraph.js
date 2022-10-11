import React, {createRef, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import {RepositoryService} from "../../../services/RepositoryService";
import Breadcrumb from "react-bootstrap/Breadcrumb";
import Popover from "react-bootstrap/Popover";
import Container from "react-bootstrap/Container";
import {ModalProgressBar} from "../../util/ModalProgressBar";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Button from "react-bootstrap/Button";
import {DataSet, Network} from "vis-network/standalone";
import {collapseAll, collapseCluster, createCluster, expandAll, expandCluster, generateAllEdges, networkOptions, types} from "../utils/GraphUtils";
import {RepositoryViewModal} from "./RepositoryViewModal";
import {RightClickMenu} from "../utils/RightClickMenu";
import {OPERATION} from "../../../constants/constants";

export const RepositoryViewGraph = () => {
    const appRef = createRef();
    let {codebaseName, strategyName, decompositionName} = useParams();

    const [now, setNow] = useState(0);
    const [update, setUpdate] = useState(undefined);
    const [actions, setActions] = useState([]);
    const [decomposition, setDecomposition] = useState(undefined);
    const [edgeWeights, setEdgeWeights] = useState(undefined);
    const [showModal, setShowModal] = useState(false);
    const [visGraph, setVisGraph] = useState({});
    const [network, setNetwork] = useState({});
    const [operations, setOperations] = useState([]);

    const [menuCoordinates, setMenuCoordinates] = useState(undefined);
    const [rightClickEvent, setRightClickEvent] = useState(undefined);
    const [doubleClickEvent, setDoubleClickEvent] = useState(undefined);
    const [clickedComponent, setClickedComponent] = useState(undefined);

    function defaultOperations() {
        setOperations(() => [
            OPERATION.COLLAPSE,
            OPERATION.COLLAPSE_ALL,
            OPERATION.EXPAND,
            OPERATION.EXPAND_ALL,
            OPERATION.ONLY_NEIGHBOURS,
            OPERATION.TOGGLE_PHYSICS,
        ]);
    }


    useEffect(() => {
        setNow(10);
        //TODO AccAndRepoSciPyDecomposition should not have modification rights in this view

        const service = new RepositoryService();
        const response1 = service.getDecomposition(decompositionName).then(response => { setNow(n => n + 30); setDecomposition(response);});
        const response2 = service.getEdgeWeights(decompositionName, "REPOSITORY_DECOMPOSITION").then(response => { setNow(n => n + 30); setEdgeWeights(response.data);});
        Promise.all([response1, response2]).then(() => setUpdate({}));
    }, []);

    useEffect(() => {
        if (decomposition !== undefined && edgeWeights !== undefined)
            updateNetwork();
    },[update]);

    function getHelpText() {
        return (<div>
            This view shows the relations between the modifications made to the entities
            and the authors that applied them.
        </div>);
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>
                    {codebaseName}
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}/similarity`}>
                    {strategyName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {decompositionName}
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    const helpPopover = (
        <Popover id="helpPopover" title={"Repository View"}>
            {getHelpText()}
        </Popover>
    );

    function updateNetwork() {
        const nodes = Object.values(decomposition.clusters).flatMap(cluster => createCluster(cluster));
        const edges = generateAllEdges(edgeWeights, "commits", nodes);
        console.log(decomposition, edgeWeights);

        let newVisGraph = { nodes: new DataSet(nodes), edges: new DataSet(edges) };
        setVisGraph(newVisGraph);

        let newNetwork = new Network(appRef.current, newVisGraph, networkOptions);

        // Whenever there is a click outside the contextMenu, it disappears
        newNetwork.on("click", () => setMenuCoordinates(undefined));

        // Whenever a click in a node is done
        //newNetwork.on("selectNode", (event) => setSelectNodeEvent(event));

        // Whenever a right click is made
        newNetwork.on("oncontext", (event) => setRightClickEvent(event));

        // Whenever a double click is made
        newNetwork.on("doubleClick", (event) => setDoubleClickEvent(event));

        // Updates graph loading progress
        newNetwork.on("stabilizationProgress", function (params) { setNow(50 + (params.iterations * 50) / params.total)});

        // Remove progress bar once graph is loaded
        newNetwork.once("stabilizationIterationsDone", function () {setNow(0)});

        if (Object.values(decomposition.clusters).reduce((prev, current) => prev + current.elements.length, 0) > 100)
            newNetwork.setOptions({interaction : {hideEdgesOnDrag: true}});

        setNetwork(newNetwork);
        defaultOperations();
    }

    useEffect(() => {
        if (rightClickEvent === undefined) return;
        rightClickEvent.event.preventDefault(); // Avoids popping up the default right click window

        // Check if cancel operation is available
        if (operations.includes(OPERATION.CANCEL)) { setMenuCoordinates({left: rightClickEvent.event.pageX, top: rightClickEvent.event.pageY}); return; }

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
            let newOperations = [...operations];

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
        if (doubleClickEvent === undefined) // Block opening modal while backend update is being made
            return;
        if (doubleClickEvent.nodes.length > 0) // Clicked cluster or entity
            setClickedComponent({...visGraph.nodes.get(doubleClickEvent.nodes[0]), operation: "doubleClickEvent"});
        else if (doubleClickEvent.edges.length > 0) // Clicked edge
            setClickedComponent({...visGraph.edges.get(doubleClickEvent.edges[0]), operation: "doubleClickEvent"});
        else return; // Clicked in the background
        setShowModal(true);
        setDoubleClickEvent(undefined);
    }, [doubleClickEvent]);

    function clearMenu() {
        setClickedComponent(undefined);
        setMenuCoordinates(undefined);
    }

    function handleExpandCluster(clusterNode = undefined) {
        expandCluster(clickedComponent, "commits", network, visGraph, edgeWeights, clusterNode);
        clearMenu();
    }

    function handleExpandAll() {
        expandAll("commits", network, visGraph, edgeWeights);
        clearMenu();
    }

    function handleCollapseCluster(clusterId = undefined) {
        collapseCluster(clickedComponent, "commits", visGraph, edgeWeights, clusterId);
        clearMenu();
    }

    function handleCollapseAll() {
        collapseAll("commits", visGraph, edgeWeights);
        clearMenu();
    }

    return (
        <Container fluid>
            <div ref={appRef}/>

            <ModalProgressBar
                now={now}
            />

            <RepositoryViewModal
                decomposition={decomposition}
                edgeWeights={edgeWeights}
                visGraph={visGraph}
                showModal={showModal}
                setShowModal={setShowModal}
                clickedComponent={clickedComponent}
                setClickedComponent={setClickedComponent}
            />

            <RightClickMenu
                menuCoordinates={menuCoordinates}
                operations={operations}
                handleExpandCluster={handleExpandCluster}
                handleExpandAll={handleExpandAll}
                handleRenameRequest={() => {}}
                handleOnlyNeighbours={() => {}}
                handleCollapseCluster={handleCollapseCluster}
                handleCollapseAll={handleCollapseAll}
                handleShowAll={() => {}}
                handleTogglePhysics={() => {}}
                handleTransferRequest={() => {}}
                handleMergeRequest={() => {}}
                handleSplitRequest={() => {}}
                handleFormClusterRequest={() => {}}
                handleSnapshot={() => {}}
                handleSave={() => {}}
                handleCancel={() => {}}
            />

            <div style={{zIndex: 1, left: "2rem", top: "4.5rem", position: "absolute"}}>
                {renderBreadCrumbs()}
            </div>

            <div style={{zIndex: 1, right: "2rem", top: "4.5rem", position: "absolute"}}>
                <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                    <Button variant="success">Help</Button>
                </OverlayTrigger>
            </div>
        </Container>
    );
}
