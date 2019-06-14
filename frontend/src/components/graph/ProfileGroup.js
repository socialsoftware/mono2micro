import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ButtonGroup, Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, FormGroup, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';
import { Dendrograms } from './Dendrograms';

var HttpStatus = require('http-status-codes');

export class ProfileGroup extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            profileGroupName: this.props.match.params.profileGroupName,
            profileGroup: {},
            newProfileName: "",
            moveController: "",
            moveToProfile: "",
        };

        this.handleChangeNewProfileName = this.handleChangeNewProfileName.bind(this);
        this.handleNewProfileSubmit = this.handleNewProfileSubmit.bind(this);
        this.handleMoveControllerSubmit = this.handleMoveControllerSubmit.bind(this);
    }

    componentDidMount() {
        this.loadProfileGroup();
    }

    loadProfileGroup() {
        const service = new RepositoryService();
        service.getProfileGroup(this.state.profileGroupName).then(response => {
            this.setState({
                profileGroup: response.data
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
        service.addProfile(this.state.profileGroupName, this.state.newProfileName).then(response => {
            this.loadProfileGroup();
        });
    }

    handleMoveController(controller) {
        this.setState({
            moveController: controller
        });
    }

    handleMoveToProfile(profile) {
        this.setState({
            moveToProfile: profile
        });
    }

    handleMoveControllerSubmit() {
        const service = new RepositoryService();
        service.moveController(this.state.profileGroupName, this.state.moveController, this.state.moveToProfile).then(response => {
            this.loadProfileGroup();
        });
    }

    handleDeleteProfile(profile) {
        const service = new RepositoryService();
        service.deleteProfile(this.state.profileGroupName, profile).then(response => {
            this.loadProfileGroup();
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/profiles">Profiles</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.profileGroupName}</BreadcrumbItem>
                </Breadcrumb>
              </div>
            );
        };

        return (
            <div>
                <BreadCrumbs />

                <ButtonToolbar>
                <InputGroup className="mr-1">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="newprofile">New Profile</InputGroup.Text>
                    </InputGroup.Prepend>
                    <FormControl
                        type="text"
                        value={this.state.newProfileName}
                        onChange={this.handleChangeNewProfileName}/>
                </InputGroup>

                <Button className="mr-1" onClick={this.handleNewProfileSubmit}>Submit</Button>
                </ButtonToolbar>

                {Object.keys(this.state.profileGroup).length !== 0 &&
                    <span>
                    <div>
                    <Button className="mr-1 mt-2">Move</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveController} className="mr-1 mt-2">
                        {Object.values(this.state.profileGroup.profiles).flat().map(controller => <Dropdown.Item 
                            key={controller}
                            eventKey={controller}
                            onSelect={() => this.handleMoveController(controller)}>{controller}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button className="mr-1 mt-2">To</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveToProfile} className="mr-1 mt-2">
                        {Object.keys(this.state.profileGroup.profiles).map(p => <Dropdown.Item 
                            key={p}
                            eventKey={p}
                            onSelect={() => this.handleMoveToProfile(p)}>{p}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button className="mt-2" onClick={this.handleMoveControllerSubmit}>Submit</Button>
                    </div>

                    {Object.keys(this.state.profileGroup.profiles).map(profile =>
                        <span>
                        <span style={{fontSize: '30px', fontWeight: 'bold'}}>{profile}</span>

                        <Button onClick={() => this.handleDeleteProfile(profile)} className="mr-4" variant="danger" size="sm">-</Button>

                        <ul>
                            {this.state.profileGroup.profiles[profile].map(controller =>
                                <li>{controller}</li>
                            )}
                        </ul> 
                        </span>
                    )}
                    </span>
                }
            </div>
        );
    }
}