import React, {useContext, useEffect, useState} from 'react';
import { TransactionView, transactionViewHelp } from './TransactionView';
import {FunctionalityRefactorToolMenu, refactorToolHelp} from './FunctionalityRefactorToolMenu';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Popover from 'react-bootstrap/Popover';
import Container from 'react-bootstrap/Container';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import {RepositoryService} from "../../services/RepositoryService";
import AppContext from "./../AppContext";
import {useParams} from "react-router-dom";
import {SourceType} from "../../models/sources/Source";
import {ClusterViewGraph, clusterViewHelp} from "./clusterView/ClusterViewGraph";

export const views = {
    CLUSTERS: 'Clusters View',
    TRANSACTION: 'Transaction View',
    REFACTOR: 'Refactorization Tool',
};

export const types = {
    NONE: 0,
    CLUSTER: 1,
    FUNCTIONALITY: 2,
    ENTITY: 3,
    EDGE: 4,
};

export const Views = () => {
    const context = useContext(AppContext);
    let { codebaseName, strategyName, decompositionName } = useParams();

    const [view, setView] = useState(views.CLUSTERS);

    useEffect(() => {
        loadTranslation();
    },[]);

    function loadTranslation() {
        const service = new RepositoryService();
        service.getInputFile(codebaseName, SourceType.IDTOENTITIY).then(source => {
            const { updateEntityTranslationFile } = context;
            updateEntityTranslationFile(source.data);
        }).catch(error => {
            console.log(error);
        });
    }

    function handleSelectView(value) {
        setView(value);
    }

    function getHelpText(view) {
        switch(view) {
            case views.CLUSTERS:
                return clusterViewHelp;
            case views.TRANSACTION:
                return transactionViewHelp;
            case views.REFACTOR:
                return refactorToolHelp;
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

    return (
        <Container fluid>
            <div style={{ zIndex: 1, left: "2rem", position: "absolute" }}>
                {renderBreadCrumbs()}
            </div>
            <div style={{ zIndex: 1, right: "2rem", position: "absolute" }}>
                <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                    <Button variant="success">Help</Button>
                </OverlayTrigger>
            </div>
            <Row style={{ zIndex: 3, left: "1.3rem", marginTop: "3rem", position: "absolute" }}>
                <Col sm="auto">
                    <DropdownButton title={view}>
                        <Dropdown.Item
                            onClick={() => handleSelectView(views.CLUSTERS)}
                        >
                            {views.CLUSTERS}
                        </Dropdown.Item>
                        <Dropdown.Item
                            onClick={() => handleSelectView(views.TRANSACTION)}
                        >
                            {views.TRANSACTION}
                        </Dropdown.Item>
                        <Dropdown.Item
                            onClick={() => handleSelectView(views.REFACTOR)}
                        >
                            {views.REFACTOR}
                        </Dropdown.Item>
                    </DropdownButton>
                </Col>
            </Row>
            <Row>
                <Col>
                    {
                        view === views.CLUSTERS &&
                            <ClusterViewGraph/>
                    }
                    {
                        view === views.TRANSACTION &&
                            <TransactionView
                                codebaseName={codebaseName}
                                strategyName={strategyName}
                                decompositionName={decompositionName}
                            />
                    }
                    {
                        view === views.REFACTOR &&
                            <FunctionalityRefactorToolMenu
                                codebaseName = {codebaseName}
                                strategyName = {strategyName}
                                decompositionName = {decompositionName}
                            />
                    }
                </Col>
            </Row>
        </Container>
    );
}