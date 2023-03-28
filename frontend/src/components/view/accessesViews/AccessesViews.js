import {useParams} from "react-router-dom";
import React, {useContext, useEffect, useState} from "react";
import {APIService} from "../../../services/APIService";
import {CancelPresentation, Redo, Search, TableView, Timeline, Undo} from "@mui/icons-material";
import Breadcrumb from "react-bootstrap/Breadcrumb";
import Popover from "react-bootstrap/Popover";
import Container from "react-bootstrap/Container";
import {ModalProgressBar} from "../utils/ModalProgressBar";
import {searchType, ViewSearchBar} from "../utils/ViewSearchBar";
import {ViewSpeedDial} from "../utils/ViewSpeedDial";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Button from "react-bootstrap/Button";
import {ClusterView} from "../utils/ClusterView";
import {FunctionalityView, functionalityViewHelp} from "./functionalityView/FunctionalityView";
import AppContext from "../../AppContext";
import {AccessesViewModal} from "./accessView/AccessesViewModal";
import {AccessesViewMetricTable} from "./accessView/AccessesViewMetricTable";
import {StrategyType} from "../../../models/strategy/StrategyTypes";
import {RepresentationType} from "../../../models/representation/Representation";

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

export const views = {
    CLUSTERS: 'Clusters View',
    FUNCTIONALITY: 'Functionality View',
};

export const AccessesViews = () => {
    const context = useContext(AppContext);
    const { updateEntityTranslationFile } = context;
    let { codebaseName, strategyName, similarityName, decompositionName } = useParams();

    const [view, setView] = useState(views.CLUSTERS);
    const [displayAccesses, setDisplayAccesses] = useState("block");
    const [displayFunctionalities, setDisplayFunctionalities] = useState("none");
    const [showTable, setShowTable] = useState(false);
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
    const [clustersFunctionalities, setClustersFunctionalities] = useState({});

    useEffect(() => {
        setNow(10);

        const service = new APIService();
        service.getClustersAndClustersFunctionalities(decompositionName).then(response => {
            setNow(prev => prev + 20);
            setClusters(Object.values(response.data.clusters).sort((a, b) => a.name - b.name));
            setClustersFunctionalities(response.data.clustersFunctionalities);

            service.getEdgeWeights(decompositionName, RepresentationType.ACCESSES_TYPE).then(response => {
                setNow(n => n + 30);
                setEdgeWeights(response.data);
            });
        });
        service.getIdToEntity(codebaseName).then(response => {
            setNow(prev => prev + 10);
            updateEntityTranslationFile(response.data);
        });

    }, []);

    function loadClustersAndClustersFunctionalities() {
        const service = new APIService();
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
        }
    }, [outdated]);

    useEffect(() => {
        if ((outdated || Object.keys(clustersFunctionalities).length === 0) && (showModal || showTable))
            loadClustersAndClustersFunctionalities();
    }, [showModal, showTable]);


    const speedDialActions = [
        { icon: <Search/>, name: 'Search Element', handler: () => setOpenSearch(true) },
        { icon: <Undo/>, name: 'Undo', handler: handleUndo },
        { icon: <Redo/>, name: 'Redo', handler: handleRedo },
        { icon: <CancelPresentation/>, name: 'Go to Top', handler: handleScrollTop },
        { icon: <TableView/>, name: 'Go to Metrics' , handler: handleScrollBottom },
        { icon: <Timeline/>, name: 'Go to Functionalities', handler: changeToFunctionalities },
    ];

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
        const service = new APIService();
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

    function handleScrollTop() { changeSpeedDial(false); setShowTable(false); }

    function handleScrollBottom() { changeSpeedDial(true); setShowTable(true); }

    function changeToClusters() {
        setDisplayAccesses("block");
        setDisplayFunctionalities("none");
        setView(views.CLUSTERS);
    }

    function changeToFunctionalities() {
        setShowTable(false);
        setDisplayAccesses("none");
        setDisplayFunctionalities("block");
        setView(views.FUNCTIONALITY);
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
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}/${similarityName}/decomposition`}>
                    {similarityName}
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
        });
    }

    useEffect(() => { // update speed dial after operation
        changeSpeedDial(showTable);
    }, [clusters]);

    const helpPopover = (
        <Popover id="helpPopover" title={view}>
            {getHelpText(view)}
        </Popover>
    );

    function getHelpText(view) {
        switch(view) {
            case views.CLUSTERS:
                return clusterViewHelp;
            case views.FUNCTIONALITY:
                return functionalityViewHelp;
            default:
                return null;
        }
    }

    useEffect(() => { // Selects the correct view
        if (searchedItem !== undefined && searchedItem.type === searchType.FUNCTIONALITY) {
            setShowTable(false);
            setDisplayAccesses("none");
            setDisplayFunctionalities("block");
            setView(views.FUNCTIONALITY);
        }
        else if (searchedItem !== undefined && (searchedItem.type === searchType.CLUSTER || searchedItem.type === searchType.ENTITY)) {
            setDisplayAccesses("block");
            setDisplayFunctionalities("none");
            setView(views.CLUSTERS);
        }
    }, [searchedItem]);

    return (
        <Container fluid>
            <ModalProgressBar
                now={now}
            />

            <ViewSearchBar
                viewType={StrategyType.ACCESSES_STRATEGY}
                dataFields={['name', 'type', 'funcType', 'cluster', 'entities']}
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

            <div style={{display: displayAccesses}}>
                <ClusterView
                    property={"functionalities"}
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
            {showTable &&
                <>
                    <div id="metricTable"></div> {/*This is used as an anchor*/}
                    <AccessesViewMetricTable
                        clusters={clusters}
                        clustersFunctionalities={clustersFunctionalities}
                        outdated={outdated}
                    />
                </>
            }
            <AccessesViewModal
                clusters={clusters}
                clustersFunctionalities={clustersFunctionalities}
                edgeWeights={edgeWeights}
                outdated={outdated}
                showModal={showModal}
                setShowModal={setShowModal}
                clickedComponent={clickedComponent}
                setClickedComponent={setClickedComponent}
            />

            <div style={{display: displayFunctionalities}}>
                <FunctionalityView
                    searchedItem={searchedItem}
                    setSearchedItem={setSearchedItem}
                    outdated={outdated}
                    setOutdated={setOutdated}
                    changeToClusters={changeToClusters}
                    setOpenSearch={setOpenSearch}
                    setActions={setActions}
                    view={view}
                />
            </div>
        </Container>
    );
}
