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
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import {AccessesSciPyForm} from "./forms/AccessesSciPyForm";
import {toast} from "react-toastify";
import {StrategyDescription, StrategyType} from "../../models/strategy/Strategy";
import {StrategyFactory} from "../../models/strategy/StrategyFactory";

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

export function Strategies() {
    let { codebaseName } = useParams();
    const [showPopup, setShowPopup] = useState(false);
    const [sourceToDelete, setSourceToDelete] = useState(undefined);
    const [selectedStrategy, setSelectedStrategy] = useState(undefined);
    const [strategies, setStrategies] = useState([]);
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
            loadStrategies();
            setSources(response.sources);
        });
    }

    function loadStrategies() {
        const service = new RepositoryService();
        service.getCodebaseStrategies(codebaseName).then(response => {
            setStrategies(response);
        });
    }

    function handleSourcesSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createStrategy(codebaseName, selectedStrategy.type, addedSources)
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
                    setIsUploaded("Upload failed. Strategy already submitted.");
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

    function handleDeleteSource(source) {
        setSourceToDelete(source);
        setShowPopup(true);
    }

    function confirmSourceToDelete() {
        setShowPopup(false);

        let toastId = toast.loading("Deleting source...", {type: toast.TYPE.INFO});
        const service = new RepositoryService();
        service.deleteSource(sourceToDelete.name).then(() => {
            loadCodebase();
            toast.dismiss(toastId);
        });
    }

    function handleDeleteStrategy(strategy) {
        let toastId = toast.loading("Deleting strategy...", {type: toast.TYPE.INFO});
        const service = new RepositoryService();
        service.deleteStrategy(strategy.name).then(() => {
            loadStrategies();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategy deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + strategy.name + ".", isLoading: false});
        });
    }

    function renderSources() {
        return (
            <Form onSubmit={handleSourcesSubmit}>
                <Form.Group as={Row} controlId="selectStrategy" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={selectedStrategy === undefined? "Choose Strategy" : StrategyDescription[selectedStrategy.type]}>
                            {Object.values(StrategyType).filter(strategyType => !strategies || !strategies.find(strategy => strategy.type === strategyType))
                                .map(strategyType =>
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

                {selectedStrategy !== undefined && selectedStrategy.type === StrategyType.ACCESSES_SCIPY &&   // Show sources request form
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

                {strategies.length !== 0 &&
                    <>
                        <h4 className="mt-4" style={{color: "#666666"}}>
                            Strategies
                        </h4>
                        <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                            {strategies.map(strategy => strategy.printCard(handleDeleteStrategy))}
                        </div>
                    </>
                }
                {sources.length !== 0 &&
                    <>
                        <h4 className="mt-4" style={{ color: "#666666" }}>
                            Sources
                        </h4>
                        <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                            {sources.map(source => source.printCard(handleDeleteSource))}
                        </div>
                    </>
                }
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

            { renderSources() }
        </div>
    );
}