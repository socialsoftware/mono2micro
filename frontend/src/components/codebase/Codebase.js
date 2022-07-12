import React, {useEffect, useState} from 'react';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Button from 'react-bootstrap/Button';
import {useParams} from "react-router-dom";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import Card from "react-bootstrap/Card";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Popover from "react-bootstrap/Popover";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import {AccessesSciPyForm} from "./forms/AccessesSciPyForm";
import {ArrowForward} from "@mui/icons-material";
import {StrategyDescription, StrategyType} from "../../models/strategies/Strategy";
import {StrategyFactory} from "../../models/strategies/StrategyFactory";

function renderBreadCrumbs(codebaseName) {
    return (
        <Breadcrumb>
            <Breadcrumb.Item href="/">
                Home
            </Breadcrumb.Item>
            <Breadcrumb.Item href="/codebases">
                Codebases
            </Breadcrumb.Item>
            <Breadcrumb.Item active>
                {codebaseName}
            </Breadcrumb.Item>
        </Breadcrumb>
    );
}

export function Codebase() {
    let { codebaseName } = useParams();
    const [showPopup, setShowPopup] = useState(false);
    const [sourceToDelete, setSourceToDelete] = useState(undefined);
    const [selectedStrategy, setSelectedStrategy] = useState(undefined);
    const [sources, setSources] = useState([]);
    const [addedSources, setAddedSources] = useState({});
    const [isUploaded, setIsUploaded] = useState("");
    const [canSubmit, setCanSubmit] = useState(false);

    //Executed on mount
    useEffect(() => {
        loadCodebase();
    }, []);

    function loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName).then(response => {
            setSources(response.sources);
        });
    }

    function handleSourcesSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.addSources(codebaseName, addedSources)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCodebase();
                    setIsUploaded("");
                } else setIsUploaded("Upload failed.");

                setSelectedStrategy(undefined);
                setAddedSources({});
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    setIsUploaded("Upload failed. Collector already submitted.");
                }
                else if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                    setIsUploaded("Upload failed. Invalid datafile path.");
                }
                else {
                    setIsUploaded("Upload failed.");
                }
            });
        setCanSubmit(false);
    }

    const getHelpText = (
        <Popover id="helpPopover">
            <div> Select a Collector type and<br/>add the required files,<br/>then proceed to the Strategies or<br/>customize the collector if possible</div>
        </Popover>
    );

    function handleSelectedStrategy(strategyType) {
        setSelectedStrategy(StrategyFactory.getStrategy({type: strategyType}));
    }

    function handleSourceDelete(source) {
        setSourceToDelete(source);
        setShowPopup(true);
    }

    function confirmSourceToDelete() {
        setShowPopup(false);

        const service = new RepositoryService();
        service.deleteSource(sourceToDelete.name).then(() => {
            loadCodebase();
        });
    }

    function renderSources() {
        return (
            <Form onSubmit={handleSourcesSubmit}>
                <Form.Group as={Row} controlId="selectSource" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={selectedStrategy === undefined? "Choose Strategy" : StrategyDescription[selectedStrategy.type]}>
                            {Object.values(StrategyType).map(strategyType =>
                                    <Dropdown.Item
                                        key={strategyType}
                                        onClick={() => handleSelectedStrategy(strategyType)}
                                    >
                                        {StrategyDescription[strategyType]}
                                    </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                {selectedStrategy !== undefined &&
                    (selectedStrategy.type === StrategyType.RECOMMEND_ACCESSES_SCIPY ||
                    selectedStrategy.type === StrategyType.ACCESSES_SCIPY) &&   // Show sources request form
                    <AccessesSciPyForm
                        strategy={selectedStrategy}
                        setAddedSources={setAddedSources}
                        sources={sources}
                        setCanSubmit={setCanSubmit}
                    />
                }

                {selectedStrategy !== undefined &&
                    <Form.Group as={Row} className="align-items-center mb-4">
                        <Col sm={{offset: 2}}>
                            <Button type="submit" disabled={!canSubmit}>Submit</Button>
                            <Form.Text className="ms-2">
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                }

                <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                    {sources.map(source => source.printCard(handleSourceDelete))}
                </div>
            </Form>
        );
    }

    function renderDeletePopup() {
        const closePopup = function () {setShowPopup(false); setSourceToDelete(undefined)};

        return <Modal
                show={showPopup}
                onHide={() => closePopup()}
                backdrop="static"
            >
                <ModalTitle>&ensp;Source Deletion</ModalTitle>
                <ModalBody>
                    Strategies created from this source will be deleted.<br/>Are you sure you want to delete this Source?
                </ModalBody>
                <ModalFooter>
                    <Button variant="secondary" onClick={() => closePopup()}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={() => confirmSourceToDelete()}>Delete</Button>
                </ModalFooter>
            </Modal>;
    }

    return (
        <div style={{ paddingLeft: "2rem" }}>
            { renderDeletePopup() }

            <Row className="mt-4">
                <Col>
                    { renderBreadCrumbs(codebaseName) }
                </Col>
                <Col className="me-5">
                    <OverlayTrigger trigger="click" placement="left" overlay={getHelpText}>
                        <Button className="float-end" variant="success">Help</Button>
                    </OverlayTrigger>
                </Col>
            </Row>

            <Row className="justify-content-center">
                <Col sm={2}>
                    <Card
                        className={"text-center"}
                    >
                        <Card.Header>{codebaseName}</Card.Header>
                        <Card.Body>
                            <Button
                                href={`/codebases/${codebaseName}/strategies`}
                                className="mt-2 mb-3"
                                disabled={sources.length === 0}
                                variant={"success"}
                            >
                                Go to Strategies <ArrowForward/>
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            { renderSources() }
        </div>
    );
}