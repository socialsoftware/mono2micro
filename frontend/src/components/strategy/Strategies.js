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
import {APIService} from "../../services/APIService";
import HttpStatus from "http-status-codes";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import {toast} from "react-toastify";
import FormControl from "react-bootstrap/FormControl";
import {AddCircleOutline, CheckCircle} from "@mui/icons-material";
import MUIButton from "@mui/material/Button";
import Card from "react-bootstrap/Card";

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
    const [representationToDelete, setRepresentationToDelete] = useState(undefined);
    const [codebaseStrategies, setCodebaseCodebaseStrategies] = useState([]);
    const [representations, setRepresentations] = useState([]);
    const [requiredRepresentations, setRequiredRepresentations] = useState([]);
    const [obtainedRepresentations, setObtainedRepresentations] = useState([]);
    const [selectedRepresentationType, setSelectedRepresentationType] = useState(undefined);
    const [addedRepresentations, setAddedRepresentations] = useState({});
    const [isUploaded, setIsUploaded] = useState("");
    const [canSubmit, setCanSubmit] = useState(false);
    const [add, setAdd] = useState("");
    const [representationGroups, setRepresentationGroups] = useState([]);
    const [codebaseRepresentationGroups, setCodebaseRepresentationGroups] = useState([]);
    const [strategyRepresentations, setStrategyRepresentations] = useState([]);
    const [algorithms, setAlgorithms] = useState([]);
    const [strategyAlgorithm, setStrategyAlgorithm] = useState(undefined);
    const [availableStrategyTypes, setAvailableStrategyTypes] = useState([]);

    //Executed on mount
    useEffect(() => {
        const service = new APIService();
        loadStrategies();
        loadRepresentations();
        loadCodebaseRepresentationGroups();
        service.getAlgorithms().then(response => setAlgorithms(response.data));
        service.getRepresentationGroups().then(response => setRepresentationGroups(response.data));
    }, []);

    function loadCodebaseRepresentationGroups() {
        const service = new APIService();
        service.getCodebaseRepresentationGroups(codebaseName).then(response => setCodebaseRepresentationGroups(response.data));
    }

    function loadRepresentations() {
        const service = new APIService();
        service.getRepresentations(codebaseName).then(response => {
            setRepresentations(response);
            setObtainedRepresentations(response.map(representation => representation.type));
        });
    }

    function loadStrategies() {
        const service = new APIService();
        service.getCodebaseStrategies(codebaseName).then(response => {
            setCodebaseCodebaseStrategies(response);
        });
    }

    function handleRepresentationsSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new APIService();
        service.addRepresentations(codebaseName, selectedRepresentationType, addedRepresentations)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCodebaseRepresentationGroups();
                    loadRepresentations();
                    setIsUploaded("");
                } else setIsUploaded("Upload failed.");

                setSelectedRepresentationType(undefined);
                setRequiredRepresentations([]);
                setAddedRepresentations({});
                setAdd("");
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
            <div> Select the decomposition type and add the required files, then proceed to the similarities or customize the representations</div>
        </Popover>
    );

    function handleSelectedRepresentation(representationType) {
        setSelectedRepresentationType(representationType);
        setRequiredRepresentations(representationGroups[representationType]);
        let representationTypes = representations.map(rep => rep.type);
        setCanSubmit(representationGroups[representationType].reduce((p, c) => {return p? representationTypes.includes(c) : false}, true));
    }

    function addRepresentation(representation, event) {
        setAddedRepresentations(prev => {
            prev[representation] = event.target.files[0];
            let totalRepresentations = [...representations.map(r => r.type), ...Object.keys(prev)];
            if (requiredRepresentations.reduce((p, c) => {return p? totalRepresentations.includes(c) : false}, true))
                setCanSubmit(true);
            return prev;
        });
    }

    function handleDeleteRepresentation(representation) {
        setRepresentationToDelete(representation);
        setShowPopup(true);
    }

    function confirmRepresentationToDelete() {
        setShowPopup(false);

        let toastId = toast.loading("Deleting representation...", {type: toast.TYPE.INFO});
        const service = new APIService();
        service.deleteRepresentation(representationToDelete.name).then(() => {
            loadCodebaseRepresentationGroups();
            loadRepresentations();
            loadStrategies();
            toast.dismiss(toastId);
        });
    }

    function handleDeleteStrategy(strategy) {
        let toastId = toast.loading("Deleting strategy...", {type: toast.TYPE.INFO});
        const service = new APIService();
        service.deleteStrategy(strategy.name).then(() => {
            loadStrategies();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategy deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + strategy.name + ".", isLoading: false});
        });
    }

    function handleStrategiesSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new APIService();
        service.createStrategy(codebaseName, strategyRepresentations, strategyAlgorithm)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadStrategies();
                    setIsUploaded("");
                } else setIsUploaded("Upload failed.");

                setStrategyRepresentations([]);
                setStrategyAlgorithm(undefined);
                setAvailableStrategyTypes([]);
                setAdd("");
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

    function clickStrategyRepresentations(type) {
        setStrategyRepresentations(prev => {
            if (prev.includes(type)) {
                prev = prev.filter(t => t !== type);
            } else if (prev.some((el) => el.includes('Vectorization')) || type.includes('Vectorization')) {
                prev.forEach((t) => document.getElementById(t).checked = false);
                prev = [type];
            } else {
                prev = [...prev, type];
            }
            return prev;
        });
    }

    function includesStrategy(strategy) {
        // this is too specific and needs to be refactored

        let existing = codebaseStrategies.map(strategy => strategy.strategyTypes).filter((strategyTypes) => strategyTypes.includes(strategy));

        // the vectorization strategies can not be combined
        if (existing.length === 1 && existing[0].length === 1 && existing[0][0].includes('Vectorization')) {
            return true;
        }

        // accesses and repository can be combined
        if (existing.length == 2) return true;

        return false;
    }

    function clickStrategyAlgorithm(type) {
        const service = new APIService();
        service.getAllowableCodebaseStrategyTypes(codebaseName).then(response => {
            setAvailableStrategyTypes(response.data.filter(strategy => !includesStrategy(strategy)));
        })

        service.getAlgorithmSupportedStrategyTypes(type).then(response => {
            setStrategyAlgorithm(type);
            setAvailableStrategyTypes(response.data.filter(strategy => availableStrategyTypes.includes(strategy)));
        });
    }

    function renderStrategies() {
        return (
            <Form onSubmit={handleStrategiesSubmit}>
                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={2}>
                        Algorithm
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        {algorithms.map(algorithm =>
                            <Col key={algorithm} sm="auto">
                                <Form.Check onClick={() => clickStrategyAlgorithm(algorithm)} name="algorithm" label={algorithm} type="radio" id={algorithm} value={algorithm}/>
                            </Col>
                        )}
                    </Col>
                </Form.Group>

                {availableStrategyTypes.length !== 0 &&
                    <Form.Group as={Row} className="align-items-center mb-3">
                        <Form.Label as="legend" column sm={2}>
                            Criteria
                        </Form.Label>
                        <Col sm={8} style={{ paddingLeft: 0 }}>
                            {availableStrategyTypes.map(type =>
                                <Col key={type} sm="auto">
                                    <Form.Check
                                        onClick={() => clickStrategyRepresentations(type)}
                                        name="representation"
                                        label={type}
                                        type="checkbox"
                                        id={type}
                                        value={type}
                                    />
                                </Col>
                            )}
                        </Col>
                    </Form.Group>
                }

                <Form.Group as={Row} className="align-items-center mb-4">
                    <Col sm={{offset: 2}}>
                        <Button type="submit" disabled={strategyAlgorithm === undefined || strategyRepresentations.length === 0}>Submit</Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    function renderRepresentations() {
        return (
            <Form onSubmit={handleRepresentationsSubmit}>
                <Form.Group as={Row} controlId="selectRepresentationInfo" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={selectedRepresentationType === undefined? "Choose Representation Type" : selectedRepresentationType}>
                            {representationGroups !== undefined && Object.keys(representationGroups).filter(representationType => !codebaseRepresentationGroups.includes(representationType))
                                .map(representationType =>
                                    <Dropdown.Item
                                        key={representationType}
                                        onClick={() => handleSelectedRepresentation(representationType)}
                                    >
                                        {representationType}
                                    </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>
                {requiredRepresentations.map(representationType =>
                    <React.Fragment key={representationType}>
                        {!obtainedRepresentations.includes(representationType) &&
                            <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                                <Form.Label column sm={2}>
                                    {representationType + " File"}
                                </Form.Label>
                                <Col sm={5}>
                                    <FormControl
                                        type="file"
                                        onChange={event => addRepresentation(representationType, event)}
                                    />
                                </Col>
                            </Form.Group>
                        }
                        {obtainedRepresentations.includes(representationType) &&
                            <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                                <Form.Label column sm={2}>
                                    {representationType + " File"}
                                </Form.Label>
                                <Col sm={5}>
                                    <MUIButton variant="contained" disableRipple={true} disableElevation={true} color="success" sx={{ bgcolor: "success.dark" }} endIcon={<CheckCircle/>}>
                                        Already Added
                                    </MUIButton>
                                </Col>
                            </Form.Group>
                        }
                    </React.Fragment>
                )}

                {selectedRepresentationType !== undefined &&
                    <Form.Group as={Row} className="align-items-center mb-4">
                        <Col sm={{offset: 2}}>
                            <Button type="submit" disabled={!canSubmit}>Submit</Button>
                            <Form.Text className="ms-2">
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                }
            </Form>
        );
    }

    function addCard(add) {
        return (
            <Card className={"text-center"} key="add" style={{cursor: "pointer", borderStyle: "dashed", borderWidth: "thick", backgroundColor: "#00000000", width: '13rem'}} onClick={() => setAdd(add)}>
                <div style={{ position: "relative", marginTop: "50%", marginBottom: "40%", marginLeft: "50%", transform: "translate(-50%,-50%)" }}>
                    <AddCircleOutline style={{transform: "scale(3)", color: "#00000022" }}/>
                </div>
            </Card>
        );
    }

    function renderDeleteModal() {
        const closePopup = function () {setShowPopup(false); setRepresentationToDelete(undefined)};

        return <Modal
                show={showPopup}
                onHide={() => closePopup()}
                backdrop="static"
            >
                <ModalTitle>&ensp;Representation Deletion</ModalTitle>
                <ModalBody>
                    Strategies created from this representation will be deleted.<br/>Are you sure you want to delete this Representation?
                </ModalBody>
                <ModalFooter>
                    <Button variant="secondary" onClick={() => closePopup()}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={() => confirmRepresentationToDelete()}>Delete</Button>
                </ModalFooter>
            </Modal>;
    }

    function renderAddModal() {
        const closeAddPopup = function () {
            setAdd("");
            setStrategyRepresentations([]);
            setStrategyAlgorithm(undefined);
            setAvailableStrategyTypes([]);
        };

        return <Modal
            show={add !== ""}
            onHide={() => closeAddPopup()}
            backdrop="static"
            size='lg'
        >
            <ModalTitle>&ensp;Add {add}</ModalTitle>
            <ModalBody>
                {add === "Strategy" && renderStrategies()}
                {add === "Representation" && renderRepresentations()}
            </ModalBody>
            <ModalFooter>
                <Button variant="secondary" onClick={() => closeAddPopup()}>
                    Close
                </Button>
            </ModalFooter>
        </Modal>;
    }

    return (
        <div style={{ paddingLeft: "2rem" }}>
            { renderDeleteModal() }

            { renderAddModal() }

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

            <h4 className="mt-4" style={{ color: "#666666" }}>
                Representation Files
            </h4>
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {[...representations.map(representation => representation.printCard(handleDeleteRepresentation)), addCard("Representation")]}
            </div>

            <h4 className="mt-4" style={{color: "#666666"}}>
                Strategies
            </h4>
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {[...codebaseStrategies.map(strategy => strategy.printCard(handleDeleteStrategy)), addCard("Strategy")]}
            </div>
        </div>
    );
}