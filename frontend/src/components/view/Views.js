import React, {useContext, useEffect, useRef, useState} from 'react';
import { FunctionalityView, functionalityViewHelp } from './FunctionalityView';
import Popover from 'react-bootstrap/Popover';
import Container from 'react-bootstrap/Container';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {RepositoryService} from "../../services/RepositoryService";
import AppContext from "./../AppContext";
import {useParams} from "react-router-dom";
import {SourceType} from "../../models/sources/Source";
import {clusterViewHelp} from "./clusterView/ClusterViewGraph";
import {searchType, ViewSearchBar} from "./ViewSearchBar";
import {ModalProgressBar} from "../util/ModalProgressBar";
import {ViewSpeedDial} from "../util/ViewSpeedDial";
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
    const { translateEntity, updateEntityTranslationFile } = context;
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [view, setView] = useState(views.CLUSTERS);
    const [reloadProperties, setReloadProperties] = useState(undefined);
    const [now, setNow] = useState(0);
    const [openSearch, setOpenSearch] = useState(false);
    const [searchItems, setSearchItems] = useState(undefined);
    const [searchedItem, setSearchedItem] = useState(undefined);
    const [clusters, setClusters] = useState([]);
    const [functionalities, setFunctionalities] = useState([]);

    const [displayClusters, setDisplayClusters] = useState("block");
    const [displayFunctionalities, setDisplayFunctionalities] = useState("none");
    const [actions, setActions] = useState([]);

    useEffect(() => {
        // Translation file
        const service = new RepositoryService();
        service.getInputFile(codebaseName, SourceType.IDTOENTITIY).then(source => {
            updateEntityTranslationFile(source.data);
        }).catch(error => {
            console.error(error);
        });
        setReloadProperties({});
    }, []);

    // Loads decomposition's properties
    useEffect(() => {
        let newClusters, newFunctionalities;
        const service = new RepositoryService();

        if (reloadProperties === undefined)
            return;
        setNow(prev => prev + 10);

        // Clusters
        service.getDecomposition(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            newClusters = Object.values(response.data.clusters);
            newFunctionalities = Object.values(response.data.functionalities);
            newClusters = newClusters.sort((a, b) => a.name - b.name);
            if (clusters.length === 0)
                setNow(prev => prev + 20);
            else setNow(0);
            setClusters(newClusters);
            setFunctionalities(newFunctionalities);
            setupSearch(newClusters, newFunctionalities);
        }).catch(error => console.error(error));
    },[reloadProperties]);

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

    function setupSearch(newClusters, newFunctionalities) {
        let items = [], key = 0;
        let translations = []; //fixes bug where translateEntity is not yet ready to translate entities
        updateEntityTranslationFile(prev => {translations = prev; return prev});

        newFunctionalities.forEach(functionality =>
            items.push({
                keyField: key++,
                name: functionality.name,
                type: searchType.FUNCTIONALITY,
                id: "",
                entities: "",
                funcType: functionality.type,
                clusters: Object.keys(functionality.entitiesPerCluster).length,
                cluster: ""
            })
        );

        newClusters.forEach(cluster => {
            items.push({
                keyField: key++,
                name: cluster.name,
                type: searchType.CLUSTER,
                id: cluster.id,
                entities: cluster.entities.length,
                funcType: "",
                clusters: "",
                cluster: ""
            });
        });

        newClusters.forEach(cluster => {
            cluster.entities.forEach(entity =>
                items.push({
                    keyField: key++,
                    name: translations[entity] ?? entity,
                    type: searchType.ENTITY,
                    id: entity,
                    entities: "",
                    funcType: "",
                    clusters: "",
                    cluster: cluster.name,
                    clusterId: cluster.id
                })
            );
        });

        setSearchItems(items);
    }

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
                <Breadcrumb.Item href={`/codebases/${codebaseName}/strategies`}>
                    Strategies
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/strategies/${strategyName}`}>
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
                searchItems={searchItems}
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
                {clusters.length !== 0 &&
                    <ClusterViewGraph
                        setNow={setNow}
                        translateEntity={translateEntity}
                        clusters={clusters}
                        setReloadProperties={setReloadProperties}
                        searchedItem={searchedItem}
                        setSearchedItem={setSearchedItem}
                        changeToFunctionalities={changeToFunctionalities}
                        setOpenSearch={setOpenSearch}
                        setActions={setActions}
                        view={view}
                    />
                }
            </div>
            <div style={{display: displayFunctionalities}}>
                {functionalities.length !== 0 &&
                    <FunctionalityView
                        searchedItem={searchedItem}
                        setSearchedItem={setSearchedItem}
                        functionalities={functionalities}
                        setFunctionalities={setFunctionalities}
                        clusters={clusters}
                        changeToClusters={changeToClusters}
                        setOpenSearch={setOpenSearch}
                        setActions={setActions}
                        view={view}
                    />
                }
            </div>
        </Container>
    );
}