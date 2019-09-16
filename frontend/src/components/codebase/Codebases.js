import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, ButtonGroup, Card, Button, Form, FormControl, Breadcrumb } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Codebases extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newCodebaseName: "",
            selectedFile: null, 
            isUploaded: "",
            codebases: [],
            codebase: {}
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewCodebaseName = this.handleChangeNewCodebaseName.bind(this);
        this.handleDeleteCodebase = this.handleDeleteCodebase.bind(this);
    }

    componentDidMount() {
        this.loadCodebases();
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            this.setState({
                codebases: response.data,
                codebase: response.data[0]
            });
        });
    }

    handleSelectedFile(event) {
        this.setState({
            selectedFile: event.target.files[0]
        });
    }

    handleSubmit(event){
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });
        
        const service = new RepositoryService();
        service.createCodebase(this.state.newCodebaseName, this.state.selectedFile).then(response => {
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
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleChangeNewCodebaseName(event) {
        this.setState({ 
            newCodebaseName: event.target.value 
        });
    }

    handleDeleteCodebase() {
        const service = new RepositoryService();
        service.deleteCodebase(this.state.codebase.name).then(response => {
            this.loadCodebases();
        });
    }

    changeCodebase(value) {
        this.setState({
            codebase: value
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item active>Codebases</Breadcrumb.Item>
                </Breadcrumb>
              </div>
            );
        };

        const codebases = this.state.codebases.map(codebase =>
            <Button key={codebase.name} active={this.state.codebase === codebase} onClick={() => this.changeCodebase(codebase)}>{codebase.name}</Button>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2>Codebases Manager</h2>
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
                                              this.state.selectedFile === null}>
                                Create Codebase
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>


                {codebases.length !== 0 &&
                    <div>
                        <div style={{overflow: "auto"}} className="mb-2">
                            <ButtonGroup>
                                {codebases}
                            </ButtonGroup>
                        </div>

                        <Card key={this.state.codebase.name} style={{ width: '20rem' }}>
                            <Card.Body>
                                <Card.Title>Codebase: {this.state.codebase.name}</Card.Title>
                                <Button href={`/codebase/${this.state.codebase.name}`} className="mb-2">Change Controller Profiles</Button><br/>
                                <Button href={`/codebase/${this.state.codebase.name}/dendrograms`} className="mb-2">Go to Dendrograms</Button><br/>
                                <Button href={`/codebase/${this.state.codebase.name}/experts`} className="mb-2">Go to Expert Cuts</Button><br/>
                                <Button onClick={this.handleDeleteCodebase} variant="danger">Delete</Button>
                            </Card.Body>
                        </Card>
                    </div>
                }
            </div>
        )
    }
}