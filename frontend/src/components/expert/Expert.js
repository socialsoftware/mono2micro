import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, ButtonGroup, Button, FormControl, Breadcrumb, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Expert extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            expertName: this.props.match.params.expertName,
            expert: {},
            newClusterName: "",
            moveToCluster: "",
            selectedEntities: [],
            isUploaded: ""
        };

        this.handleChangeNewClusterName = this.handleChangeNewClusterName.bind(this);
        this.handleNewClusterSubmit = this.handleNewClusterSubmit.bind(this);
        this.handleMoveEntitiesSubmit = this.handleMoveEntitiesSubmit.bind(this);
        this.handleSelectEntity = this.handleSelectEntity.bind(this);
    }

    componentDidMount() {
        this.loadExpert();
    }

    loadExpert() {
        const service = new RepositoryService();
        service.getExpert(this.state.codebaseName, this.state.expertName).then(response => {
            this.setState({
                expert: response.data
            });
        });
    }

    handleChangeNewClusterName(event) {
        this.setState({
            newClusterName: event.target.value
        });
    }

    handleNewClusterSubmit(event) {
        event.preventDefault();
        const service = new RepositoryService();
        service.addCluster(this.state.codebaseName, this.state.expertName, this.state.newClusterName).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadExpert();
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
                    isUploaded: "Upload failed. Cluster name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleMoveToCluster(cluster) {
        this.setState({
            moveToCluster: cluster
        });
    }

    handleMoveEntitiesSubmit() {
        const service = new RepositoryService();
        service.moveEntities(this.state.codebaseName, this.state.expertName, this.state.selectedEntities, this.state.moveToCluster).then(response => {
            this.setState({
                selectedEntities: []
            });
            this.loadExpert();
        });
    }

    handleDeleteCluster(cluster) {
        const service = new RepositoryService();
        service.deleteCluster(this.state.codebaseName, this.state.expertName, cluster).then(response => {
            this.loadExpert();
        });
    }

    handleSelectEntity(event) {
        if (this.state.selectedEntities.includes(event.target.id)) {
            let filteredArray = this.state.selectedEntities.filter(c => c !== event.target.id);
            this.setState({
                selectedEntities: filteredArray
            });
        } else {
            this.setState({
                selectedEntities: [...this.state.selectedEntities, event.target.id]
            });
        }
    }

    render() {
        const BreadCrumbs = () => {
            return (
                <div>
                    <Breadcrumb>
                        <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                        <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                        <Breadcrumb.Item href={`/codebase/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                        <Breadcrumb.Item href={`/codebase/${this.state.codebaseName}/experts`}>Experts</Breadcrumb.Item>
                        <Breadcrumb.Item active>{this.state.expertName}</Breadcrumb.Item>
                    </Breadcrumb>
              </div>
            );
        };

        return (
            <div>
                <BreadCrumbs />

                <Form onSubmit={this.handleNewClusterSubmit}>
                    <Form.Group as={Row} controlId="newClusterName">
                        <Form.Label column sm={2}>
                            Cluster Name
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="text"
                                maxLength="30"
                                placeholder="Cluster Name"
                                value={this.state.newClusterName}
                                onChange={this.handleChangeNewClusterName}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={this.state.isUploaded === "Uploading..." ||
                                              this.state.newClusterName === ""}>
                                Create Cluster
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                {Object.keys(this.state.expert).length !== 0 &&
                    <div>
                        <ButtonToolbar>
                            <Button className="mr-1">Move selected entities to</Button>

                            <DropdownButton as={ButtonGroup} title={this.state.moveToCluster === "" ? "Cluster" : this.state.moveToCluster} className="mr-1">
                                {Object.keys(this.state.expert.clusters).map(c => <Dropdown.Item 
                                    key={c}
                                    onSelect={() => this.handleMoveToCluster(c)}>{c}</Dropdown.Item>)}
                            </DropdownButton>

                            <Button disabled={this.state.selectedEntities.length === 0 || this.state.moveToCluster === ""} onClick={this.handleMoveEntitiesSubmit}>Submit</Button>
                        </ButtonToolbar>

                        {Object.keys(this.state.expert.clusters).map(cluster =>
                            <div>
                                <span style={{fontSize: '30px', fontWeight: 'bold'}}>
                                    {cluster}
                                    {this.state.expert.clusters[cluster].length === 0 && <Button onClick={() => this.handleDeleteCluster(cluster)} variant="danger" size="sm">-</Button>}
                                </span>
                            
                                {this.state.expert.clusters[cluster].map(entity =>
                                    <Form.Check checked={this.state.selectedEntities.includes(entity)} style={{ paddingLeft: "3em" }} onClick={this.handleSelectEntity} label={entity} type="checkbox" id={entity} />
                                )}
                            </div>
                        )}
                    </div>
                }
            </div>
        );
    }
}