import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';

var HttpStatus = require('http-status-codes');

export class Profiles extends React.Component {
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
                codebase: response.data === null ? {} : response.data
            });
        });
    }

    handleChangeNewProfileName(event) {
        this.setState({
            newProfileName: event.target.value
        });
    }

    handleNewProfileSubmit(event) {
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });

        const service = new RepositoryService();
        service.addProfile(this.state.codebaseName, this.state.newProfileName).then(response => {
            if (response.status === HttpStatus.OK) {
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
            if (error.response?.status === HttpStatus.UNAUTHORIZED) {
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
        
        service.moveControllers(
            this.state.codebaseName, 
            this.state.selectedControllers, 
            this.state.moveToProfile
        ).then(response => {
            this.setState({
                selectedControllers: []
            });
            this.loadCodebase();
        });
    }

    handleDeleteProfile(profile) {
        const service = new RepositoryService();
        
        service.deleteProfile(
            this.state.codebaseName,
            profile
        ).then(response => {
            this.loadCodebase();
        });
    }

    handleSelectController(event) {
        const eventTargetId = event.target.id;
        const { selectedControllers } = this.state;

        if (selectedControllers.includes(eventTargetId)) {
            this.setState({
                selectedControllers: selectedControllers.filter(c => c !== eventTargetId)
            });

            return;
        }

        this.setState({
            selectedControllers: [...selectedControllers, eventTargetId]
        });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}`}>
                    {this.state.codebaseName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    Profiles
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {
        const {
            newProfileName,
            isUploaded,
            codebase,
            moveToProfile,
            codebaseName,
            selectedControllers,
        } = this.state;


        return (
            <div>
                {this.renderBreadCrumbs()}
                <h4 style={{color: "#666666"}}>
                    Create Controller Profile
                </h4>

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
                                value={newProfileName}
                                onChange={this.handleChangeNewProfileName}
                            />
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button 
                                type="submit"
                                disabled={isUploaded === "Uploading..." || newProfileName === ""}
                            >
                                Create Profile
                            </Button>
                            <Form.Text>
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                <h4 style={{color: "#666666"}}>
                    Controller Profiles
                </h4>
                {Object.keys(codebase).length &&
                    <div>
                        <ButtonToolbar>
                            <Button className="mr-1">Move selected controllers to</Button>

                            <DropdownButton 
                                as={ButtonGroup} 
                                title={moveToProfile === "" ? "Controller Profile" : moveToProfile} 
                                className="mr-1"
                            >
                                {Object.keys(codebase.profiles).map(profile =>
                                    <Dropdown.Item 
                                        key={profile}
                                        onSelect={() => this.handleMoveToProfile(profile)}
                                    >
                                            {profile}
                                    </Dropdown.Item>
                                )}
                            </DropdownButton>

                            <Button 
                                disabled={selectedControllers.length === 0 || moveToProfile === ""} 
                                onClick={this.handleMoveControllersSubmit}
                            >
                                Submit
                            </Button>
                        </ButtonToolbar>

                        {Object.keys(codebase.profiles).map(profile =>
                            <div key={profile}>
                                <div style={{fontSize: '25px'}}>
                                    {profile}
                                    {!codebase.profiles[profile].length && 
                                        <Button 
                                            onClick={() => this.handleDeleteProfile(profile)} 
                                            className="ml-2"
                                            variant="danger" 
                                            size="sm"
                                        >
                                            -
                                        </Button>
                                    }
                                </div>
                            
                                {codebase.profiles[profile].map(controller =>
                                    <Form.Check
                                        id={controller}
                                        key={controller}
                                        type="checkbox" 
                                        label={controller} 
                                        checked={selectedControllers.includes(controller)} 
                                        onChange={this.handleSelectController} 
                                        style={{ paddingLeft: "3em" }} 
                                    />
                                )}
                            </div>
                        )}
                    </div>
                }
            </div>
        );
    }
}