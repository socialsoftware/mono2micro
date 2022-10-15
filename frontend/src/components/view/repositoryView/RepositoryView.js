import React, {useEffect, useState} from 'react';
import Container from 'react-bootstrap/Container';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {APIService} from "../../../services/APIService";
import {useParams} from "react-router-dom";
import {ModalProgressBar} from "../utils/ModalProgressBar";
import {ViewSpeedDial} from "../utils/ViewSpeedDial";
import {ClusterView} from "../utils/ClusterView";
import {Redo, Search, Undo} from "@mui/icons-material";
import {RepositoryViewModal} from "./RepositoryViewModal";
import Popover from "react-bootstrap/Popover";
import {ViewSearchBar} from "../utils/ViewSearchBar";

export const repositoryViewHelp = (<div>
    Double click a node to see its properties.<br />
    Double click an edge to see authors.<br />
    Right click in a node to begin a modification.<br />
    Other operations are also available when<br />
    right-clicking the background or the edges.<br />
    Search specific entity/cluster<br />
    in the speed dial.<br />
    Also undo/redo operations on the speed dial<br />
    To select multiple nodes, leave the mouse
    button pressed or use "Ctrl+click" to add nodes.
    Then right click to see available operations.<br />
</div>);

const REPOSITORY_DECOMPOSITION = "REPOSITORY_DECOMPOSITION";
export {REPOSITORY_DECOMPOSITION};

export const RepositoryView = () => {
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [displayRepository, setDisplayRepository] = useState("none");
    const [showModal, setShowModal] = useState(false);
    const [clickedComponent, setClickedComponent] = useState(undefined);
    const [actions, setActions] = useState([]);
    const [reloadPositions, setReloadPositions] = useState(undefined);

    const [now, setNow] = useState(0);
    const [openSearch, setOpenSearch] = useState(false);
    const [searchedItem, setSearchedItem] = useState(undefined);
    const [outdated, setOutdated] = useState(true);
    const [edgeWeights, setEdgeWeights] = useState(undefined);

    const [clusters, setClusters] = useState(undefined);
    const [authors, setAuthors] = useState(undefined);
    const [commitsInCommon, setCommitsInCommon] = useState(undefined);
    const [totalCommits, setTotalCommits] = useState(undefined);

    useEffect(() => {
        setNow(10);

        const service = new APIService();
        const response1 = service.getDecomposition(decompositionName).then(response => {
            setNow(n => n + 30);
            setClusters(Object.values(response.clusters));
            setAuthors(response.authors);
            setCommitsInCommon(response.commitsInCommon);
            setTotalCommits(response.totalCommits);
        });
        const response2 = service.getEdgeWeights(decompositionName, REPOSITORY_DECOMPOSITION).then(response => { setNow(n => n + 30); setEdgeWeights(response.data);});
        Promise.all([response1, response2]).then(() => {
            setDisplayRepository("block");
            changeSpeedDial();
        });
    }, []);

    const speedDialActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <Undo/>, name: 'Undo', handler: handleUndo },
        { icon: <Redo/>, name: 'Redo', handler: handleRedo },
    ];

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

    function handleUndo() {
        const service = new APIService();
        service.undoOperation(decompositionName).then(() => {
            service.getClusters(decompositionName).then(response => {
                setClusters(Object.values(response.data));
                setReloadPositions({});
            });
            setOutdated(true);
            changeSpeedDial();
        });
    }

    function handleRedo() {
        const service = new APIService();
        service.redoOperation(decompositionName).then(() => {
            service.getClusters(decompositionName).then(response => {
                setClusters(Object.values(response.data));
                setReloadPositions({});
            });
            setOutdated(true);
            changeSpeedDial();
        });
    }

    useEffect(() => { // update speed dial after operation
        if (outdated)
            changeSpeedDial();
    }, [outdated]);

    function changeSpeedDial() {
        setActions(speedDialActions);
        const service = new APIService();
        service.canUndoRedo(decompositionName).then(response => {
            setActions(actions => [...actions.filter(action => action.name !== "Undo" && action.name !== "Redo")]);
            if (response.data["undo"])
                setActions(actions => [...actions, {icon: <Undo/>, name: 'Undo', handler: handleUndo}]);
            if (response.data["redo"])
                setActions(actions => [...actions, {icon: <Redo/>, name: 'Redo', handler: handleRedo}]);
        });

        setActions(actions => [...actions.filter(action => action.name !== "Go to Top" && action.name !== "Go to Metrics")]);
    }

    const helpPopover = (
        <Popover id="helpPopover" title="Help">
            {repositoryViewHelp}
        </Popover>
    );

    return (
        <Container fluid>
            <ModalProgressBar
                now={now}
            />

            <ViewSearchBar
                viewType={REPOSITORY_DECOMPOSITION}
                dataFields={['name', 'type', 'cluster', 'entities']}
                openSearch={openSearch}
                setOpenSearch={setOpenSearch}
                setSearchedItem={setSearchedItem}
            />

            <ViewSpeedDial actions={actions}/>

            <div style={{ zIndex: 1, left: "2rem", top: "4.5rem", position: "absolute" }}>
                {renderBreadCrumbs()}
            </div>

            <div style={{ zIndex: 1, right: "2rem", top: "4.5rem", position: "absolute" }}>
                <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                    <Button variant="success">Help</Button>
                </OverlayTrigger>
            </div>

            <RepositoryViewModal
                authors={authors}
                clusters={clusters}
                commitsInCommon={commitsInCommon}
                totalCommits={totalCommits}
                edgeWeights={edgeWeights}
                showModal={showModal}
                setShowModal={setShowModal}
                clickedComponent={clickedComponent}
                setClickedComponent={setClickedComponent}
            />
            <div style={{display: displayRepository}}>
                <ClusterView
                    property={"commits"}
                    reloadPositions={reloadPositions}
                    setReloadPositions={setReloadPositions}
                    clickedComponent={clickedComponent}
                    setClickedComponent={setClickedComponent}
                    clusters={clusters}
                    setClusters={setClusters}
                    edgeWeights={edgeWeights}
                    setNow={setNow}
                    setOutdated={setOutdated}
                    searchedItem={searchedItem}
                    setSearchedItem={setSearchedItem}
                    setShowModal={setShowModal}
                />
            </div>
        </Container>
    );
}
