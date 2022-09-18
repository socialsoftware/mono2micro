import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import {useParams} from "react-router-dom";

const HttpStatus = require('http-status-codes');

export const Profiles = () => {
    let { codebaseName, representationName } = useParams();
    const [representation, setRepresentation] = useState({});
    const [newProfileName, setNewProfileName] = useState("");
    const [moveToProfile, setMoveToProfile] = useState("");
    const [selectedFunctionalities, setSelectedFunctionalities] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");

    useEffect(() => loadRepresentation(), []);

    function loadRepresentation() {
        const service = new RepositoryService();
        service.getRepresentation(representationName).then(response => {
            setRepresentation(response === null ? {} : response);
        });
    }

    function handleChangeNewProfileName(event) {
        setNewProfileName(event.target.value);
    }

    function handleNewProfileSubmit(event) {
        event.preventDefault();

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.addAccessesProfile(representationName, newProfileName).then(response => {
            if (response.status === HttpStatus.OK) {
                loadRepresentation();
                setIsUploaded("Upload completed successfully.");
            } else {
                setIsUploaded("Upload failed.");
            }
        })
        .catch(error => {
            if (error.response?.status === HttpStatus.UNAUTHORIZED) {
                setIsUploaded("Upload failed. Profile name already exists.");
            } else {
                setIsUploaded("Upload failed.");
            }
        });
    }

    function handleMoveToProfile(profile) {
        setMoveToProfile(profile);
    }

    function handleMoveFunctionalitiesSubmit() {
        const service = new RepositoryService();

        service.moveAccessesFunctionalities(
            representationName,
            selectedFunctionalities,
            moveToProfile
        ).then(() => {
            setSelectedFunctionalities([]);
            loadRepresentation();
        });
    }

    function handleDeleteProfile(profile) {
        const service = new RepositoryService();

        service.deleteAccessesProfile(
            representationName,
            profile
        ).then(() => {
            loadRepresentation();
        });
    }

    function handleSelectFunctionality(event) {
        const eventTargetId = event.target.id;

        if (selectedFunctionalities.includes(eventTargetId)) {
            setSelectedFunctionalities(selectedFunctionalities.filter(c => c !== eventTargetId));
            return;
        }

        setSelectedFunctionalities([...selectedFunctionalities, eventTargetId]);
    }

    const renderBreadCrumbs = () => {
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
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>
                    {representationName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    Profiles
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    return (
        <div style={{ paddingLeft: "2rem" }}>
            {renderBreadCrumbs()}
            <h4 style={{color: "#666666"}}>
                Create Functionality Profile
            </h4>

            <Form onSubmit={handleNewProfileSubmit}>
                <Form.Group className="mb-3" as={Row} controlId="newProfileName">
                    <Form.Label column sm={2}>
                        Profile Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            maxLength="30"
                            placeholder="Profile Name"
                            value={newProfileName}
                            onChange={handleChangeNewProfileName}
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={isUploaded === "Uploading..." || newProfileName === ""}
                        >
                            Create Profile
                        </Button>
                        <Form.Text className={"ms-2"}>
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>

            <h4 style={{color: "#666666"}}>
                Functionality Profiles
            </h4>
            {Object.keys(representation).length &&
                <div>
                    <ButtonToolbar>
                        <Button className="me-1">Move selected functionalities to</Button>

                        <DropdownButton
                            as={ButtonGroup}
                            title={moveToProfile === "" ? "Functionality Profile" : moveToProfile}
                            className="me-1"
                        >
                            {Object.keys(representation.profiles).map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onClick={() => handleMoveToProfile(profile)}
                                >
                                        {profile}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>

                        <Button
                            disabled={selectedFunctionalities.length === 0 || moveToProfile === ""}
                            onClick={handleMoveFunctionalitiesSubmit}
                        >
                            Submit
                        </Button>
                    </ButtonToolbar>

                    {Object.keys(representation.profiles).map(profile =>
                        <div key={profile}>
                            <div style={{fontSize: '25px'}}>
                                {profile}
                                {!representation.profiles[profile].length &&
                                    <Button
                                        onClick={() => handleDeleteProfile(profile)}
                                        className="ms-2"
                                        variant="danger"
                                        size="sm"
                                    >
                                        -
                                    </Button>
                                }
                            </div>

                            {representation.profiles[profile].map(functionality =>
                                <Form.Check
                                    id={functionality}
                                    key={functionality}
                                    type="checkbox"
                                    label={functionality}
                                    checked={selectedFunctionalities.includes(functionality)}
                                    onChange={handleSelectFunctionality}
                                    style={{ paddingLeft: "3em" }}
                                />
                            )}
                        </div>
                    )}
                </div>
            }
        </div>
    );
}