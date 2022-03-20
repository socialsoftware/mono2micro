import React, {useContext, useEffect, useState} from 'react';
import { ClusterView, clusterViewHelp } from './ClusterView';
import { TransactionView, transactionViewHelp } from './TransactionView';
import { EntityView, entityViewHelp } from './EntityView';
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

export const views = {
    CLUSTERS: 'Clusters View',
    TRANSACTION: 'Transaction View',
    ENTITY: 'Entity View',
    REFACTOR: 'Refactorization Tool',
};

export const types = {
    CLUSTER: 0,
    CONTROLLER: 1,
    ENTITY: 2
};

export const Views = () => {
    const context = useContext(AppContext);
    let { codebaseName, dendrogramName, decompositionName } = useParams();

    const [view, setView] = useState(views.CLUSTERS);

    useEffect(() => {
        loadTranslation();
    },[]);

    function loadTranslation() {
        const service = new RepositoryService();
        service.getTranslation(codebaseName).then(response => {
            const { updateEntityTranslationFile } = context;
            updateEntityTranslationFile(response.data);
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
            case views.ENTITY:
                return entityViewHelp;
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
                <Breadcrumb.Item href={`/codebases/${codebaseName}/dendrograms`}>
                    Dendrograms
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/dendrograms/${dendrogramName}`}>
                    {dendrogramName}
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
            {renderBreadCrumbs()}
            <Row className="mb-2">
                <Col>
                    <h4 style={{color: "#666666"}}>{decompositionName}</h4>
                </Col>
                <Col className="me-5">
                    <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                        <Button className="float-end" variant="success">Help</Button>
                    </OverlayTrigger>
                </Col>
            </Row>
            <Row className="mb-2">
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
                            onClick={() => handleSelectView(views.ENTITY)}
                        >
                            {views.ENTITY}
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
                            <ClusterView
                                codebaseName={codebaseName}
                                dendrogramName={dendrogramName}
                                decompositionName={decompositionName}
                            />
                    }
                    {
                        view === views.TRANSACTION &&
                            <TransactionView
                                codebaseName={codebaseName}
                                dendrogramName={dendrogramName}
                                decompositionName={decompositionName}
                            />
                    }
                    {
                        view === views.ENTITY &&
                            <EntityView
                                codebaseName={codebaseName}
                                dendrogramName={dendrogramName}
                                decompositionName={decompositionName}
                            />
                    }
                    {
                        view === views.REFACTOR &&
                            <FunctionalityRefactorToolMenu
                                codebaseName = {codebaseName}
                                dendrogramName = {dendrogramName}
                                decompositionName = {decompositionName}
                            />
                    }
                </Col>
            </Row>
        </Container>
    );
}