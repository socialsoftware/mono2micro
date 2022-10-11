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
import {toast} from "react-toastify";
import FormControl from "react-bootstrap/FormControl";
import {CheckCircle} from "@mui/icons-material";
import MUIButton from "@mui/material/Button";

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
    const [decompositionTypes, setDecompositionTypes] = useState(undefined);
    const [selectedDecompositionType, setSelectedDecompositionType] = useState(undefined);
    const [addedRepresentations, setAddedRepresentations] = useState({});
    const [isUploaded, setIsUploaded] = useState("");
    const [canSubmit, setCanSubmit] = useState(false);

    //Executed on mount
    useEffect(() => {
        const service = new RepositoryService();
        loadStrategies();
        loadRepresentations();
        service.getDecompositionTypes(codebaseName).then(response => setDecompositionTypes(response.data));
    }, []);

    function loadRepresentations() {
        const service = new RepositoryService();
        service.getRepresentations(codebaseName).then(response => {
            setRepresentations(response);
            setObtainedRepresentations(response.map(representation => representation.type));
        });
    }

    function loadStrategies() {
        const service = new RepositoryService();
        service.getCodebaseStrategies(codebaseName).then(response => {
            setStrategies(response);
        });
    }

    function handleRepresentationsSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createStrategy(codebaseName, selectedDecompositionType, addedRepresentations)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadStrategies();
                    loadRepresentations();
                    setIsUploaded("");
                } else setIsUploaded("Upload failed.");

                setSelectedDecompositionType(undefined);
                setRequiredRepresentations([]);
                setAddedRepresentations({});
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

    function handleSelectedDecomposition(decompositionType) {
        setSelectedDecompositionType(decompositionType);
        const service = new RepositoryService();
        service.getRequiredRepresentations(decompositionType).then(response => {
            setRequiredRepresentations(response.data);

            let representationTypes = representations.map(rep => rep.type);
            if (response.data.reduce((p, c) => {return p? representationTypes.includes(c) : false}, true))
                setCanSubmit(true);
        });
    }

    function addRepresentation(representation, event) {
        setAddedRepresentations(prev => {
            prev[representation] = event.target.files[0];
            if (Object.keys(prev).reduce((p, c) => {return p? requiredRepresentations.includes(c) : false}, true))
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
        const service = new RepositoryService();
        service.deleteRepresentation(representationToDelete.name).then(() => {
            loadRepresentations();
            loadStrategies();
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

    function renderRepresentations() {
        return (
            <Form onSubmit={handleRepresentationsSubmit}>
                <Form.Group as={Row} controlId="selectDecompositionType" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={selectedDecompositionType === undefined? "Choose Decomposition Type" : selectedDecompositionType}>
                            {decompositionTypes !== undefined && decompositionTypes.filter(decompositionType => !strategies || !strategies.find(strategy => strategy.decompositionType === decompositionType))
                                .map(decompositionType =>
                                    <Dropdown.Item
                                        key={decompositionType}
                                        onClick={() => handleSelectedDecomposition(decompositionType)}
                                    >
                                        {decompositionType}
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

                {selectedDecompositionType !== undefined &&
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
                {representations.length !== 0 &&
                    <>
                        <h4 className="mt-4" style={{ color: "#666666" }}>
                            Representations
                        </h4>
                        <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                            {representations.map(representation => representation.printCard(handleDeleteRepresentation))}
                        </div>
                    </>
                }
            </Form>
        );
    }

    function renderDeletePopup() {
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

            { renderRepresentations() }
        </div>
    );
}