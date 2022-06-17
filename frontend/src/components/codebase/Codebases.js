import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {ArrowForward} from "@mui/icons-material";

const HttpStatus = require('http-status-codes');

export const Codebases = () => {
    const [newCodebaseName, setNewCodebaseName] = useState("");
    const [codebases, setCodebases] = useState([]);
    const [isCreated, setIsCreated] = useState("");

    //Executed on mount
    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases(
            ['name', 'collectors'],
        ).then(response => {
            setCodebases(response.data);
        });
    }

    function handleChangeNewCodebaseName(event) {
        setNewCodebaseName(event.target.value);
    }

    function handleDeleteCodebase(codebaseName) {
        const service = new RepositoryService();
        service.deleteCodebase(codebaseName).then(response => {
            loadCodebases();
        });
    }

    function renderCodebases() {
        return (
            <Row>
                {
                    codebases.map(codebase =>
                        <Col key={codebase.name} md="auto">
                            <Card className={"text-center"} style={{ width: '15rem', marginBottom: "16px" }}>
                                <Card.Header>{codebase.name}</Card.Header>
                                <Card.Body>
                                    <Button
                                        href={`/codebases/${codebase.name}/strategies`}
                                        className="mb-2"
                                        variant={"success"}
                                        disabled={codebase.collectors.length === 0}
                                    >
                                        Go to Strategies <ArrowForward/>
                                    </Button>
                                    <br/>
                                    <Button
                                        href={`/codebases/${codebase.name}`}
                                        className="mb-2"
                                        variant={"primary"}
                                    >
                                        Go to Collectors <b>+</b>
                                    </Button>
                                    <br/>
                                    <Button
                                        onClick={() => handleDeleteCodebase(codebase.name)}
                                        variant="danger"
                                    >
                                        Delete
                                    </Button>
                                </Card.Body>
                            </Card>
                        </Col>
                    )
                }
            </Row>
        );
    }

    function handleSubmit() {
        event.preventDefault()

        setIsCreated("Creating...");

        const service = new RepositoryService();
        service.createCodebase(newCodebaseName)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCodebases();
                    setIsCreated("Creation completed successfully.");
                } else {
                    setIsCreated("Creation failed.");
                }
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    setIsCreated("Creation failed. Codebase name already exists.");
                }
                else setIsCreated("Creation failed.");
            });
    }

    function renderCreateCodebaseForm() {
        return (
            <Form onSubmit={handleSubmit}>
                <Form.Group as={Row} controlId="newCodebaseName" className="mb-3 align-items-center">
                    <Form.Label column sm={2}>
                        Codebase Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            maxLength="256"
                            placeholder="Codebase Name"
                            value={newCodebaseName}
                            onChange={handleChangeNewCodebaseName}
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="mb-3">
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={
                                isCreated === "Creating..." ||
                                newCodebaseName === ""
                            }
                        >
                            Create Codebase
                        </Button>
                        <Form.Text className="ms-3">
                            {isCreated}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }


    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    Codebases
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }


    return (
        <div style={{ paddingLeft: "2rem" }}>
            {renderBreadCrumbs()}

            <h4 style={{color: "#666666"}}>
                Create Codebase
            </h4>
            {renderCreateCodebaseForm()}

            <h4 style={{color: "#666666"}}>
                Codebases
            </h4>
            {renderCodebases()}
        </div>
    );
}