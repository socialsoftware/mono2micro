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
    const [strategies, setStrategies] = useState([]);
    const [representations, setRepresentations] = useState([]);
    const [requiredRepresentations, setRequiredRepresentations] = useState([]);
    const [obtainedRepresentations, setObtainedRepresentations] = useState([]);
    const [selectedRepresentationInfoType, setSelectedRepresentationInfoType] = useState(undefined);
    const [addedRepresentations, setAddedRepresentations] = useState({});
    const [isUploaded, setIsUploaded] = useState("");
    const [canSubmit, setCanSubmit] = useState(false);
    const [add, setAdd] = useState("");
    const [representationInfoTypes, setRepresentationInfoTypes] = useState([]);
    const [codebaseRepresentationInfoTypes, setCodebaseRepresentationInfoTypes] = useState([]);
    const [strategyRepresentations, setStrategyRepresentations] = useState([]);
    const [algorithms, setAlgorithms] = useState([]);
    const [strategyAlgorithm, setStrategyAlgorithm] = useState(undefined);
    const [supportedRepresentationInfoTypes, setSupportedRepresentationInfoTypes] = useState([]);

    //Executed on mount
    useEffect(() => {
        const service = new APIService();
        loadStrategies();
        loadRepresentations();
        loadCodebaseRepresentationInfoTypes();
        service.getAlgorithms().then(response => setAlgorithms(response.data));
        service.getRepresentationInfoTypes().then(response => setRepresentationInfoTypes(response.data));
    }, []);

    function loadCodebaseRepresentationInfoTypes() {
        const service = new APIService();
        service.getCodebaseRepresentationInfoTypes(codebaseName).then(response => setCodebaseRepresentationInfoTypes(response.data));
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
            setStrategies(response);
        });
    }

    function handleRepresentationsSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new APIService();
        service.addRepresentations(codebaseName, selectedRepresentationInfoType, addedRepresentations)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCodebaseRepresentationInfoTypes();
                    loadRepresentations();
                    setIsUploaded("");
                } else setIsUploaded("Upload failed.");

                setSelectedRepresentationInfoType(undefined);
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
        setSelectedRepresentationInfoType(representationType);
        setRequiredRepresentations(representationInfoTypes[representationType]);
        let representationTypes = representations.map(rep => rep.type);
        setCanSubmit(representationInfoTypes[representationType].reduce((p, c) => {return p? representationTypes.includes(c) : false}, true));
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
            loadCodebaseRepresentationInfoTypes();
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
                setSupportedRepresentationInfoTypes([]);
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
            if (prev.includes(type))
                prev = prev.filter(t => t !== type);
            else prev = [...prev, type];
            return prev;
        });
    }

    function clickStrategyAlgorithm(type) {
        const service = new APIService();
        service.getSupportedRepresentationInfoTypes(type).then(response => {
            setStrategyAlgorithm(type);
            setSupportedRepresentationInfoTypes(response.data.filter(rep => codebaseRepresentationInfoTypes.includes(rep)));
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

                {supportedRepresentationInfoTypes.length !== 0 &&
                    <Form.Group as={Row} className="align-items-center mb-3">
                        <Form.Label as="legend" column sm={2}>
                            Representation Informations
                        </Form.Label>
                        <Col sm={3} style={{ paddingLeft: 0 }}>
                            {supportedRepresentationInfoTypes.map(type =>
                                <Col key={type} sm="auto">
                                    <Form.Check onClick={() => clickStrategyRepresentations(type)} name="representation" label={type} type="checkbox" id={type} value={type} />
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
                        <DropdownButton title={selectedRepresentationInfoType === undefined? "Choose Representation Type" : selectedRepresentationInfoType}>
                            {representationInfoTypes !== undefined && Object.keys(representationInfoTypes).filter(representationType => !codebaseRepresentationInfoTypes.includes(representationType))
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

                {selectedRepresentationInfoType !== undefined &&
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
            setSupportedRepresentationInfoTypes([]);
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
                {[...strategies.map(strategy => strategy.printCard(handleDeleteStrategy)), addCard("Strategy")]}
            </div>
        </div>
    );
}