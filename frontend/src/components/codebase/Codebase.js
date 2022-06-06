import React, {useEffect, useState} from 'react';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Button from 'react-bootstrap/Button';
import {useParams} from "react-router-dom";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Popover from "react-bootstrap/Popover";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";
import {CollectorType} from "../../models/collectors/Collector";
import {CollectorFactory} from "../../models/collectors/CollectorFactory";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import {AccessesCollectorForm} from "./forms/AccessesCollectorForm";

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
    const [collectorToDelete, setCollectorToDelete] = useState(undefined);
    const [selectedCollector, setSelectedCollector] = useState(undefined);
    const [collectors, setCollectors] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");

    //Executed on mount
    useEffect(() => {
        loadCollectors()
    }, []);

    function loadCollectors() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName, ['collectors'])
            .then(response => {
                if (response.data !== undefined)
                    setCollectors(response.data.collectors.map(collector =>
                        CollectorFactory.getCollector({type:collector, codebaseName}))
                    );
            });
    }

    function handleCollectorSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.addCollector(codebaseName, selectedCollector.type, selectedCollector.addedSources)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCollectors();
                    setSelectedCollector(undefined);
                    setIsUploaded("");
                } else {
                    setIsUploaded("Upload failed.");
                }
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
    }

    const getHelpText = (
        <Popover id="helpPopover">
            <div> Select a Collector type and<br/>add the required files,<br/>then proceed to the Strategies or<br/>customize the collector if possible</div>
        </Popover>
    );

    function handleSelectedCollector(collectorType) {
        setSelectedCollector(CollectorFactory.getCollector({type: collectorType, codebaseName}));
    }

    function handleCollectorDelete(collectorToDelete) {
        setCollectorToDelete(collectorToDelete);
        setShowPopup(true);
    }

    function confirmCollectorDelete() {
        setShowPopup(false);

        const service = new RepositoryService();
        service.deleteCollector(
            collectorToDelete.codebaseName,
            collectorToDelete.type,
            collectorToDelete.sources,
            collectorToDelete.possibleStrategies
        ).then(() => {
            loadCollectors()
        });
    }

    function renderCollectors() {
        return (
            <Form onSubmit={handleCollectorSubmit}>
                <Form.Group as={Row} controlId="selectSource" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={selectedCollector === undefined? "Add Collector" : selectedCollector.type}>
                            {Object.values(CollectorType).filter(collectorType => !collectors.find(collector => collector.type === collectorType))
                                .map(collectorType =>
                                    <Dropdown.Item
                                        key={collectorType}
                                        onClick={() => handleSelectedCollector(collectorType)}
                                    >
                                        {collectorType}
                                    </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                {selectedCollector !== undefined &&                                     // Show sources request form
                    <AccessesCollectorForm
                        collector={selectedCollector}
                        setCollector={setSelectedCollector}
                    />
                }

                {selectedCollector !== undefined &&                                     // Submit button
                    <Form.Group as={Row} className="align-items-center mb-4">
                        <Col sm={{ offset: 2 }}>
                            <Button type="submit" disabled={!selectedCollector.canSubmit()}>Submit</Button>
                            <Form.Text className="ms-2">
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                }

                {collectors.length !== 0 && <h4 className="mb-3" style={{ color: "#666666" }}> Collectors </h4>}

                <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                    {collectors.map(collector => collector.printCard(handleCollectorDelete))}
                </div>
            </Form>
        );
    }

    function renderDeletePopup() {
        const closePopup = function () {setShowPopup(false); setCollectorToDelete(undefined)};

        return <Modal
                show={showPopup}
                onHide={() => closePopup()}
                backdrop="static"
            >
                <ModalTitle>&ensp;Collector Deletion</ModalTitle>
                <ModalBody>
                    Strategies created from this collector will be deleted.<br/>Are you sure you want to delete this Collector?
                </ModalBody>
                <ModalFooter>
                    <Button variant="secondary" onClick={() => closePopup()}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={() => confirmCollectorDelete()}>Delete</Button>
                </ModalFooter>
            </Modal>;
    }

    return (
        <div style={{ paddingLeft: "2rem" }}>
            { renderDeletePopup() }

            { renderBreadCrumbs(codebaseName) }

            <Row className="mt-4">
                <Col>
                    <h3 style={{color: "#666666"}}>{codebaseName}</h3>
                </Col>
                <Col className="me-5">
                    <OverlayTrigger trigger="click" placement="left" overlay={getHelpText}>
                        <Button className="float-end" variant="success">Help</Button>
                    </OverlayTrigger>
                </Col>
            </Row>

            <Button
                href={`/codebases/${codebaseName}/strategies`}
                className="mt-2 mb-3"
                disabled={collectors.length === 0}
                variant={"success"}
            >
                Go to Strategies
            </Button>

            { renderCollectors() }
        </div>
    );
}