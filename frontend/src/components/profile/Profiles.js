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
    let { codebaseName } = useParams();
    const [codebase, setCodebase] = useState({});
    const [newProfileName, setNewProfileName] = useState("");
    const [moveToProfile, setMoveToProfile] = useState("");
    const [selectedControllers, setSelectedControllers] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");

    useEffect(() => loadCodebase(), []);

    function loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName, ["profiles"]).then(response => {
            setCodebase(response.data === null ? {} : response.data);
        });
    }

    function handleChangeNewProfileName(event) {
        setNewProfileName(event.target.value);
    }

    function handleNewProfileSubmit(event) {
        event.preventDefault();

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.addProfile(codebaseName, newProfileName).then(response => {
            if (response.status === HttpStatus.OK) {
                loadCodebase();
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

    function handleMoveControllersSubmit() {
        const service = new RepositoryService();
        
        service.moveControllers(
            codebaseName,
            selectedControllers,
            moveToProfile
        ).then(() => {
            setSelectedControllers([]);
            loadCodebase();
        });
    }

    function handleDeleteProfile(profile) {
        const service = new RepositoryService();
        
        service.deleteProfile(
            codebaseName,
            profile
        ).then(() => {
            loadCodebase();
        });
    }

    function handleSelectController(event) {
        const eventTargetId = event.target.id;

        if (selectedControllers.includes(eventTargetId)) {
            setSelectedControllers(selectedControllers.filter(c => c !== eventTargetId));
            return;
        }

        setSelectedControllers([...selectedControllers, eventTargetId]);
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
                <Breadcrumb.Item active>
                    Profiles
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    return (
        <div>
            {renderBreadCrumbs()}
            <h4 style={{color: "#666666"}}>
                Create Controller Profile
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
                        <Form.Text>
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>

            <h4 style={{color: "#666666"}}>
                Controller Profiles
            </h4>
            {Object.keys(codebase).length &&
                <div>
                    <ButtonToolbar>
                        <Button className="me-1">Move selected controllers to</Button>

                        <DropdownButton
                            as={ButtonGroup}
                            title={moveToProfile === "" ? "Controller Profile" : moveToProfile}
                            className="me-1"
                        >
                            {Object.keys(codebase.profiles).map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onClick={() => handleMoveToProfile(profile)}
                                >
                                        {profile}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>

                        <Button
                            disabled={selectedControllers.length === 0 || moveToProfile === ""}
                            onClick={handleMoveControllersSubmit}
                        >
                            Submit
                        </Button>
                    </ButtonToolbar>

                    {Object.keys(codebase.profiles).map(profile =>
                        <div key={profile}>
                            <div style={{fontSize: '25px'}}>
                                {profile}
                                {!codebase.profiles[profile].length &&
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

                            {codebase.profiles[profile].map(controller =>
                                <Form.Check
                                    id={controller}
                                    key={controller}
                                    type="checkbox"
                                    label={controller}
                                    checked={selectedControllers.includes(controller)}
                                    onChange={handleSelectController}
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