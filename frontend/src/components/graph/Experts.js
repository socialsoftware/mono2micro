import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { DropdownButton, Dropdown, ButtonGroup, Card, Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, FormGroup, ButtonToolbar } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <BreadcrumbItem active>Experts</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class Experts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newExpert: "",
            codebases: [],
            codebase: "",
            isUploaded: "",
            experts: [],
            expert: ""
        };

        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeNewExpert = this.handleChangeNewExpert.bind(this);
    }

    componentDidMount() {
        this.load();
    }

    load() {
        const service = new RepositoryService();
        service.getCodebaseNames().then(response => {
            this.setState({
                codebases: response.data
            });
        });

        service.getExpertNames().then(response => {
            this.setState({
                experts: response.data,
                expert: response.data === [] ? "" : response.data[0]
            });
        });
    }

    handleUpload(event){
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });
        
        const service = new RepositoryService();
        service.createExpert(this.state.newExpert, this.state.codebase).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.load();
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
                    isUploaded: "Upload failed. Expert name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleChangeNewExpert(event) {
        this.setState({ 
            newExpert: event.target.value 
        });
    }

    setCodebase(codebase) {
        this.setState({
            codebase: codebase
        });
    }

    handleDeleteExpert(expertName) {
        const service = new RepositoryService();
        service.deleteExpert(expertName).then(response => {
            this.load();
        });
    }

    render() {
        const experts = this.state.experts.map(expert =>
            <Button active={this.state.expert === expert} onClick={() => this.changeExpert(expert)}>{expert}</Button>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-4">Experts Manager</h2>
                <Form onSubmit={this.handleUpload}>
                    <ButtonToolbar>
                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="expert">Expert Name</InputGroup.Text>
                    </InputGroup.Prepend>
                        <FormControl 
                            type="text"
                            maxLength="18"
                            value={this.state.newExpert}
                            onChange={this.handleChangeNewExpert}/>
                    </InputGroup>
                    </ButtonToolbar>

                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="basic-addon1">Codebase Name</InputGroup.Text>
                    </InputGroup.Prepend>
                        <DropdownButton
                            title={this.state.codebase === "" ? "Select Codebase" : this.state.codebase}
                            id="input-group-dropdown-1"
                            >
                            {this.state.codebases.map(codebase => <Dropdown.Item onClick={() => this.setCodebase(codebase)}>{codebase}</Dropdown.Item>)}
                        </DropdownButton>
                    </InputGroup>
                    
                    <Button variant="primary" type="submit" disabled={this.state.newExpert === "" || this.state.codebase === ""}>
                        Submit
                    </Button>
                    <br />
                    {this.state.isUploaded}
                    <br /><br />
                </Form>

                {experts.length !== 0 &&
                    <div>
                    <div style={{overflow: "auto"}} className="mb-3">
                    <ButtonGroup>
                        {experts}
                    </ButtonGroup>
                    </div>

                    <Card className="mb-5" key={this.state.expert} style={{ width: '17rem' }}>
                        <Card.Body>
                            <Card.Title>{this.state.expert}</Card.Title>
                            <Button href={`/expert/${this.state.expert}`} className="mr-4" variant="primary">See Expert</Button>
                            <Button onClick={() => this.handleDeleteExpert(this.state.expert)} variant="danger">Delete</Button>
                        </Card.Body>
                    </Card>
                    </div>
                }
            </div>
        )
    }
}