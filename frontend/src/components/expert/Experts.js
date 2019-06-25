import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, ButtonGroup, Card, Button, Form, FormControl, Breadcrumb } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Experts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            newExpert: "",
            isUploaded: "",
            experts: [],
            expert: ""
        };

        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewExpert = this.handleChangeNewExpert.bind(this);
        this.handleDeleteExpert = this.handleDeleteExpert.bind(this);
    }

    componentDidMount() {
        this.loadExperts();
    }

    loadExperts() {
        const service = new RepositoryService();
        service.getExpertNames(this.state.codebaseName).then(response => {
            this.setState({
                experts: response.data,
                expert: response.data[0]
            });
        });
    }

    handleSubmit(event){
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });
        
        const service = new RepositoryService();
        service.createExpert(this.state.codebaseName, this.state.newExpert).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadExperts();
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

    handleDeleteExpert() {
        const service = new RepositoryService();
        service.deleteExpert(this.state.codebaseName, this.state.expert).then(response => {
            this.loadExperts();
        });
    }

    changeExpert(value) {
        this.setState({
            expert: value
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
                <div>
                    <Breadcrumb>
                        <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                        <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                        <Breadcrumb.Item href={`/codebase/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                        <Breadcrumb.Item active>Experts</Breadcrumb.Item>
                    </Breadcrumb>
              </div>
            );
        };

        const experts = this.state.experts.map(expert =>
            <Button active={this.state.expert === expert} onClick={() => this.changeExpert(expert)}>{expert}</Button>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2>Experts Manager</h2>
                <Form onSubmit={this.handleSubmit}>
                    <Form.Group as={Row} controlId="newExpertName">
                        <Form.Label column sm={2}>
                            Expert Name
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="text"
                                maxLength="30"
                                placeholder="Expert Name"
                                value={this.state.newExpert}
                                onChange={this.handleChangeNewExpert}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={this.state.isUploaded === "Uploading..." ||
                                              this.state.newExpert === ""}>
                                Create Expert
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                {experts.length !== 0 &&
                    <div>
                        <div style={{overflow: "auto"}} className="mb-2">
                            <ButtonGroup>
                                {experts}
                            </ButtonGroup>
                        </div>

                        <Card key={this.state.expert} style={{ width: '20rem' }}>
                            <Card.Body>
                                <Card.Title>Expert: {this.state.expert}</Card.Title>
                                <Button href={`/codebase/${this.state.codebaseName}/expert/${this.state.expert}`} className="mb-2">Go to Expert</Button><br/>
                                <Button onClick={this.handleDeleteExpert} variant="danger">Delete</Button>
                            </Card.Body>
                        </Card>
                    </div>
                }
            </div>
        )
    }
}