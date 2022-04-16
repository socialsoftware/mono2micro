import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

const HttpStatus = require('http-status-codes');

export const Codebases = () => {
    const [newCodebaseName, setNewCodebaseName] = useState("");
    const [newDatafilePath, setNewDatafilePath] = useState("");
    const [selectedFile, setSelectedFile] = useState(null);
    const [translationFile, setTranslationFile] = useState(null);
    const [commitFile, setCommitFile] = useState(null);
    const [authorFile, setAuthorFile] = useState(null);
    const [isUploaded, setIsUploaded] = useState("");
    const [codebases, setCodebases] = useState([]);

    //Executed on mount
    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases(
            ["name"],
        ).then(response => {
            setCodebases(response.data);
        });
    }

    function handleSelectedFile(event) {
        if (event.target.files.length !== 4) {
            setIsUploaded("Data Collection File, ID to Entity File, Commit Collection File and Author Collection File need to be uploaded simultaneously.");
            return;
        }
        setSelectedFile(event.target.files[0]);
        setTranslationFile(event.target.files[1]);
        setCommitFile(event.target.files[2]);
        setAuthorFile(event.target.files[3]);
        setNewDatafilePath("");
        setIsUploaded("");
    }

    function handleStaticFile(event) {
        setSelectedFile(event.target.files[0]);
        setNewDatafilePath("");
        setIsUploaded("");
    }

    function handleCommitFile(event) {
        setCommitFile(event.target.files[0]);
        setNewDatafilePath("");
        setIsUploaded("");
    }

    function handleAuthorFile(event) {
        console.log(event.target.files[0]);
        setAuthorFile(event.target.files[0]);
        setNewDatafilePath("");
        setIsUploaded("");
    }

    function handleIDToEntityFile(event) {
        setTranslationFile(event.target.files[0]);
        setNewDatafilePath("");
        setIsUploaded("");
    }

    function handleChangeNewCodebaseName(event) {
        setNewCodebaseName(event.target.value);
    }

    function handleChangeNewDatafilePath(event) {
        setNewDatafilePath(event.target.value);
        setSelectedFile(null);
        setTranslationFile(null);
        setCommitFile(null);
    }

    function handleDeleteCodebase(codebaseName) {
        const service = new RepositoryService();
        service.deleteCodebase(codebaseName).then(response => {
            loadCodebases();
        });
    }

    function handleSubmit(event){
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (selectedFile !== null && translationFile !== null && commitFile !== null && authorFile !== null) {
            doCreateCodebaseRequest(
                newCodebaseName,
                selectedFile,
                translationFile,
                commitFile,
                authorFile
            );
        }
        else {
            doCreateCodebaseRequest(
                newCodebaseName,
                newDatafilePath,
            );
        }
    }

    function doCreateCodebaseRequest(codebaseName, pathOrFile, translationFile = null, commitFile = null, authorFile = null) {
        const service = new RepositoryService();
        service.createCodebase(codebaseName, pathOrFile, translationFile, commitFile, authorFile)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadCodebases();
                    setIsUploaded("Upload completed successfully.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    setIsUploaded("Upload failed. Codebase name already exists.");
                }
                else if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                    setIsUploaded("Upload failed. Invalid datafile path.");
                }
                else {
                    setIsUploaded("Upload failed.");
                }
            });
        setNewCodebaseName("");
        setSelectedFile(null);
        setTranslationFile(null);
        setCommitFile(null);
        setAuthorFile(null)
    }

    function renderCodebases() {
        return (
            <Row>
                {
                    codebases.map(codebase =>
                        <Col key={codebase.name} md="auto">
                            <Card style={{ width: '15rem', marginBottom: "16px" }}>
                                <Card.Body>
                                    <Card.Title>
                                        {codebase.name}
                                    </Card.Title>
                                    <Button
                                        href={`/codebases/${codebase.name}`}
                                        className="mb-2"
                                    >
                                        Go to Codebase
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

                <Form.Group as={Row} controlId="datafilePath" className="mb-3">
                    <Form.Label column sm={2}>
                        Datafile Absolute Path
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            placeholder="/home/example/datafile.json"
                            value={newDatafilePath}
                            onChange={handleChangeNewDatafilePath}
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="OR" className="mb-3">
                    <Form.Label column sm={2}></Form.Label>
                    <Col sm={5}> OR </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="static-data-collection" className="mb-3">
                    <Form.Label column sm={2}>
                        Data Collection File
                    </Form.Label>
                    <Col sm={5}>
                        <Form.Control
                            type="file"
                            onChange={handleStaticFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="commit-data-collection" className="mb-3">
                    <Form.Label column sm={2}>
                        Commit Collection File
                    </Form.Label>
                    <Col sm={5}>
                        <Form.Control
                            type="file"
                            onChange={handleCommitFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="author-data-collection" className="mb-3">
                    <Form.Label column sm={2}>
                        Author Collection File
                    </Form.Label>
                    <Col sm={5}>
                        <Form.Control
                            type="file"
                            onChange={handleAuthorFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="id-to-entity" className="mb-3">
                    <Form.Label column sm={2}>
                        ID to Entity File
                    </Form.Label>
                    <Col sm={5}>
                        <Form.Control
                            type="file"
                            onChange={handleIDToEntityFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="mb-3">
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..." ||
                                newCodebaseName === "" ||
                                ((translationFile === null || selectedFile === null) &&
                                    newDatafilePath === "")
                            }
                        >
                            Create Codebase
                        </Button>
                        <Form.Text className="ms-3">
                            {isUploaded}
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
        <div>
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
