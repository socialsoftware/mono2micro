import React, {useContext, useEffect, useRef, useState} from 'react';
import { FunctionalityView, functionalityViewHelp } from './functionalityView/FunctionalityView';
import Popover from 'react-bootstrap/Popover';
import Container from 'react-bootstrap/Container';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {RepositoryService} from "../../../services/RepositoryService";
import AppContext from "../../AppContext";
import {useParams} from "react-router-dom";
import {clusterViewHelp} from "./clusterView/ClusterViewGraph";
import {searchType, ViewSearchBar} from "./ViewSearchBar";
import {ModalProgressBar} from "../../util/ModalProgressBar";
import {ViewSpeedDial} from "../../util/ViewSpeedDial";
import {ClusterViewGraph} from "./clusterView/ClusterViewGraph";

export const views = {
    CLUSTERS: 'Clusters View',
    FUNCTIONALITY: 'Functionality View',
};

export const types = {
    NONE: 0,
    CLUSTER: 1,
    FUNCTIONALITY: 2,
    ENTITY: 3,
    EDGE: 4,
    MULTIPLE: 5, // When selecting multiple nodes
    BETWEEN_CLUSTERS: 6,
    BETWEEN_ENTITIES: 7,
    BETWEEN_CLUSTER_ENTITY: 8,
};

export const Views = () => {
    const context = useContext(AppContext);
    const { updateEntityTranslationFile } = context;
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [view, setView] = useState(views.CLUSTERS);
    const [now, setNow] = useState(0);
    const [openSearch, setOpenSearch] = useState(false);
    const [searchedItem, setSearchedItem] = useState(undefined);
    const [outdated, setOutdated] = useState(true);

    const [displayClusters, setDisplayClusters] = useState("block");
    const [displayFunctionalities, setDisplayFunctionalities] = useState("none");
    const [actions, setActions] = useState([]);

    useEffect(() => {
        setNow(10);

        // Translation file
        const service = new RepositoryService();
        service.getIdToEntity(codebaseName).then(response => {
            setNow(prev => prev + 10);
            updateEntityTranslationFile(response.data);
        }).catch(error => {
            console.error(error);
        });
    }, []);

    useEffect(() => { // Selects the correct view
        if (searchedItem !== undefined && searchedItem.type === searchType.FUNCTIONALITY) {
            setDisplayClusters("none");
            setDisplayFunctionalities("block");
            setView(views.FUNCTIONALITY);
        }
        else if (searchedItem !== undefined && (searchedItem.type === searchType.CLUSTER || searchedItem.type === searchType.ENTITY)) {
            setDisplayClusters("block");
            setDisplayFunctionalities("none");
            setView(views.CLUSTERS);
        }
    }, [searchedItem]);

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
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}`}>
                    {strategyName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {decompositionName}
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    const helpPopover = (
        <Popover id="helpPopover" title={view}>
            {getHelpText(view)}
        </Popover>
    );

    function changeToFunctionalities() {
        setDisplayClusters("none");
        setDisplayFunctionalities("block");
        setView(views.FUNCTIONALITY);
    }

    function changeToClusters() {
        setDisplayClusters("block");
        setDisplayFunctionalities("none");
        setView(views.CLUSTERS);
    }

    return (
        <Container fluid>
            <ModalProgressBar
                now={now}
            />

            <ViewSearchBar
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

            <div style={{display: displayClusters}}>
                <ClusterViewGraph
                    setNow={setNow}
                    outdated={outdated}
                    setOutdated={setOutdated}
                    searchedItem={searchedItem}
                    setSearchedItem={setSearchedItem}
                    changeToFunctionalities={changeToFunctionalities}
                    setOpenSearch={setOpenSearch}
                    setActions={setActions}
                    view={view}
                />
            </div>
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