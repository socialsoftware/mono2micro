import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ButtonGroup, Card, Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, FormGroup, ButtonToolbar } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <BreadcrumbItem active>Codebases</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class Codebases extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newCodebase: "",
            selectedFile: null, 
            isUploaded: "",
            codebases: [],
            codebase: ""
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeNewCodebase = this.handleChangeNewCodebase.bind(this);
        this.handleDeleteCodebase = this.handleDeleteCodebase.bind(this);
    }

    componentDidMount() {
        this.loadCodebases();
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebaseNames().then(response => {
            this.setState({
                codebases: response.data,
                codebase: response.data === [] ? "" : response.data[0]
            });
        });
    }

    handleSelectedFile(event) {
        this.setState({
            selectedFile: event.target.files[0],
            isUploaded: ""
        });
    }

    handleUpload(event){
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });
        
        const service = new RepositoryService();
        service.createCodebase(this.state.newCodebase, this.state.selectedFile).then(response => {
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

    handleChangeNewCodebase(event) {
        this.setState({ 
            newCodebase: event.target.value 
        });
    }

    handleDeleteCodebase(codebaseName) {
        const service = new RepositoryService();
        service.deleteCodebase(codebaseName).then(response => {
            this.loadCodebases();
        });
    }

    changeCodebase(value) {
        this.setState({
            codebase: value
        });
    }

    render() {
        const codebases = this.state.codebases.map(codebase =>
            <Button active={this.state.codebase === codebase} onClick={() => this.changeCodebase(codebase)}>{codebase}</Button>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-4">Codebases Manager</h2>
                <Form onSubmit={this.handleUpload}>
                    <ButtonToolbar>
                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="codebase">Codebase Name</InputGroup.Text>
                    </InputGroup.Prepend>
                        <FormControl 
                            type="text"
                            maxLength="18"
                            value={this.state.newCodebase}
                            onChange={this.handleChangeNewCodebase}/>
                    </InputGroup>
                    </ButtonToolbar>

                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="datafile">Data Collection File</InputGroup.Text>
                    </InputGroup.Prepend>
                        <FormGroup>
                            <FormControl
                                type="file"
                                onChange={this.handleSelectedFile}/>
                        </FormGroup>
                    </InputGroup>
                    
                    <Button variant="primary" type="submit" disabled={this.state.isUploaded === "Uploading..." || this.state.newCodebase === "" || this.state.selectedFile === null}>
                        Submit
                    </Button>
                    <br />
                    {this.state.isUploaded}
                    <br /><br />
                </Form>

                {codebases.length !== 0 &&
                    <div>
                    <div style={{overflow: "auto"}} className="mb-3">
                    <ButtonGroup>
                        {codebases}
                    </ButtonGroup>
                    </div>

                    <Card className="mb-5" key={this.state.codebase} style={{ width: '17rem' }}>
                        <Card.Body>
                            <Card.Title>{this.state.codebase}</Card.Title>
                            <Button href={`/codebase/${this.state.codebase}`} className="mr-4" variant="primary">See Codebase</Button>
                            <Button onClick={() => this.handleDeleteCodebase(this.state.codebase)} variant="danger">Delete</Button>
                        </Card.Body>
                    </Card>
                    </div>
                }
            </div>
        )
    }
}