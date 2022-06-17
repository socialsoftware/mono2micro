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
import {ClusterViewGraph, clusterViewHelp} from "./clusterView/ClusterViewGraph";
import {searchType, ViewSearchBar} from "./ViewSearchBar";
import {ModalProgressBar} from "../util/ModalProgressBar";
import {toast} from "react-toastify";
import {ViewSpeedDial} from "../util/ViewSpeedDial";

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
};

export const Views = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;
    let { codebaseName, strategyName, decompositionName } = useParams();
    const toastId = useRef(null);

    const [view, setView] = useState(views.CLUSTERS);
    const [reloadProperties, setReloadProperties] = useState({});
    const [now, setNow] = useState(0);
    const [openSearch, setOpenSearch] = useState(false);
    const [searchItems, setSearchItems] = useState(undefined);
    const [searchedItem, setSearchedItem] = useState(undefined);
    const [clusters, setClusters] = useState([]);
    const [functionalities, setFunctionalities] = useState([]);

    const [displayClusters, setDisplayClusters] = useState("block");
    const [displayFunctionalities, setDisplayFunctionalities] = useState("none");
    const [actions, setActions] = useState([]);

    // Loads decomposition's properties
    useEffect(() => {
        let newClusters, newFunctionalities, newTranslateEntity;
        const service = new RepositoryService();
        setNow(5);
        toastId.current = toast.loading("Loading properties...");

        // Translation file
        let first = service.getInputFile(codebaseName, SourceType.IDTOENTITIY).then(source => {
            const { updateEntityTranslationFile } = context;
            updateEntityTranslationFile(source.data);
            newTranslateEntity = source.data;
        }).catch(error => {
            console.error(error);
        });

        // Clusters
        let second = service.getDecomposition(
            codebaseName,
            strategyName,
            decompositionName
        ).then(response => {
            newClusters = Object.values(response.data.clusters);
            newFunctionalities = Object.values(response.data.functionalities);
            newClusters = newClusters.sort((a, b) => a.name - b.name);
            setNow(prev => prev + 25);
            toast.update(toastId.current, {render: "Loaded Clusters"});
            setClusters(newClusters);
            setFunctionalities(newFunctionalities);
        }).catch(error => {
            toast.update(toastId.current, {render: "An error occurred while fetching the decomposition", type: toast.TYPE.ERROR})
            console.error(error);
        });

        Promise.all([first, second]).then(() => {
            setupSearch(newClusters, newFunctionalities, newTranslateEntity);
        });
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

    function setupSearch(newClusters, newFunctionalities, newTranslateEntity) {
        let items = [];

        // Setup tags, some of them are added by default
        let tags = {
            available: {
                type: [searchType.CLUSTER, searchType.ENTITY, searchType.FUNCTIONALITY],
                entities: [],
                cluster: [],
                funcType: ["QUERY", "SAGA"],
                clusters: [],
            },
            selected: {
                type: [],
                entities: [],
                cluster: [],
                funcType: [],
                clusters: []
            }
        };

        newFunctionalities.forEach(functionality => {
            const item = {name: functionality.name, type: searchType.FUNCTIONALITY, funcType: functionality.type, clusters: Object.keys(functionality.entitiesPerCluster).length};
            items.push(item);
            if (!tags.available.clusters.includes(item.clusters))
                tags.available.clusters.push(item.clusters);
        });

        newClusters.forEach(cluster => {
            const item = {name: cluster.name, type: searchType.CLUSTER, id: cluster.id, entities: cluster.entities.length,
                cohesion: cluster.cohesion, coupling: cluster.coupling, complexity: cluster.complexity};
            items.push(item);
            if (!tags.available.entities.includes(item.entities))
                tags.available.entities.push(item.entities);
        });

        newClusters.forEach(cluster => {
            cluster.entities.forEach(entity => {
                const item = {name: newTranslateEntity[entity], type: searchType.ENTITY, id: entity, cluster: cluster.name, clusterId: cluster.id};
                items.push(item);
                if (!tags.available.cluster.includes(item.cluster))
                    tags.available.cluster.push(item.cluster);
            });
        });

        setSearchItems({items, tags});
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
                        toastId={toastId}
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