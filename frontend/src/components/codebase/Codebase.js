import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, ButtonGroup, Button, FormControl, Breadcrumb, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Codebase extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            codebase: {},
            newProfileName: "",
            moveToProfile: "",
            selectedControllers: [],
            isUploaded: ""
        };

        this.handleChangeNewProfileName = this.handleChangeNewProfileName.bind(this);
        this.handleNewProfileSubmit = this.handleNewProfileSubmit.bind(this);
        this.handleMoveControllersSubmit = this.handleMoveControllersSubmit.bind(this);
        this.handleSelectController = this.handleSelectController.bind(this);
    }

    componentDidMount() {
        this.loadCodebase();
    }

    loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(this.state.codebaseName).then(response => {
            this.setState({
                codebase: response.data
            });
        });
    }

    handleChangeNewProfileName(event) {
        this.setState({
            newProfileName: event.target.value
        });
    }

    handleNewProfileSubmit() {
        const service = new RepositoryService();
        service.addProfile(this.state.codebaseName, this.state.newProfileName).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadCodebase();
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
                    isUploaded: "Upload failed. Profile name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleMoveToProfile(profile) {
        this.setState({
            moveToProfile: profile
        });
    }

    handleMoveControllersSubmit() {
        const service = new RepositoryService();
        service.moveControllers(this.state.codebaseName, this.state.selectedControllers, this.state.moveToProfile).then(response => {
            this.setState({
                selectedControllers: []
            });
            this.loadCodebase();
        });
    }

    handleDeleteProfile(profile) {
        const service = new RepositoryService();
        service.deleteProfile(this.state.codebaseName, profile).then(response => {
            this.loadCodebase();
        });
    }

    handleSelectController(event) {
        if (this.state.selectedControllers.includes(event.target.id)) {
            let filteredArray = this.state.selectedControllers.filter(c => c !== event.target.id);
            this.setState({
                selectedControllers: filteredArray
            });
        } else {
            this.setState({
                selectedControllers: [...this.state.selectedControllers, event.target.id]
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
                        <Breadcrumb.Item active>{this.state.codebaseName}</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
            );
        };

        return (
            <div>
                <BreadCrumbs />

                <Form onSubmit={this.handleNewProfileSubmit}>
                    <Form.Group as={Row} controlId="newProfileName">
                        <Form.Label column sm={2}>
                            Profile Name
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="text"
                                maxLength="30"
                                placeholder="Profile Name"
                                value={this.state.newProfileName}
                                onChange={this.handleChangeNewProfileName}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={this.state.isUploaded === "Uploading..." ||
                                              this.state.newProfileName === ""}>
                                Create Profile
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                {Object.keys(this.state.codebase).length !== 0 &&
                    <div>
                        <ButtonToolbar>
                            <Button className="mr-1">Move selected controllers to</Button>

                            <DropdownButton as={ButtonGroup} title={this.state.moveToProfile === "" ? "Controller Profile" : this.state.moveToProfile} className="mr-1">
                                {Object.keys(this.state.codebase.profiles).map(p => <Dropdown.Item 
                                    key={p}
                                    onSelect={() => this.handleMoveToProfile(p)}>{p}</Dropdown.Item>)}
                            </DropdownButton>

                            <Button disabled={this.state.selectedControllers.length === 0 || this.state.moveToProfile === ""} onClick={this.handleMoveControllersSubmit}>Submit</Button>
                        </ButtonToolbar>

                        {Object.keys(this.state.codebase.profiles).map(profile =>
                            <div>
                                <span style={{fontSize: '30px', fontWeight: 'bold'}}>
                                    {profile}
                                    {this.state.codebase.profiles[profile].length === 0 && <Button onClick={() => this.handleDeleteProfile(profile)} variant="danger" size="sm">-</Button>}
                                </span>
                            
                                {this.state.codebase.profiles[profile].map(controller =>
                                    <Form.Check checked={this.state.selectedControllers.includes(controller)} style={{ paddingLeft: "3em" }} onClick={this.handleSelectController} label={controller} type="checkbox" id={controller} />
                                )}
                            </div>
                        )}
                    </div>
                }
            </div>
        );
    }
}