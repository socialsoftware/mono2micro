import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Card, Button, Form, FormControl, Breadcrumb } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Codebases extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newCodebaseName: "",
            selectedFile: null, 
            isUploaded: "",
            codebases: [],
            checkedAnalysisType: "",
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewCodebaseName = this.handleChangeNewCodebaseName.bind(this);
        this.handleDeleteCodebase = this.handleDeleteCodebase.bind(this);
        this.handleSelectAnalysisType = this.handleSelectAnalysisType.bind(this);
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
            selectedFile: event.target.files[0]
        });
    }

    handleChangeNewCodebaseName(event) {
        this.setState({ 
            newCodebaseName: event.target.value
        });
    }

    handleSelectAnalysisType(event) {
        console.log(event.target.value);
        this.setState({
            checkedAnalysisType: event.target.value
        });
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
        
        const service = new RepositoryService();
        service.createCodebase(
            this.state.newCodebaseName,
            this.state.selectedFile,
            this.state.checkedAnalysisType, // either static or dynamic
        ).then(response => {
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
                <Form.Group as={Row} controlId="newCodebaseName" className="align-items-center">
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

                <Form.Group as={Row} controlId="datafile" className="align-items-center">
                    <Form.Label column sm={2}>
                        Data Collection File
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="file"
                            onChange={this.handleSelectedFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} key={"analysis-type-section"} className="align-items-center">
                    <Form.Label column sm={2}>
                        Analysis type
                    </Form.Label>
                    <Col sm={5} >
                        <Form.Check
                            inline
                            id="static-analysis-radio-button"
                            type="radio"
                            value="static"
                            label="Static analysis"
                            onChange={this.handleSelectAnalysisType}
                            checked={this.state.checkedAnalysisType === "static"}
                        />
                        <Form.Check
                            inline
                            id="dynamic-analysis-radio-button"
                            type="radio"
                            value="dynamic"
                            label="Dynamic analysis"
                            onChange={this.handleSelectAnalysisType}
                            checked={this.state.checkedAnalysisType === "dynamic"}
                        />
                    </Col>
                </Form.Group>
                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button 
                            type="submit"
                            disabled={
                                this.state.isUploaded === "Uploading..." ||
                                !this.state.newCodebaseName.length ||
                                this.state.selectedFile === null ||
                                !this.state.checkedAnalysisType.length
                            }
                        >
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