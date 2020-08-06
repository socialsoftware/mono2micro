import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Card, Button, Form, FormControl, Breadcrumb } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Codebases extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newCodebaseName: "",
            newDatafilePath: "",
            selectedFile: null,
            isUploaded: "",
            codebases: []
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewCodebaseName = this.handleChangeNewCodebaseName.bind(this);
        this.handleChangeNewDatafilePath = this.handleChangeNewDatafilePath.bind(this);
        this.handleDeleteCodebase = this.handleDeleteCodebase.bind(this);
        this.doCreateCodebaseRequest = this.doCreateCodebaseRequest.bind(this);
    }

    componentDidMount() {
        this.loadCodebases();
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            this.setState({
                codebases: response.data
            });
        });
    }

    handleSelectedFile(event) {
        this.setState({
            selectedFile: event.target.files[0],
            newDatafilePath: ""
        });
    }

    handleChangeNewCodebaseName(event) {
        this.setState({
            newCodebaseName: event.target.value
        });
    }

    handleChangeNewDatafilePath(event) {
        this.setState({
            newDatafilePath: event.target.value,
            selectedFile: null
        });
        // reset input form
    }

    handleDeleteCodebase(codebaseName) {
        const service = new RepositoryService();
        service.deleteCodebase(codebaseName).then(response => {
            this.loadCodebases();
        });
    }

    handleSubmit(event){
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });

        if (this.state.selectedFile !== null) {
            this.doCreateCodebaseRequest(this.state.newCodebaseName, this.state.selectedFile);
        }
        else {
            this.doCreateCodebaseRequest(this.state.newCodebaseName, this.state.newDatafilePath);
        }
    }

    doCreateCodebaseRequest(codebaseName, pathOrFile) {
        const service = new RepositoryService();
        service.createCodebase(codebaseName, pathOrFile)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    this.loadCodebases();
                    this.setState({
                        isUploaded: "Upload completed successfully."
                    });
                } else {
                    this.setState({
                        isUploaded: "Upload failed."
                    });
                }
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    this.setState({
                        isUploaded: "Upload failed. Codebase name already exists."
                    });
                }
                else if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                    this.setState({
                        isUploaded: "Upload failed. Invalid datafile path."
                    });
                }
                else {
                    this.setState({
                        isUploaded: "Upload failed."
                    });
                }
            });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item active>Codebases</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    renderCreateCodebaseForm = () => {
        return (
            <Form onSubmit={this.handleSubmit}>
                <Form.Group as={Row} controlId="newCodebaseName">
                    <Form.Label column sm={2}>
                        Codebase Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            maxLength="30"
                            placeholder="Codebase Name"
                            value={this.state.newCodebaseName}
                            onChange={this.handleChangeNewCodebaseName}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="datafilePath">
                    <Form.Label column sm={2}>
                        Datafile Absolute Path
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            placeholder="/home/example/datafile.json"
                            value={this.state.newDatafilePath}
                            onChange={this.handleChangeNewDatafilePath}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="OR">
                    <Form.Label column sm={2}></Form.Label>
                    <Col sm={5}> OR </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="datafile">
                    <Form.Label column sm={2}>
                        Data Collection File
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="file"
                            onChange={this.handleSelectedFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button type="submit"
                                disabled={this.state.isUploaded === "Uploading..." ||
                                        this.state.newCodebaseName === "" ||
                                        (this.state.selectedFile === null &&
                                        this.state.newDatafilePath === "")}>
                            Create Codebase
                        </Button>
                        <Form.Text>
                            {this.state.isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    renderCodebases = () => {
        return this.state.codebases.map(codebase =>
            <div>
                <Card key={codebase.name} style={{ width: '15rem' }}>
                    <Card.Body>
                        <Card.Title>
                            {codebase.name}
                        </Card.Title>
                        <Button href={`/codebases/${codebase.name}`} 
                                className="mb-2">
                                    Go to Codebase
                        </Button><br/>
                        <Button onClick={() => this.handleDeleteCodebase(codebase.name)} 
                                variant="danger">
                                    Delete
                        </Button>
                    </Card.Body>
                </Card>
                <br/>
            </div>
        );
    }

    render() {
        return (
            <div>
                {this.renderBreadCrumbs()}
                
                <h4 style={{color: "#666666"}}>Create Codebase</h4>
                {this.renderCreateCodebaseForm()}

                <h4 style={{color: "#666666"}}>Codebases</h4>
                {this.renderCodebases()}
            </div>
        );
    }
}
